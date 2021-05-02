package minesweeper.banner;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.event.MouseListener;

import static minesweeper.utility.Icon.SMILEY;

public class StatusIndicator extends JButton {

    public StatusIndicator(MouseListener onStatusIndicatorClick) {
        super(SMILEY.getIcon());
        setPreferredSize(new java.awt.Dimension(60, 37));
        setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.BLACK));
        setToolTipText("Restart");
        // Restart the game on status indicator click
        addMouseListener(onStatusIndicatorClick);
    }

    public void reset()
    {
        setIcon(SMILEY.getIcon());
    }
}
