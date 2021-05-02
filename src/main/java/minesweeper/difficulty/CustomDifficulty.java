package minesweeper.difficulty;

import minesweeper.game.Dimensions;

public class CustomDifficulty implements Difficulty {

    private final Dimensions dimensions;
    private final int bombCount;

    public CustomDifficulty(Dimensions dimensions, int bombs) {
        this.dimensions = dimensions;
        this.bombCount = bombs;
    }

    @Override
    public int toInt() {
        return DifficultyPreset.CUSTOM.toInt();
    }

    @Override
    public int getRows() {
        return dimensions.rows();
    }

    @Override
    public int getCols() {
        return dimensions.cols();
    }

    @Override
    public Dimensions getDimensions() {
        return dimensions;
    }

    @Override
    public int getBombCount() {
        return bombCount;
    }

    @Override
    public DifficultyPreset getType() {
        return DifficultyPreset.CUSTOM;
    }

    @Override
    public String toString() {
        return DifficultyPreset.CUSTOM.toString();
    }
}
