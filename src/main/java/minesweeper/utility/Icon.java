package minesweeper.utility;

import minesweeper.Minesweeper;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.util.Objects;

public enum Icon {
    // Cell Icons
    FLAG(new ImageIcon(Objects.requireNonNull(Minesweeper.class.getClassLoader().getResource("flag.png")))),
    BOMB(new ImageIcon(Objects.requireNonNull(Minesweeper.class.getClassLoader().getResource("bomb1.png")))),
    BOMB_EXPLODED(new ImageIcon(Objects.requireNonNull(Minesweeper.class.getClassLoader().getResource("bomb2.png")))),
    BOMB_DEFUSED(new ImageIcon(Objects.requireNonNull(Minesweeper.class.getClassLoader().getResource("bomb.png")))),

    // General Icons
    SMILEY(new ImageIcon(Objects.requireNonNull(Minesweeper.class.getClassLoader().getResource("smiley.png")))),
    WIN(new ImageIcon(Objects.requireNonNull(Minesweeper.class.getClassLoader().getResource("win.png")))),
    LOSE(new ImageIcon(Objects.requireNonNull(Minesweeper.class.getClassLoader().getResource("lose.png")))),
    ICON(new ImageIcon(Objects.requireNonNull(Minesweeper.class.getClassLoader().getResource("icon.png"))));

    private final ImageIcon icon;

    Icon(ImageIcon icon) {
        this.icon = icon;
    }

    public ImageIcon getIcon() {
        return icon;
    }

    public Image getImage() {
        return icon.getImage();
    }
}
