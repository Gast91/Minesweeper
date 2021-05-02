package minesweeper;

import minesweeper.banner.BombIndicator;
import minesweeper.banner.MinesweeperBanner;
import minesweeper.banner.StatusIndicator;
import minesweeper.banner.StatusIndicatorClickedListener;
import minesweeper.banner.TimeIndicator;
import minesweeper.difficulty.CustomDifficulty;
import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.Dimensions;
import minesweeper.game.MinesweeperGameManager;
import minesweeper.game.cells.CellValue;
import minesweeper.game.cells.MinesweeperButton;
import minesweeper.stats.GameStats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import static java.util.function.Predicate.not;
import static minesweeper.utility.Icon.*;

public class Minesweeper extends JFrame {
    private static MinesweeperBanner banner;
    private JPanel cellPanel;
    private JMenu diffMenu;
    private int[] bombLoc;
    private MinesweeperButton[] cells;

    private static final Path ABOUT_PATH = Path.of("src/resources/About.mine");
    private static final MinesweeperGameManager gameManager = MinesweeperGameManager.getInstance();

    public MinesweeperButton getCell(int position) {
        return cells[position];
    }
     
    public static void main(String[] args)
    {
        instance = new Minesweeper();
        instance.setVisible(true);
    }

    private static Minesweeper instance = null;

    public static Minesweeper getInstance() {
        if (instance == null)
            instance = new Minesweeper();

        return instance;
    }

    public Minesweeper()
    {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Minesweeper");
        setIconImage((ICON.getImage()));

        createMenuBar();

        final Font gameFont = new Font("Verdana", Font.BOLD, 16);
        banner = new MinesweeperBanner.BannerBuilder()
                .withTimeIndicator(new TimeIndicator(gameFont))
                .withStatusIndicator(new StatusIndicator(onStatusIndicatorClick()))
                .withBombIndicator(new BombIndicator(gameFont, gameManager.getDifficulty()))
                .build();
        add(banner, BorderLayout.NORTH);

        createGrid();

        add(banner, BorderLayout.NORTH);
        add(cellPanel);
        pack();
        
        setResizable(false);
        setLocationRelativeTo(null); 
    }
    
    //Creates the initial grid of size X and initialises all the cells (X = ROWS * COLS, specified by the user)  + MOUSE LISTENER FOR GRID
    private void createGrid() {
        Difficulty difficulty = gameManager.getDifficulty();

        cells = new MinesweeperButton[difficulty.getDimensions().toArea()];
        //Panel that stores all the cells
        cellPanel = new JPanel(new GridLayout(difficulty.getRows(), difficulty.getCols()));
        for (int i = 0; i < difficulty.getDimensions().toArea(); i++) {
            cells[i] = new MinesweeperButton(i, difficulty.getRows(), difficulty.getCols());
            cells[i].addMouseListener(new MouseAdapter() {  
            @Override
            public void mousePressed(MouseEvent e) {
                MinesweeperButton selected = ((MinesweeperButton)e.getSource());
                // Toggle cell flag; user can win if they mark the last bomb through this
                if (e.getButton() == MouseEvent.BUTTON3
                        && gameManager.isGameRunning()
                        && !selected.isRevealed())
                    toggleFlag(selected);

                // Start game if first reveal, check if bomb and end game or reveal otherwise
                else if (e.getButton() == MouseEvent.BUTTON1
                        && (gameManager.isGameWaiting() || gameManager.isGameRunning())
                        && selected.inInitialState())
                    checkAndReveal(selected);

                // Reveal (if flagged neighbors equal to cell value) or highlight neighbors
                else if (e.getButton() == MouseEvent.BUTTON2
                        && gameManager.isGameRunning()
                        && !selected.isFlagged() && selected.isRevealed())
                    checkFlags(selected);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                MinesweeperButton selected = ((MinesweeperButton)e.getSource());
                if (e.getButton() == MouseEvent.BUTTON2 && selected.isHighlighted())
                    highlightNeighbors(selected, false);
            }
            });
            cellPanel.add(cells[i]);
            cellPanel.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.BLACK));  //ARGS => TOP, BOTTOM, LEFT, RIGHT
        }
    }

    private void checkAndReveal(MinesweeperButton selected) {
        if (gameManager.isGameWaiting()) {
            diffMenu.setEnabled(false);
            generateBombs(selected);
            selected.reveal();
            banner.getTimeIndicator().startTimer();
            gameManager.setGameStatus(GameStatus.RUNNING);
        } else if (selected.isBomb()) gameOver(selected);
        else selected.reveal();
    }

    private void toggleFlag(MinesweeperButton selected) {
        int bombsMarked = gameManager.getBombsFlagged();
        BombIndicator bi = banner.getBombIndicator();
        if (selected.isFlagged()) {
            bi.increment();
            selected.setIcon(null);
            if (selected.isBomb()) gameManager.setBombsFlagged(--bombsMarked);
        }
        else if (!selected.isRevealed()) {
            bi.decrement();
            selected.setIcon(FLAG.getIcon());
            if (selected.isBomb()) gameManager.setBombsFlagged(++bombsMarked);

            // If the user has marked all the bombs (and no extra!) they win
            if (gameManager.hasMarkedAll() && !bi.counterIsNegative()) {
                bi.setForeground(new Color(0,153,0));
                banner.getStatusIndicator().setIcon(WIN.getIcon());
                GameStats.getInstance().updateStats(true, gameManager.getDifficulty().getType(), banner.getTimeIndicator().stopTimer());
                gameManager.setGameStatus(GameStatus.WON);
                diffMenu.setEnabled(true);
            }
        }
    }
    
    //Creates the MenuBar and all of its subcomponents and adds their functionality
    private void createMenuBar()
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        menuBar.add(gameMenu);
        
        diffMenu = new JMenu("Difficulty");     //Difficulty Sub Menu
        gameMenu.add(diffMenu);
        gameMenu.addSeparator();
        
        //Difficulty Sub Menu Radio Buttons - Create them, group them all together to prevent multiple radio buttons being selected at the same time and add a listener to each of them
        diffMenu.add(new JRadioButtonMenuItem("Beginner"));
        diffMenu.add(new JRadioButtonMenuItem("Intermediate"));
        diffMenu.add(new JRadioButtonMenuItem("Expert"));
        diffMenu.getItem(gameManager.getDifficulty().toInt()).setSelected(true);
        ButtonGroup rbGroup = new ButtonGroup();  
        for (int i = 0; i < 3; i++)
        {
            rbGroup.add(diffMenu.getItem(i));
            diffMenu.getItem(i).addItemListener(e -> {
                //When a radio button is selected, set the difficulty according to the component order, mark that the preset changed and restart the game with the correct values
                gameManager.setPresetChanged(true);
                if (e.getStateChange() == ItemEvent.SELECTED)
                    gameManager.setDifficulty(DifficultyPreset.fromInt(((JMenuItem)e.getSource()).getParent().getComponentZOrder(((JMenuItem)e.getSource()))));
                restart();
            }); //Radio Button Listener
            diffMenu.getItem(i).setAccelerator(KeyStroke.getKeyStroke(i + 49, KeyEvent.SHIFT_DOWN_MASK));  //SHIFT + 1 TO 3 Difficulty Keybinds
        }
        diffMenu.addSeparator();
        JMenuItem custom = new JMenuItem("Custom..");
        diffMenu.add(custom); 
        custom.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, KeyEvent.SHIFT_DOWN_MASK));  //SHIFT + 4 Custom Difficulty Keybind
        custom.addActionListener(ae -> {
            JTextField r = new JTextField();
            JTextField c = new JTextField();
            JTextField b = new JTextField();
            JLabel inputInfo = new JLabel();    //INVALID INPUT LABEL
            inputInfo.setForeground(Color.RED);

            JPanel inputPanel = new JPanel();
            inputPanel.setLayout(new GridLayout(4, 2));
            inputPanel.add(new JLabel("Rows:"));
            inputPanel.add(r);
            r.setToolTipText("Value must be between 5 and 25");
            inputPanel.add(new JLabel("Columns:"));
            inputPanel.add(c);
            c.setToolTipText("Value must be between 5 and 45");
            inputPanel.add(new JLabel("Mines:"));
            inputPanel.add(b);
            b.setToolTipText("Value must be less than (Rows X Cols) - 9");
            inputPanel.add(inputInfo);

            Object[] options = {"Submit", "Cancel"};  //MessageDialog Buttons' Text
            final JOptionPane optionPane = new JOptionPane(inputPanel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, options);
            final javax.swing.JDialog dialog = new javax.swing.JDialog();
            dialog.setTitle("Customize Difficulty");
            dialog.setContentPane(optionPane);

            optionPane.addPropertyChangeListener(e -> {
                if (JOptionPane.VALUE_PROPERTY.equals(e.getPropertyName()))
                {
                    if (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE)  //Wait until the user clicks on a button
                        return;

                    if (optionPane.getValue().equals(options[0]))   //if the user clicked on submit
                    {
                        if (inputIsValid(r.getText(), c.getText(), b.getText(), inputInfo))  //and if the input is valid update the game's variables according to the input and restart the game
                        {
                            MinesweeperGameManager.getInstance().setDifficulty(new CustomDifficulty(
                                    new Dimensions(Integer.parseInt(r.getText()), Integer.parseInt(c.getText())),
                                    Integer.parseInt(b.getText())));
                            rbGroup.clearSelection();
                            gameManager.setPresetChanged(true);
                            dialog.dispose();
                            restart();
                        }
                        else  //if the input is not valid, display an error message and reset the value of the optionPane
                            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE); //If the user presses the same button next time, without this being reseted, no property change event will be fired
                    }
                    else
                        dialog.dispose();
                }
            });  //Listener for the MessageDialog's Buttons
            dialog.pack();
            dialog.setIconImage(ICON.getImage());
            dialog.setVisible(true);
            dialog.setLocationRelativeTo(Minesweeper.this);
        });     //Listener for the Custom Menu Item that brings up a MessageDialog where the user can input his custom difficulty
        
        //About Menu and Listener for MessageDialog
        JMenuItem about = new JMenuItem("About");
        about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));  //F1 Keybind for the "About" MessageDialog
        gameMenu.add(about);
        about.addActionListener(ae -> JOptionPane.showMessageDialog(Minesweeper.this, readAboutFile(), "About", JOptionPane.PLAIN_MESSAGE, ICON.getIcon()));    //Clicking on "About" loads information from a txt file
        
        //Stats Menu and Listener for MessageDialog
        JMenu statsMenu = new JMenu("Stats");
        menuBar.add(statsMenu);
        // Listener for the Stats Menu that brings up the Stats Message Dialog
        statsMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) 
            {
                javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
                JTextArea bTab = new JTextArea(GameStats.getInstance().getBeginnerStats().toString());
                JTextArea iTab = new JTextArea(GameStats.getInstance().getIntermediateStats().toString());
                JTextArea eTab = new JTextArea(GameStats.getInstance().getExpertStats().toString());
                bTab.setEditable(false);
                iTab.setEditable(false);
                eTab.setEditable(false);
                tabs.addTab("Beginner", bTab);
                tabs.addTab("Intermediate", iTab);
                tabs.addTab("Expert", eTab);
                    
                Object[] options = {"Ok", "Reset"};  //MessageDialog Buttons' Text
                final JOptionPane optionPane = new JOptionPane(tabs, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options);
                final javax.swing.JDialog dialog = new javax.swing.JDialog();
                dialog.setTitle("Stats");
                dialog.setContentPane(optionPane);
                // Listener for the MessageDialog's Buttons
                optionPane.addPropertyChangeListener(e1 -> {
                    if (JOptionPane.VALUE_PROPERTY.equals(e1.getPropertyName())) {
                        // Wait until the user clicks on a button
                        if (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE) return;

                        // Reset the currently showed tab (along with the stats associated with the difficulty the tab represents)
                        if (optionPane.getValue().equals(options[1])) {
                            if (bTab.isShowing()) {
                                 GameStats.getInstance().resetStats(DifficultyPreset.BEGINNER);
                                 bTab.setText(GameStats.getInstance().getStats(DifficultyPreset.BEGINNER).toString());
                            } else if (iTab.isShowing()) {
                                GameStats.getInstance().resetStats(DifficultyPreset.INTERMEDIATE);
                                iTab.setText(GameStats.getInstance().getStats(DifficultyPreset.INTERMEDIATE).toString());
                            } else {
                                GameStats.getInstance().resetStats(DifficultyPreset.EXPERT);
                                eTab.setText(GameStats.getInstance().getStats(DifficultyPreset.EXPERT).toString());
                            }
                            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                        }
                        else dialog.dispose();
                    }
                });
                dialog.pack();
                dialog.setIconImage(ICON.getImage());
                dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setLocationRelativeTo(Minesweeper.this);
                dialog.setVisible(true);    
            }
        });
        
        setJMenuBar(menuBar);
    }
    
    //Checks if the user's custom difficulty inputs are appropriate and displays an error message if needed
    private boolean inputIsValid(String r, String c, String b, JLabel i)
    {
        if (r.equals("") || c.equals("") || b.equals(""))
        {
            i.setText("Empty Fields..");
            return false;
        }
        else
        {
            int rs, cs, bs;
            try
            {
                rs = Integer.parseInt(r);
                cs = Integer.parseInt(c);
                bs = Integer.parseInt(b);
            }
            catch(NumberFormatException e)
            {
                i.setText("Invalid Input..");
                return false;
            }
            if (bs < ((rs * cs) - 9) && rs >= 5 && cs >= 5 && rs <= 25 && cs <= 45) //GRID SIZE BETWEEN 5x5 AND 25x45 AND TOTAL BOMBS MUST BE LESS THAT GRID_SIZE - 9 TO ENSURE THAT THE USER'S FIRST CLICK IS "SAFE"
                return true;
            else
            {
                i.setText("Grid Size/Mines..");
                return false;
            }
        }
    }

    private static String readAboutFile() {
        try {
            return Files.readString(ABOUT_PATH);
        } catch (IOException ignored) {
            return "File not found";
        }
    }
    
    //Reveal all the bombs and stop the game if a bomb is revealed
    private void gameOver(MinesweeperButton selected) {
        selected.setIcon(BOMB_EXPLODED.getIcon());
        for (int j : bombLoc) cells[j].reveal();
        banner.getStatusIndicator().setIcon(LOSE.getIcon());

        GameStats.getInstance().updateStats(false, gameManager.getDifficulty().getType(), banner.getTimeIndicator().stopTimer());

        gameManager.setGameStatus(GameStatus.LOST);
        diffMenu.setEnabled(true);
    }

    private void checkFlags(MinesweeperButton selected) {
        long neighborsFlagged = selected.getNeighbors()
                .filter(MinesweeperButton::isFlagged)
                .count();

        if (neighborsFlagged < selected.getValue().asInt()) {
            highlightNeighbors(selected, true);
            return;
        }

        selected.getNeighbors()
                .filter(MinesweeperButton::inInitialState)
                .forEach(n -> {
                    if (n.isBomb()) gameOver(n);
                    else n.reveal();
                });
    }

    private void highlightNeighbors(MinesweeperButton selected, boolean state) {
        selected.setHighlighted(state);

        selected.getNeighbors()
                .filter(MinesweeperButton::inInitialState)
                .forEach(n -> n.getModel().setRollover(state));
    }

    private void generateBombs(MinesweeperButton selected) {
        bombLoc = new Random()
                .ints(0, gameManager.getDifficulty().getDimensions().toArea())
                .filter(loc -> isValidBombLocation(selected, loc))
                .distinct()
                .limit(gameManager.getDifficulty().getBombCount())
                .toArray();

        configureCells();
    }

    // Checks if the number generated is a valid location for a bomb
    private boolean isValidBombLocation(MinesweeperButton selected, int r)  {
        // First selection cannot be a bomb
        if (selected.getPosition() == r)  return false;

        // First selection neighbors must not be bombs to ensure a better start
        for (int n : selected.getNeighborsPositions())
            if (n == r) return false;

        return true;
    }

    // Update each cell's value depending on how many adjacent bombs there are
    private void configureCells() {
        Arrays.stream(bombLoc)
                .forEach( bl -> {
                    MinesweeperButton currentCell = cells[bl];
                    currentCell.setValue(CellValue.BOMB);
                    currentCell.getNeighbors()
                            .filter(not(MinesweeperButton::isBomb))
                            .forEach(MinesweeperButton::incrementValue);
                });
    }

    private void restart() {
        if (gameManager.hasPresetChanged()) {
            Container c = cellPanel.getParent();
            c.remove(cellPanel);

            createGrid();
            c.add(cellPanel);

            //revalidate();
            repaint();
            pack();
            
            //Recenter the window
            setLocationRelativeTo(null);
        } else  // If the difficulty is still the same, just reset the values of every cell's properties
            for (MinesweeperButton cell : cells) cell.reset();

        gameManager.reset();
        banner.reset(gameManager.getDifficulty().getBombCount());
        diffMenu.setEnabled(true);
        gameManager.setPresetChanged(false);
    }

    private StatusIndicatorClickedListener onStatusIndicatorClick() {
        return e -> {
            if (e.getButton() == MouseEvent.BUTTON1 && !gameManager.isGameWaiting())
                restart();
        };
    }
}