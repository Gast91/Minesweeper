package minesweeper;

import minesweeper.banner.BombIndicator;
import minesweeper.banner.MinesweeperBanner;
import minesweeper.banner.StatusIndicator;
import minesweeper.banner.StatusIndicatorClickedListener;
import minesweeper.banner.TimeIndicator;
import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.MinesweeperGameManager;
import minesweeper.game.MinesweeperMouseAdapter;
import minesweeper.game.cells.MinesweeperButton;
import minesweeper.menu.DifficultyMenu;
import minesweeper.menu.MinesweeperMenuBar;
import minesweeper.menu.StatsMenu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import static minesweeper.utility.Icon.*;

public class Minesweeper extends JFrame {
    public static MinesweeperBanner banner;
    public static MinesweeperMenuBar menuBar;
    private JPanel cellPanel;
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

        add(cellPanel);
        pack();
        
        setResizable(false);
        setLocationRelativeTo(null); 
    }

    private void createGrid() {
        Difficulty difficulty = gameManager.getDifficulty();

        cellPanel = new JPanel(new GridLayout(difficulty.getRows(), difficulty.getCols()));
        cells = IntStream.range(0, difficulty.getDimensions().toArea())
                .mapToObj(MinesweeperButton::new)
                .peek(cell -> {
                    cell.addMouseListener(new MinesweeperMouseAdapter());
                    cellPanel.add(cell);
                })
                .toArray(MinesweeperButton[]::new);
        cellPanel.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.BLACK));
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