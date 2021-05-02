package minesweeper.game.cells;

import minesweeper.game.Dimensions;
import minesweeper.game.MinesweeperGameManager;

import java.util.Arrays;
import java.util.stream.Stream;

public record Coordinates(int x, int y) {

    private static Dimensions dimensions = MinesweeperGameManager.getInstance().getDifficulty().getDimensions();

    public Coordinates {
        dimensions = MinesweeperGameManager.getInstance().getDifficulty().getDimensions();
    }

    public static Coordinates fromPosition(int position) {
        return new Coordinates(position / dimensions.cols(), position % dimensions.cols());
    }

    public boolean isInsideGrid() {
        return x >= 0
                && y >= 0
                && x <= dimensions.rows() - 1
                && y <= dimensions.cols() - 1;
    }

    public int toPosition() {
        return y + dimensions.cols() * x;
    }

    public Stream<Coordinates> getNeighbors() {
        return Arrays.stream(new Coordinates[]{
                new Coordinates(x - 1, y - 1),
                new Coordinates(x, y - 1),
                new Coordinates(x + 1, y - 1),

                new Coordinates(x - 1, y),
                new Coordinates(x + 1, y),

                new Coordinates(x - 1, y + 1),
                new Coordinates(x, y + 1),
                new Coordinates(x + 1, y + 1)
        });
    }
}
