package minesweeper;

import minesweeper.banner.BombIndicator;
import minesweeper.banner.MinesweeperBanner;
import minesweeper.banner.StatusIndicator;
import minesweeper.banner.StatusIndicatorClickedListener;
import minesweeper.banner.TimeIndicator;
import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.MinesweeperGameManager;
import minesweeper.game.MinesweeperGrid;
import minesweeper.menu.DifficultyMenu;
import minesweeper.menu.MinesweeperMenuBar;
import minesweeper.menu.StatsMenu;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

import static minesweeper.utility.Icon.*;

public class Minesweeper extends JFrame {
    public static MinesweeperBanner banner;
    public static MinesweeperMenuBar menuBar;
    public static MinesweeperGrid gameGrid;
    private static final MinesweeperGameManager gameManager = MinesweeperGameManager.getInstance();

    public static void main(String[] args) {
        new Minesweeper().setVisible(true);
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

        gameGrid = new MinesweeperGrid(gameManager.getDifficulty());
        add(gameGrid);

        pack();
        
        setResizable(false);
        setLocationRelativeTo(null); 
    }

    private void restart() {
        final Difficulty newDifficulty = gameManager.getDifficulty();
        banner.reset(newDifficulty.getBombCount());
        gameGrid.reset(newDifficulty);
        gameManager.reset();

        if (gameManager.hasPresetChanged()) {
            //revalidate();
            repaint();
            pack();

            // Recenter the window
            setLocationRelativeTo(null);
            gameManager.setPresetChanged(false);
        }
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