package minesweeper.banner;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;

public class TimeIndicator extends JLabel {

    private final Timer timer;
    private Duration time = Duration.ZERO;

    public TimeIndicator(Font font) {
        super("0", SwingConstants.CENTER);
        setBorder(BorderFactory.createMatteBorder(3, 0, 3, 3, Color.BLACK));
        setBackground(Color.WHITE);
        setOpaque(true);
        setPreferredSize(new java.awt.Dimension(70, 20));
        setFont(font);
        // On mouse over, update the label's tooltip text with the current time formatted as MM:SS
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Display tooltip only if a minute has passed
                if (time.getSeconds() > 60)
                    setToolTipText(String.format("%02d:%02d", time.toMinutesPart(), time.toSecondsPart()));
            }
        });
        timer = new Timer(1000, ae -> {
            time = time.plusSeconds(1L);
            setText(Long.toString(time.getSeconds()));
        });
    }

    public Duration stopTimer() {
        timer.stop();
        return time;
    }

    public Duration getTime() {
        return time;
    }

    public void startTimer() {
        timer.start();
    }

    public void reset() {
        stopTimer();
        time = Duration.ZERO;
        setText("0");
        setToolTipText(null);
    }
}
