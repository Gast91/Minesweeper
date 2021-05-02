package minesweeper.statusbar;

import java.awt.Color;

public enum GameStatus {
    WAITING("Minesweeper Game", Color.black),
    RUNNING("Game Running", Color.black),
    WON("Game Won", new Color(0,153,0)),
    LOST("Game Lost", Color.red);

    private final String text;
    private final Color textColor;

    GameStatus(String text, Color textColor) {
        this.text = text;
        this.textColor = textColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    @Override
    public String toString() {
        return text;
    }
}
