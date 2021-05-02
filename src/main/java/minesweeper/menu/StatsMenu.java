package minesweeper.menu;

import minesweeper.difficulty.DifficultyPreset;
import minesweeper.stats.DifficultyStats;
import minesweeper.stats.GameStats;

import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static minesweeper.utility.Icon.ICON;

public class StatsMenu extends JMenu {

    public StatsMenu(Container parentFrame) {
        super("Stats");

        // Listener for the Stats Menu that brings up the Stats Message Dialog
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();

                JTextArea bTab = createStatTab(GameStats.getInstance().getBeginnerStats());
                JTextArea iTab = createStatTab(GameStats.getInstance().getIntermediateStats());
                JTextArea eTab = createStatTab(GameStats.getInstance().getExpertStats());

                tabs.addTab("Beginner", bTab);
                tabs.addTab("Intermediate", iTab);
                tabs.addTab("Expert", eTab);

                // MessageDialog Buttons' Text
                Object[] options = {"Ok", "Reset"};
                final JOptionPane optionPane = new JOptionPane(tabs, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION, null, options);
                final javax.swing.JDialog dialog = new javax.swing.JDialog();
                dialog.setTitle("Stats");
                dialog.setContentPane(optionPane);

                // Listener for the MessageDialog's Buttons
                optionPane.addPropertyChangeListener(e1 -> {
                    if (JOptionPane.VALUE_PROPERTY.equals(e1.getPropertyName())) {
                        // Wait until the user clicks on a button
                        if (optionPane.getValue() == JOptionPane.UNINITIALIZED_VALUE)  return;

                        // Reset the currently showed tab (along with the stats associated with the difficulty the tab represents)
                        if (optionPane.getValue().equals(options[1])) {
                            if      (bTab.isShowing())  resetTab(bTab, DifficultyPreset.BEGINNER);
                            else if (iTab.isShowing())  resetTab(iTab, DifficultyPreset.INTERMEDIATE);
                            else                        resetTab(eTab, DifficultyPreset.EXPERT);

                            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                        }
                        else dialog.dispose();
                    }
                });

                dialog.pack();
                dialog.setIconImage(ICON.getImage());
                dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                dialog.setLocationRelativeTo(parentFrame);
                dialog.setVisible(true);
            }
        });
    }

    private JTextArea createStatTab(DifficultyStats stats) {
        JTextArea statsTab = new JTextArea(stats.toString());
        statsTab.setEditable(false);
        return statsTab;
    }

    private void resetTab(JTextArea statsTab, DifficultyPreset ofDifficulty) {
        GameStats.getInstance().resetStats(ofDifficulty);
        statsTab.setText(GameStats.getInstance().getStats(ofDifficulty).toString());
    }
}
