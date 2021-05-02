package minesweeper;

import minesweeper.difficulty.CustomDifficulty;
import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.Dimensions;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
import javax.swing.Timer;

import static minesweeper.utility.Icon.*;

public class Minesweeper extends JFrame {
    private JPanel cellPanel, banner;
    private JButton statusIndicator;
    private JLabel timeIndicator, bombIndicator;
    private JMenu diffMenu;
    private int bombs, bombsMarked = 0, seconds = 0;
    private Difficulty difficulty = DifficultyPreset.EXPERT;
    private int[] bombLoc;                                                         //ARRAY FOR THE LOCATION OF EACH BOMB
    private MinesweeperButton[] cells;                                            //ARRAY THAT STORES ALL THE CELLS
    private boolean running = false, gameEnded = false, changedPreset = false;
    private final int[] stats = new int[] {0, 0, 0 ,0 ,0 ,0 , 0, 0, 0, 0, 0, 0};//ARRAY THAT STORES IN MEMORY THE USER'S STATS TAKEN FROM A FILE (ORDER: BEG/INTER/EXP - GAMES PLAYED, WON, %, BEST TIME)
    ArrayList<MinesweeperButton> emptyCells = new ArrayList<>();
    Timer timer = new Timer(1000, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ae) 
        {
            timeIndicator.setText(Integer.toString(seconds++));
        }
    });        //TIMER FOR SECONDS ELAPSED - MOUSING OVER TIMEINDICATOR DISPLAYS TIME AS MM:SS
     
    public static void main(String[] args)
    {
        new Minesweeper().setVisible(true);
    }
    
    //Constructor
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
        bombs = difficulty.getBombCount();

        cells = new MinesweeperButton[difficulty.getDimensions().toArea()];
        //Panel that stores all the cells
        cellPanel = new JPanel(new GridLayout(difficulty.getRows(), difficulty.getCols()));
        for (int i = 0; i < difficulty.getDimensions().toArea(); i++)
        {   
            cells[i] = new MinesweeperButton(i, difficulty.getRows(), difficulty.getCols());
            cells[i].addMouseListener(new MouseAdapter() {  
            @Override
            public void mousePressed(MouseEvent e) {
                //RIGHT_MOUSE_PRESS -- WIN CONDITIONS HERE
                if (e.getButton() == MouseEvent.BUTTON3 && !(((MinesweeperButton)e.getSource()).isRevealed() | gameEnded | bombs != 0))
                {
                    //If the icon of the cell clicked by the user is a flag, remove it.Else set it as flag
                    if (((MinesweeperButton)e.getSource()).isFlagged())
                    {
                        bombIndicator.setText(Integer.toString(Integer.parseInt(bombIndicator.getText()) + 1));  //Update the indicator with the number of potential bombs left
                        ((MinesweeperButton)e.getSource()).setIcon(null);
                        if (((MinesweeperButton)e.getSource()).isBomb())
                            bombsMarked--;
                    }
                    else if (((MinesweeperButton)e.getSource()).getText().equals(""))
                    {
                        bombIndicator.setText(Integer.toString(Integer.parseInt(bombIndicator.getText()) - 1)); //Update the indicator with the number of potential bombs left
                        ((MinesweeperButton)e.getSource()).setIcon(FLAG.getIcon());
                        if (((MinesweeperButton)e.getSource()).isBomb())
                            bombsMarked++;
                        //If the user has marked all the bombs, he wins
                        if (bombsMarked == bombLoc.length && Integer.parseInt(bombIndicator.getText()) == 0)
                        {
                            bombIndicator.setForeground(new Color(0,153,0));
                            statusIndicator.setIcon(WIN.getIcon());
                            if (difficulty != DifficultyPreset.CUSTOM)
                            {
                                updateStats(true);
                                updateStats();
                            }
                            gameEnded = true;
                            diffMenu.setEnabled(true);
                            timer.stop();
                        }
                    }
                }
                //LEFT_MOUSE_PRESS -- LOSE CONDITIONS HERE
                if (e.getButton() == MouseEvent.BUTTON1 && !(((MinesweeperButton)e.getSource()).isRevealed() | gameEnded | ((MinesweeperButton)e.getSource()).isFlagged()))
                {
                    //If the bombs arent assigned yet, it means that this is the first click of the game
                    //So generate the bombs and configure the cells. Then reveal the cell clicked and any additional ones as needed
                    if (bombs != 0)
                    {
                        diffMenu.setEnabled(false);
                        placeBombs(e);
                        configCells();
                        revealCell(((MinesweeperButton)e.getSource()).getPosition());
                        timeIndicator.setText(Integer.toString(seconds++));
                        timer.start();
                    }
                    //If button clicked is a bomb, reveal all the bombs and stop the game
                    if (((MinesweeperButton)e.getSource()).isBomb())
                    {
                        ((MinesweeperButton)e.getSource()).setIcon(BOMB_EXPLODED.getIcon());
                        gameOver();
                    }
                    else
                        revealCell(((MinesweeperButton)e.getSource()).getPosition());
                }
                //MIDDLE_MOUSE_PRESS -- Checks whether it should reveal all the cell's neighbors or highlight them
                if (e.getButton() == MouseEvent.BUTTON2 && bombs == 0 && !((MinesweeperButton)e.getSource()).isFlagged() && ((MinesweeperButton)e.getSource()).isRevealed())
                {
                    if (gameEnded)
                        return;
                    checkFlags(e);
                }
            }   //LEFT, RIGHT AND MIDDLE MOUSE BUTTONS LISTENERS
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2 && ((MinesweeperButton)e.getSource()).isHighlighted())
                    highlightNeighbors(e, false);
            }   //If the button released is the mouse wheel and if the cell's neighbors are highlighted, remove that highlight
            }); //MOUSE LISTENER FOR ALL THE CELLS
            cellPanel.add(cells[i]);
            cellPanel.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.BLACK));  //ARGS => TOP, BOTTOM, LEFT, RIGHT
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
                if (e.getButton() == MouseEvent.BUTTON1 && bombs == 0)  //Dont bother restarting if the game hasnt even started yet
                    restart();
            }
        });
      
        timeIndicator = new JLabel(Integer.toString(seconds), SwingConstants.CENTER);
        timeIndicator.setBorder(BorderFactory.createMatteBorder(3, 0, 3, 3, Color.BLACK));
        timeIndicator.setBackground(Color.WHITE);
        timeIndicator.setOpaque(true);
        timeIndicator.setPreferredSize(new java.awt.Dimension(70, 20));
        timeIndicator.setFont(font);
        //When the user mouses over the timeIndicator, update the label's tooltip text with the current time formatted as MM:SS
        timeIndicator.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                int s = (gameEnded) ? seconds : seconds - 1;
                if (seconds >= 60)   //Displays the formatted time only if a minute has elapsed. Format time as MM:SS (minus a second to sync the tooltip with the constantly increasing label value)
                    timeIndicator.setToolTipText(String.format("%02d", (s) / 60) + ":" + String.format("%02d", (s) % 60));
            }
        });
        javax.swing.ToolTipManager.sharedInstance().setInitialDelay(250);   //Decrease the initial delay for showing the tooltip and increase the dismiss delay (from 750 to 250 and 4000 to 6000ms respectively)
        javax.swing.ToolTipManager.sharedInstance().setDismissDelay(6000);
        
        bombIndicator = new JLabel(Integer.toString(bombs - bombsMarked), SwingConstants.CENTER);
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
        diffMenu.getItem(difficulty.toInt()).setSelected(true);
        ButtonGroup rbGroup = new ButtonGroup();  
        for (int i = 0; i < 3; i++)
        {
            rbGroup.add(diffMenu.getItem(i));
            diffMenu.getItem(i).addItemListener(e -> {
                //When a radio button is selected, set the difficulty according to the component order, mark that the preset changed and restart the game with the correct values
                changedPreset = true;
                if (e.getStateChange() == ItemEvent.SELECTED)
                    difficulty = DifficultyPreset.fromInt(((JMenuItem)e.getSource()).getParent().getComponentZOrder(((JMenuItem)e.getSource())));
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
                            difficulty = new CustomDifficulty(
                                    new Dimensions(Integer.parseInt(r.getText()), Integer.parseInt(c.getText())),
                                    Integer.parseInt(b.getText()));
                            rbGroup.clearSelection();
                            changedPreset = true;
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
    private void updateStats(Boolean won)
    {
        seconds--;  //for sync purposes
        switch (difficulty.getType()) {
            case BEGINNER -> {
                stats[0]++;
                if (won) {
                    stats[1]++;
                    if (seconds < stats[3] || stats[3] == 0)
                        stats[3] = seconds;
                }
                if (stats[1] != 0 && stats[0] != 0)
                    stats[2] = (int) (((double) stats[1] / stats[0]) * 100);
            }
            case INTERMEDIATE -> {
                stats[4]++;
                if (won) {
                    stats[5]++;
                    if (seconds < stats[7] || stats[7] == 0)
                        stats[7] = seconds;
                }
                if (stats[5] != 0 && stats[4] != 0)
                    stats[6] = (int) (((double) stats[5] / stats[4]) * 100);
            }
            case EXPERT -> {
                stats[8]++;
                if (won) {
                    stats[9]++;
                    if (seconds < stats[11] || stats[11] == 0)
                        stats[11] = seconds;
                }
                if (stats[8] != 0 && stats[9] != 0)
                    stats[10] = (int) (((double) stats[9] / stats[8]) * 100);
            }
        }
    }
    
    //Updates/Creates the Stats file with the values from the stats array
    private void updateStats()
    {
        try
        {
            List<String> lines = Arrays.asList(Integer.toString(stats[0]), Integer.toString(stats[1]), Integer.toString(stats[2]), Integer.toString(stats[3]),
                                               Integer.toString(stats[4]), Integer.toString(stats[5]), Integer.toString(stats[6]), Integer.toString(stats[7]),
                                               Integer.toString(stats[8]), Integer.toString(stats[9]), Integer.toString(stats[10]), Integer.toString(stats[11]));
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
    private void gameOver()
    {
        timer.stop();
        for (int j : bombLoc) revealCell(cells[j].getPosition());
        statusIndicator.setIcon(LOSE.getIcon());
        if (difficulty != DifficultyPreset.CUSTOM)
        {
            updateStats(false);
            updateStats();
        }
        gameEnded = true;
        diffMenu.setEnabled(true);
    }
    
    //Checks how many flagged neighbors the clicked cell has and if it matches its value, all the valid nearby cells are revealed
    //Can backfire if the user has marked the incorrect cells or more cells than the number indicates. If the user hasnt marked enough cells, this function hightlights all the cells neighbors.
    private void checkFlags(MouseEvent e)
    {
        int flagged = 0; //How many cells the user has marked
        int[] neighborsPositions = ((MinesweeperButton)e.getSource()).getNeighborsPositions();
        for (int i = 0; i < 8; i++)
        {
            if ( neighborsPositions[i] != -1 && cells[neighborsPositions[i]].isFlagged())
                flagged++;
        }
        if (flagged >= ((MinesweeperButton)e.getSource()).getValue().asInt())
        {
            for (int n : neighborsPositions)
            {
                if (n != -1)
                {
                    if (!cells[n].isFlagged() && !cells[n].isRevealed())
                    {
                        revealCell(n);
                        if (cells[n].isBomb())
                        {
                            cells[n].setIcon(BOMB_EXPLODED.getIcon());
                            gameOver();
                        }
                    }
                }
            }
        }
        else
            highlightNeighbors(e, true);
    }
    
    //Toggle the rollover state of the cell's neighbors on or off
    private void highlightNeighbors(MouseEvent e, boolean state)
    {
        ((MinesweeperButton)e.getSource()).setHighlighted(state);
            for (int n : ((MinesweeperButton)e.getSource()).getNeighborsPositions())
            {
                if (n != -1)
                {
                    if (!cells[n].isFlagged() && !cells[n].isRevealed())
                        cells[n].getModel().setRollover(state);
                }
            }   
    }

    //Assigns X random cells with bombs on the first user click (where X = amount of bombs specified by the user)
    private void placeBombs(MouseEvent e)
    {
        bombLoc = new int[bombs];
        while (bombs > 0)
        {
            //Random cell location between 0 and total number of cells
            int r = (int) Math.floor(Math.random() * difficulty.getDimensions().toArea());
            
            //Making sure that the first cell the user clicks on is an empty cell and that its immediate neighboring cells arent bombs
            //If the above condition is satisfied and if the cell does not already contain a bomb, assign one to it
            if (isValid(e, r) && !cells[r].isBomb())
            {
                    cells[r].setValue(CellValue.BOMB);
                    bombLoc[bombs-1] = r;
                    bombs--;
            }
        }
    }
    
    //Checks if the number generated is a valid location for a bomb
    private boolean isValid(MouseEvent e, int r)
    {
        //If the number generated is the location of the cell clicked by the user, return false
        if (((MinesweeperButton)e.getSource()).getPosition() == r)
            return false;
        //Check each neighbor of the cell first clicked by the user; if the number generated isnt one of them return true else return false
        for (int n : ((MinesweeperButton)e.getSource()).getNeighborsPositions())
        {
            if (n == r)
                return false;
        }
        return true;
    }

    // Update each cell's value depending on how many adjacent bombs there are
    private void configCells()
    {
        for (int k : bombLoc) {
            for (int j = 0; j < 8; j++) {
                int[] neighborsPositions = cells[k].getNeighborsPositions();
                if (neighborsPositions[j] != -1 && !cells[neighborsPositions[j]].isBomb())
                    cells[neighborsPositions[j]].incrementValue();
            }
        }
    }

    // Reveal the cell clicked by the user and change the visuals of the cell depending on its value
    private void revealCell(int loc)
    {
        MinesweeperButton cell = cells[loc];
        cell.setBackground(new Color(222, 219, 219));
        cell.setRevealed(true);

        if (cell.isBomb()) {
            if (cell.getIcon() == null) cell.setIcon(BOMB.getIcon());
            else if (cell.isFlagged())  cell.setIcon(BOMB_DEFUSED.getIcon());
        } else if (cell.isEmpty()) {
            cell.setEnabled(false);
            revealNeighbors(loc);
        } else {
            cell.setText(cell.getValue().getText());
            cell.setForeground(cell.getValue().getTextColor());
            // Disable the select highlight
            cell.setRolloverEnabled(false);
            // Override the isPressed() method of the default button model to prevent it from ever identifying the button as clicked/mimicking setEnabled()
            cell.setModel(new javax.swing.DefaultButtonModel(){
                @Override
                public boolean isPressed() {
                    return false;
                }
            });
        }
    }
    
    //When an empty cell is clicked, check its eligible neighbors and whether they are already revealed or not and reveal them
    private void revealNeighbors(int loc) {
        if (running) return;

        //Flag to avoid infinite recursion over the same elements in the list
        running = true;

        int[] neighborsPositions = cells[loc].getNeighborsPositions();
        for (int i = 0; i < 8; i ++)
        {
            if (neighborsPositions[i] != -1)
            {
                if (cells[neighborsPositions[i]].shouldRevealNeighbors())
                    emptyCells.add(cells[neighborsPositions[i]]);
                revealCell(neighborsPositions[i]);
            }
        }
        //When the current empty cell and its immediate neighbors are processed, change the flag to true and move to the next empty cell on the list
        running = false;
        
        Iterator<MinesweeperButton> cell = emptyCells.iterator();
        while (cell.hasNext())
        {
            MinesweeperButton temp = cell.next();
            
            //Remove the current empty cell from the list and reveal itself and its immediate neighbors
            cell.remove();
            revealCell(temp.getPosition());
        }
    }
    
    //Reset all the game variables, cells and flags; place new random bombs and await player input to reconfigure the cells
    private void restart()
    {
        timer.stop();
        //If the user requested a change in difficulty, get rid of the current panel and each components and recreate the grid with the correct values
        if (changedPreset)
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

        gameEnded = false;
        bombsMarked = 0;
        if (!changedPreset) 
            bombs = bombLoc.length; 
        timeIndicator.setText(Integer.toString(seconds = 0));
        timeIndicator.setToolTipText("");
        bombIndicator.setForeground(Color.RED);
        bombIndicator.setText(Integer.toString(bombs - bombsMarked));
        statusIndicator.setIcon(SMILEY.getIcon());
        diffMenu.setEnabled(true);
        changedPreset = false;
    }    
}