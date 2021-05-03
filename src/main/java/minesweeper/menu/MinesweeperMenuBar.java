package minesweeper.menu;

import minesweeper.Minesweeper;
import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.statusbar.GameStatus;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

import static minesweeper.utility.Icon.ICON;

public class MinesweeperMenuBar extends JMenuBar implements PropertyChangeListener {

    private DifficultyMenu difficultyMenu;
    private StatsMenu statsMenu;

    private static final String ABOUT_MESSAGE = readAboutFile();

    private MinesweeperMenuBar() {
        super();
    }

    private static String readAboutFile() {
        BufferedReader br = new BufferedReader(new InputStreamReader(Objects.requireNonNull(Minesweeper.class.getClassLoader().getResourceAsStream("About.mine"))));
        return br.lines().collect(Collectors.joining(System.getProperty("line.separator")));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("difficulty")) {
            if (((Difficulty) evt.getNewValue()).getType() == DifficultyPreset.CUSTOM)
                difficultyMenu.clearSelection();
        } else if (evt.getPropertyName().equals("gameStatus")) {
            switch ((GameStatus) evt.getNewValue()) {
                case WAITING, WON, LOST -> difficultyMenu.setEnabled(true);
                case RUNNING -> difficultyMenu.setEnabled(false);
            }
        }
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
            about.addActionListener(ae -> JOptionPane.showMessageDialog(menuParent, ABOUT_MESSAGE, "About", JOptionPane.PLAIN_MESSAGE, ICON.getIcon()));
            gameMenu.add(about);

            menuBar.add(gameMenu);
            menuBar.add(statsMenu);

            return menuBar;
        }
    }
}
