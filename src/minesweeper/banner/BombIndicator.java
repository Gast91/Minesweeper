package minesweeper.banner;

import minesweeper.difficulty.Difficulty;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Font;

public class BombIndicator extends JLabel {

    public BombIndicator(Font font, Difficulty difficultyPreset) {
        super(difficultyPreset.bombCountToString(), SwingConstants.CENTER);
        setBorder(BorderFactory.createMatteBorder(3, 3, 3, 0, Color.BLACK));
        setBackground(Color.WHITE);
        setForeground(Color.RED);
        setOpaque(true);
        setPreferredSize(new java.awt.Dimension(70, 20));
        setFont(font);
    }

    public boolean counterIsNegative() {
        return Integer.parseInt(getText()) < 0;
    }

    public void increment() {
        setText(Integer.toString(Integer.parseInt(getText()) + 1));
    }

    public void decrement() {
        setText(Integer.toString(Integer.parseInt(getText()) - 1));
    }

    public void reset(int newBombCount) {
        setForeground(Color.RED);
        setText(Integer.toString(newBombCount));
    }
}
