package minesweeper.statusbar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class MinesweeperStatusBar extends JPanel implements PropertyChangeListener {

    private final JLabel gameStatusLabel;
    private final JLabel currentDifficultyLabel;

    public MinesweeperStatusBar(GameStatus initialGameStatus, String initialDifficulty) {
        super();

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(BorderFactory.createMatteBorder(0, 3, 3, 3, Color.BLACK));

        gameStatusLabel = new JLabel(initialGameStatus.toString());
        gameStatusLabel.setBorder(new CompoundBorder(gameStatusLabel.getBorder(), new EmptyBorder(0, 10, 0, 0)));
        add(gameStatusLabel);

        add(Box.createHorizontalGlue());

        currentDifficultyLabel = new JLabel(initialDifficulty);
        currentDifficultyLabel.setBorder(new CompoundBorder(gameStatusLabel.getBorder(), new EmptyBorder(0, 0, 0, 10)));
        add(currentDifficultyLabel);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("gameStatus")) {
            final GameStatus newGameStatus = (GameStatus) evt.getNewValue();
            gameStatusLabel.setText(newGameStatus.toString());
            gameStatusLabel.setForeground(newGameStatus.getTextColor());
        } else if (evt.getPropertyName().equals("difficulty"))
            currentDifficultyLabel.setText(evt.getNewValue().toString());
    }
}
