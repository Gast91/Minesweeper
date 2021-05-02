package minesweeper;

import minesweeper.banner.BombIndicator;
import minesweeper.banner.MinesweeperBanner;
import minesweeper.banner.StatusIndicator;
import minesweeper.banner.StatusIndicatorClickedListener;
import minesweeper.banner.TimeIndicator;
import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.MinesweeperGameManager;
import minesweeper.game.cells.CellValue;
import minesweeper.game.cells.MinesweeperButton;
import minesweeper.menu.DifficultyMenu;
import minesweeper.menu.MinesweeperMenuBar;
import minesweeper.menu.StatsMenu;
import minesweeper.stats.GameStats;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import static java.util.function.Predicate.not;
import static minesweeper.utility.Icon.*;

public class Minesweeper extends JFrame {
    private static MinesweeperBanner banner;
    private static MinesweeperMenuBar menuBar;
    private JPanel cellPanel;
    private int[] bombLoc;
    private MinesweeperButton[] cells;

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

        menuBar = new MinesweeperMenuBar.MenuBarBuilder()
                .withDifficultyMenu(new DifficultyMenu(this, DifficultyPreset.EXPERT, onGameDifficultyPresetChange(), this::restart))
                .withStatsMenu(new StatsMenu(this))
                .build(this);
        setJMenuBar(menuBar);

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
            menuBar.setDifficultyMenuEnabled(false);
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
                menuBar.setDifficultyMenuEnabled(true);
                gameManager.setGameStatus(GameStatus.WON);
            }
        }
    }
    
    //Reveal all the bombs and stop the game if a bomb is revealed
    private void gameOver(MinesweeperButton selected) {
        selected.setIcon(BOMB_EXPLODED.getIcon());
        for (int j : bombLoc) cells[j].reveal();
        banner.getStatusIndicator().setIcon(LOSE.getIcon());
        GameStats.getInstance().updateStats(false, gameManager.getDifficulty().getType(), banner.getTimeIndicator().stopTimer());
        menuBar.setDifficultyMenuEnabled(true);
        gameManager.setGameStatus(GameStatus.LOST);
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

        banner.reset(gameManager.getDifficulty().getBombCount());
        gameManager.reset();
        gameManager.setPresetChanged(false);
    }

    private Consumer<ItemEvent> onGameDifficultyPresetChange() {
        return e -> {
            if (e.getStateChange() == ItemEvent.SELECTED)
                gameManager.setDifficulty(DifficultyPreset.fromInt(((JMenuItem)e.getSource()).getParent().getComponentZOrder(((JMenuItem)e.getSource()))));
            restart();
        };
    }

    private StatusIndicatorClickedListener onStatusIndicatorClick() {
        return e -> {
            if (e.getButton() == MouseEvent.BUTTON1 && !gameManager.isGameWaiting())
                restart();
        };
    }
}