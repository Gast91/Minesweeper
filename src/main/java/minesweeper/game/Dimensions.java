package minesweeper.game;

public record Dimensions(int rows, int cols) {

    public int toArea() {
        return rows * cols;
    }
}
