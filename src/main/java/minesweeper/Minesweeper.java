package minesweeper;

import minesweeper.banner.BombIndicator;
import minesweeper.banner.MinesweeperBanner;
import minesweeper.banner.StatusIndicator;
import minesweeper.banner.StatusIndicatorClickedListener;
import minesweeper.banner.TimeIndicator;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.MinesweeperGameManager;
import minesweeper.game.MinesweeperGrid;
import minesweeper.menu.DifficultyMenu;
import minesweeper.menu.MinesweeperMenuBar;
import minesweeper.menu.StatsMenu;
import minesweeper.stats.GameStats;
import minesweeper.statusbar.GameStatus;
import minesweeper.statusbar.MinesweeperStatusBar;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

import static minesweeper.utility.Icon.ICON;

public class Minesweeper extends JFrame implements PropertyChangeListener {
    private static MinesweeperBanner banner;
    private static MinesweeperGrid gameGrid;
    private static final MinesweeperGameManager gameManager = MinesweeperGameManager.getInstance();

    public static void main(String[] args) {
        new Minesweeper().setVisible(true);
    }

    public Minesweeper() {
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Minesweeper");
        setIconImage((ICON.getImage()));

        MinesweeperMenuBar menuBar = new MinesweeperMenuBar.MenuBarBuilder()
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

        gameGrid = new MinesweeperGrid(gameManager.getDifficulty());
        add(gameGrid);

        MinesweeperStatusBar statusBar = new MinesweeperStatusBar(GameStatus.WAITING, gameManager.getDifficulty().toString());
        add(statusBar, BorderLayout.SOUTH);

        gameManager.addPropertyChangeListener(this, menuBar, banner, gameGrid, statusBar);

        pack();
        
        setResizable(false);
        setLocationRelativeTo(null); 
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("difficulty") || evt.getPropertyName().equals("bombMark")) return;
        switch ((GameStatus) evt.getNewValue()) {
            case WAITING -> banner.reset(gameManager.getDifficulty().getBombCount());
            case WON  -> GameStats.getInstance().updateStats(true, gameManager.getDifficulty().getType(), banner.getTime());
            case LOST -> GameStats.getInstance().updateStats(false, gameManager.getDifficulty().getType(), banner.getTime());
        }
    }

    private void restart() {
        gameGrid.reset(gameManager.getDifficulty());
        if (gameManager.hasPresetChanged()) {
            repaint();
            pack();

            setLocationRelativeTo(null);
        }
        gameManager.reset();
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