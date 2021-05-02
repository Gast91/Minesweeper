package minesweeper;

import minesweeper.banner.TimeIndicator;
import minesweeper.difficulty.CustomDifficulty;
import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.Dimensions;
import minesweeper.game.MinesweeperGameManager;
import minesweeper.game.cells.CellValue;
import minesweeper.game.cells.MinesweeperButton;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
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
import javax.swing.SwingConstants;

import static java.util.function.Predicate.not;
import static minesweeper.utility.Icon.*;

public class Minesweeper extends JFrame {
    private JPanel cellPanel, banner;
    private JButton statusIndicator;
    private JLabel bombIndicator;
    private static TimeIndicator timeIndicator;
    private JMenu diffMenu;
    private int[] bombLoc;
    private MinesweeperButton[] cells;
    private final long[] stats = new long[] {0, 0, 0 ,0 ,0 ,0 , 0, 0, 0, 0, 0, 0};//ARRAY THAT STORES IN MEMORY THE USER'S STATS TAKEN FROM A FILE (ORDER: BEG/INTER/EXP - GAMES PLAYED, WON, %, BEST TIME)

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
        
        readFile("Stats.mine");  //Load the stats file into memory/or create/recreate the stats file if it doesnt exist/is corrupted
        createMenuBar();      
        createGrid();
        createBanner();
        
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
            timeIndicator.startTimer();
            gameManager.setGameStatus(GameStatus.RUNNING);
        } else if (selected.isBomb()) gameOver(selected);
        else selected.reveal();
    }

    private void toggleFlag(MinesweeperButton selected) {
        int bombsMarked = gameManager.getBombsFlagged();
        if (selected.isFlagged()) {
            bombIndicator.setText(Integer.toString(Integer.parseInt(bombIndicator.getText()) + 1));  // Update the indicator with the number of potential bombs left
            selected.setIcon(null);
            if (selected.isBomb()) gameManager.setBombsFlagged(--bombsMarked);
        }
        else if (!selected.isRevealed()) {
            bombIndicator.setText(Integer.toString(Integer.parseInt(bombIndicator.getText()) - 1)); // Update the indicator with the number of potential bombs left
            selected.setIcon(FLAG.getIcon());
            if (selected.isBomb()) gameManager.setBombsFlagged(++bombsMarked);
            //If the user has marked all the bombs, he wins
            if (gameManager.hasMarkedAll() && Integer.parseInt(bombIndicator.getText()) == 0) {
                bombIndicator.setForeground(new Color(0,153,0));
                statusIndicator.setIcon(WIN.getIcon());
                if (gameManager.getDifficulty() != DifficultyPreset.CUSTOM) {
                    updateStats(true, gameManager.getDifficulty().getType(), timeIndicator.stopTimer());
                    updateStats();
                }
                gameManager.setGameStatus(GameStatus.WON);
                diffMenu.setEnabled(true);
            }
        }
    }

    //Creates the banner on the top that stores the timeIndicator, statusIndicator and bombIndicator and handles the way the components are displayed
    private void createBanner()
    {
        banner = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        Font font = new Font("Verdana", Font.BOLD, 16);
        
        statusIndicator = new JButton(SMILEY.getIcon());
        statusIndicator.setPreferredSize(new java.awt.Dimension(60, 37));
        statusIndicator.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.BLACK));
        //If statusIndicator is pressed, restart the game
        statusIndicator.addMouseListener(new MouseAdapter() {  
            @Override
            public void mousePressed(MouseEvent e) 
            {
                if (e.getButton() == MouseEvent.BUTTON1 && !gameManager.isGameWaiting())
                    restart();
            }
        });
      
        timeIndicator = new TimeIndicator(font);
        javax.swing.ToolTipManager.sharedInstance().setInitialDelay(250);   //Decrease the initial delay for showing the tooltip and increase the dismiss delay (from 750 to 250 and 4000 to 6000ms respectively)
        javax.swing.ToolTipManager.sharedInstance().setDismissDelay(6000);
        
        bombIndicator = new JLabel(gameManager.getDifficulty().bombCountToString(), SwingConstants.CENTER);
        bombIndicator.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 0, Color.BLACK));
        bombIndicator.setBackground(Color.WHITE);
        bombIndicator.setForeground(Color.RED);
        bombIndicator.setOpaque(true);
        bombIndicator.setPreferredSize(new java.awt.Dimension(70, 20));
        bombIndicator.setFont(font);
        
        c.insets = new Insets(10,0,10,0); //1 high external top and bottom padding for all the components
        c.ipady = 20;                    //2 high extra internal padding for this component
        c.anchor = GridBagConstraints.LINE_START;
        banner.add(timeIndicator, c);
        
        c.weightx = 1;     //allocate more space for the status button
        c.anchor = GridBagConstraints.CENTER;
        banner.add(statusIndicator,c);
        
        c.weightx = 0;  //reset
        c.anchor = GridBagConstraints.LINE_END;
        banner.add(bombIndicator, c);
        banner.setBorder(BorderFactory.createMatteBorder(3, 3, 0, 3, Color.BLACK));
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
        about.addActionListener(ae -> JOptionPane.showMessageDialog(Minesweeper.this, readFile("/About.mine"), "About", JOptionPane.PLAIN_MESSAGE, ICON.getIcon()));    //Clicking on "About" loads information from a txt file
        
        //Stats Menu and Listener for MessageDialog
        JMenu statsMenu = new JMenu("Stats");
        menuBar.add(statsMenu);
        statsMenu.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) 
            {
                javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();
                JTextArea bTab = new JTextArea("Games Played: " + stats[0] +"\nGames Won: " + stats[1] + "\nWin Percentage: " + stats[2] + "%\nBest Time: " + String.format("%02d", (stats[3]) / 60) + ":" + String.format("%02d", (stats[3]) % 60));
                JTextArea iTab = new JTextArea("Games Played: " + stats[4] +"\nGames Won: " + stats[5] + "\nWin Percentage: " + stats[6] + "%\nBest Time: " + String.format("%02d", (stats[7]) / 60) + ":" + String.format("%02d", (stats[7]) % 60));
                JTextArea eTab = new JTextArea("Games Played: " + stats[8] +"\nGames Won: " + stats[9] + "\nWin Percentage: " + stats[10] + "%\nBest Time: " + String.format("%02d", (stats[11]) / 60) + ":" + String.format("%02d", (stats[11]) % 60));
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
                optionPane.addPropertyChangeListener(e1 -> {
                    if (JOptionPane.VALUE_PROPERTY.equals(e1.getPropertyName()))
                    {
                        if (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE)  //Wait until the user clicks on a button
                            return;

                        if (optionPane.getValue().equals(options[1]))   //If the user clicked on reset, reset the tab showing and update the Stats File with the correct info
                        {
                            if (bTab.isShowing())
                            {
                                resetStats(0, 3);
                                bTab.setText("Games Played: " + stats[0] +"\nGames Won: " + stats[1] + "\nWin Percentage: " + stats[2] + "%\nBest Time: " + String.format("%02d", (stats[3]) / 60) + ":" + String.format("%02d", (stats[3]) % 60));
                            }
                            else if (iTab.isShowing())
                            {
                                resetStats(4, 7);
                                iTab.setText("Games Played: " + stats[4] +"\nGames Won: " + stats[5] + "\nWin Percentage: " + stats[6] + "%\nBest Time: " + String.format("%02d", (stats[7]) / 60) + ":" + String.format("%02d", (stats[7]) % 60));
                            }
                            else
                            {
                                resetStats(8, 11);
                                eTab.setText("Games Played: " + stats[8] +"\nGames Won: " + stats[9] + "\nWin Percentage: " + stats[10] + "%\nBest Time: " + String.format("%02d", (stats[11]) / 60) + ":" + String.format("%02d", (stats[11]) % 60));
                            }
                            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                        }
                        else
                            dialog.dispose();
                    }
                });  //Listener for the MessageDialog's Buttons
                dialog.pack();
                dialog.setIconImage(ICON.getImage());
                dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setLocationRelativeTo(Minesweeper.this);
                dialog.setVisible(true);    
            }
        });  //Listener for the Stats Menu that brings up the Stats Message Dialog
        
        setJMenuBar(menuBar);
    }
    
    //Updates the stats array after a game has ended
    private void updateStats(Boolean won, DifficultyPreset difficulty, Duration timeTaken) {
        switch (difficulty) {
            case BEGINNER -> {
                stats[0]++;
                if (won) {
                    stats[1]++;
                    if (timeTaken.getSeconds() < stats[3] || stats[3] == 0)
                        stats[3] = timeTaken.getSeconds();
                }
                if (stats[1] != 0 && stats[0] != 0)
                    stats[2] = (long) (((double) stats[1] / stats[0]) * 100);
            }
            case INTERMEDIATE -> {
                stats[4]++;
                if (won) {
                    stats[5]++;
                    if (timeTaken.getSeconds() < stats[7] || stats[7] == 0)
                        stats[7] = timeTaken.getSeconds();
                }
                if (stats[5] != 0 && stats[4] != 0)
                    stats[6] = (long) (((double) stats[5] / stats[4]) * 100);
            }
            case EXPERT -> {
                stats[8]++;
                if (won) {
                    stats[9]++;
                    if (timeTaken.getSeconds() < stats[11] || stats[11] == 0)
                        stats[11] = timeTaken.getSeconds();
                }
                if (stats[8] != 0 && stats[9] != 0)
                    stats[10] = (long) (((double) stats[9] / stats[8]) * 100);
            }
        }
    }
    
    //Updates/Creates the Stats file with the values from the stats array
    private void updateStats()
    {
        try
        {
            List<String> lines = Arrays.asList(Long.toString(stats[0]), Long.toString(stats[1]), Long.toString(stats[2]), Long.toString(stats[3]),
                                               Long.toString(stats[4]), Long.toString(stats[5]), Long.toString(stats[6]), Long.toString(stats[7]),
                                               Long.toString(stats[8]), Long.toString(stats[9]), Long.toString(stats[10]), Long.toString(stats[11]));
            Path file = Paths.get("Stats.mine");
            Files.write(file, lines, StandardCharsets.UTF_8);
        }
        catch(IOException ignored) {}
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
    
    //Reads the file on the path specified and either updates the stats array or returns a string to be used in the About MessageDialog
    private String readFile(String path)
    {
        if (path.equals("Stats.mine"))  //If the path specified is the Stats file path, read the file line by line and update the stats array
        {
            try (BufferedReader br1 = new BufferedReader(new FileReader(path))) 
            {
                String line = br1.readLine();
                int i = 0;
                while (line != null && i < 12) //If there are more elements than there should be, ignore them
                {
                    stats[i] = Integer.parseInt(line);
                    line = br1.readLine();
                    i++;
                }
                if (i < 11) //If there are less elements than there should be, reset the stats and rewrite the tampered file
                    resetStats(0, 11);
            }
            catch(IOException | NumberFormatException e1){resetStats(0, 11);}  //If there is a problem with the file, reset the array and overwrite/reset the file
            return null;
        }   
        else    //If the path specified is the About file path, read the whole file and return it as a string
            try(BufferedReader br2 = new BufferedReader(new java.io.InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(path)))))
            {
                StringBuilder sb = new StringBuilder();
                String line = br2.readLine();
                while (line != null) 
                {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br2.readLine();
                }
                return sb.toString();
            } catch(IOException e2){return "File Not Found";}
    }
    
    //Reset the Stats array from the start index to the end index specified and then update the stats file
    private void resetStats(int s, int e)
    {
        for (int i = s; i < e + 1; i++)
            stats[i] = 0;
        updateStats();
    }
    
    //Reveal all the bombs and stop the game if a bomb is revealed
    private void gameOver(MinesweeperButton selected)
    {
        selected.setIcon(BOMB_EXPLODED.getIcon());
        for (int j : bombLoc) cells[j].reveal();
        statusIndicator.setIcon(LOSE.getIcon());
        if (gameManager.getDifficulty() != DifficultyPreset.CUSTOM)
        {
            updateStats(false, gameManager.getDifficulty().getType(), timeIndicator.stopTimer());
            updateStats();
        }
        gameManager.setGameStatus(GameStatus.LOST);
        diffMenu.setEnabled(true);
    }

    private void checkFlags(MinesweeperButton selected)
    {
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
    
    //Reset all the game variables, cells and flags; place new random bombs and await player input to reconfigure the cells
    private void restart()
    {
        //If the user requested a change in difficulty, get rid of the current panel and each components and recreate the grid with the correct values
        if (gameManager.hasPresetChanged())
        {
            Container c = cellPanel.getParent();
            c.remove(cellPanel);

            createGrid();
            c.add(cellPanel);

            //revalidate();
            repaint();
            pack();
            
            //Recenter the window
            setLocationRelativeTo(null);
        }
        else  //If the difficulty is still the same, just reset the values of every cell's properties
            for (MinesweeperButton cell : cells) cell.reset();

        gameManager.reset();
        timeIndicator.reset();
        bombIndicator.setForeground(Color.RED);
        bombIndicator.setText(gameManager.getDifficulty().bombCountToString());
        statusIndicator.setIcon(SMILEY.getIcon());
        diffMenu.setEnabled(true);
        gameManager.setPresetChanged(false);
    }
}