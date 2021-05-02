package minesweeper.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static minesweeper.utility.Icon.ICON;

public class MinesweeperMenuBar extends JMenuBar {

    private DifficultyMenu difficultyMenu;
    private StatsMenu statsMenu;

    private static final Path ABOUT_PATH = Path.of("src/main/resources/About.mine");

    private MinesweeperMenuBar() {
        super();
    }

    private static String readAboutFile() {
        try {
            return Files.readString(ABOUT_PATH);
        } catch (IOException ignored) {
            return "File not found";
        }
    }

    public void clearDifficultyMenuSelection() {
        difficultyMenu.clearSelection();
    }

    public void setDifficultyMenuEnabled(boolean enabled) {
        difficultyMenu.setEnabled(enabled);
    }

    public static final class MenuBarBuilder {

        private DifficultyMenu difficultyMenu;
        private StatsMenu statsMenu;

        public MenuBarBuilder withDifficultyMenu(DifficultyMenu difficultyMenu) {
            this.difficultyMenu = difficultyMenu;
            return this;
        }

        public MenuBarBuilder withStatsMenu(StatsMenu statsMenu) {
            this.statsMenu = statsMenu;
            return this;
        }

        public MinesweeperMenuBar build(Component menuParent) {
            MinesweeperMenuBar menuBar = new MinesweeperMenuBar();
            menuBar.difficultyMenu = this.difficultyMenu;
            menuBar.statsMenu = this.statsMenu;

            JMenu gameMenu = new JMenu("Game");
            gameMenu.add(difficultyMenu);

            // Setup the about menu
            JMenuItem about = new JMenuItem("About");
            about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
            // Clicking on "About" loads information from a txt file
            about.addActionListener(ae -> JOptionPane.showMessageDialog(menuParent, readAboutFile(), "About", JOptionPane.PLAIN_MESSAGE, ICON.getIcon()));
            gameMenu.add(about);

            menuBar.add(gameMenu);
            menuBar.add(statsMenu);

            return menuBar;
        }
    }
}
