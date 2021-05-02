package minesweeper.game.cells;

import java.awt.Color;

public enum CellValue {

    EMPTY("", new Color(222, 219, 219)),
    ONE("1", Color.blue),
    TWO("2",  new Color(0, 153, 0)),
    THREE("3", Color.red),
    FOUR("4", new Color(77, 176, 230)),
    FIVE("5", new Color(153, 0, 0)),
    SIX("6", Color.cyan),
    SEVEN("7", Color.black),
    EIGHT("8", Color.gray),
    BOMB("X", null);

    private final String text;
    private final Color textColor;

    private static final CellValue[] cellValues = values();

    CellValue(String text, Color textColor) {
        this.text = text;
        this.textColor = textColor;
    }

    public String getText() {
        return text;
    }

    public int asInt() {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    public Color getTextColor() {
        return textColor;
    }

    public CellValue increment() {
        return cellValues[(this.ordinal()+1) % cellValues.length];
    }
}
