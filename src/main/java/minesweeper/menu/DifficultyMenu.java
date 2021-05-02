package minesweeper.menu;

import minesweeper.difficulty.DifficultyPreset;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import java.awt.Container;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class DifficultyMenu extends JMenu {

    private final ButtonGroup difficultyRadioButtonGroup;

    public DifficultyMenu(Container parentFrame, DifficultyPreset preset, Consumer<ItemEvent> onDifficultyChange, Runnable onValidCustomDifficultySelection) {
        super("Difficulty");

        add(new JRadioButtonMenuItem("Beginner"));
        add(new JRadioButtonMenuItem("Intermediate"));
        add(new JRadioButtonMenuItem("Expert"));

        getItem(preset.toInt()).setSelected(true);

        // Group all radio buttons together to prevent them from being selected at the same time
        difficultyRadioButtonGroup = new ButtonGroup();
        for (int i = 0; i < getItemCount(); i++)
        {
            difficultyRadioButtonGroup.add(getItem(i));
            getItem(i).addItemListener(onDifficultyChange::accept);
            getItem(i).setAccelerator(KeyStroke.getKeyStroke(i + 49, KeyEvent.SHIFT_DOWN_MASK));
        }
        addSeparator();

        add(new CustomDifficultyMenuItem(parentFrame, onValidCustomDifficultySelection));
    }

    public void clearSelection() {
        difficultyRadioButtonGroup.clearSelection();
    }
}
