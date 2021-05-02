package minesweeper.banner;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class MinesweeperBanner extends JPanel {

    private TimeIndicator timeIndicator;
    private StatusIndicator statusIndicator;
    private BombIndicator bombIndicator;

    private MinesweeperBanner() {
        super(new GridBagLayout());
    }

    public TimeIndicator getTimeIndicator() {
        return timeIndicator;
    }
    public StatusIndicator getStatusIndicator() {
        return statusIndicator;
    }
    public BombIndicator getBombIndicator() {
        return bombIndicator;
    }

    public void reset(int bombCount) {
        timeIndicator.reset();
        bombIndicator.reset(bombCount);
        statusIndicator.reset();
    }

    public static final class BannerBuilder {

        private StatusIndicator statusIndicator;
        private TimeIndicator timeIndicator;
        private BombIndicator bombIndicator;

        public BannerBuilder withStatusIndicator(StatusIndicator statusIndicator) {
            this.statusIndicator = statusIndicator;
            return this;
        }

        public BannerBuilder withTimeIndicator(TimeIndicator timeIndicator) {
            this.timeIndicator = timeIndicator;
            return this;
        }

        public BannerBuilder withBombIndicator(BombIndicator bombIndicator) {
            this.bombIndicator = bombIndicator;
            return this;
        }

        public MinesweeperBanner build() {
            MinesweeperBanner banner = new MinesweeperBanner();
            banner.timeIndicator = this.timeIndicator;
            banner.bombIndicator = this.bombIndicator;
            banner.statusIndicator = this.statusIndicator;

            // Decrease the initial delay for showing the tooltip and increase the dismiss delay (from 750 to 250 and 4000 to 6000ms respectively)
            javax.swing.ToolTipManager.sharedInstance().setInitialDelay(250);
            javax.swing.ToolTipManager.sharedInstance().setDismissDelay(6000);

            GridBagConstraints c = new GridBagConstraints();

            // 1 high external top and bottom padding for all the components
            c.insets = new Insets(10,0,10,0);

            // 2 high extra internal padding for this component
            c.ipady = 20;

            c.anchor = GridBagConstraints.LINE_START;
            banner.add(timeIndicator, c);

            // allocate more space for the status button
            c.weightx = 1;

            c.anchor = GridBagConstraints.CENTER;
            banner.add(statusIndicator, c);

            // reset
            c.weightx = 0;

            c.anchor = GridBagConstraints.LINE_END;
            banner.add(bombIndicator, c);

            banner.setBorder(BorderFactory.createMatteBorder(3, 3, 0, 3, Color.BLACK));

            return banner;
        }
    }
}
