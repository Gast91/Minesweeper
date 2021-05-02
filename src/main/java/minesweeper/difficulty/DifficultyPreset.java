package minesweeper.difficulty;

import minesweeper.game.Dimensions;

public enum DifficultyPreset implements Difficulty {
    BEGINNER(0, new Dimensions(9, 9), 10),
    INTERMEDIATE(1, new Dimensions(16, 16), 40),
    EXPERT(2, new Dimensions(16, 30), 99),
    CUSTOM(3, null, 0);

    private final int type;
    private final Dimensions dimensions;
    private final int bombCount;

    DifficultyPreset(int type, Dimensions dimensions, int bombs) {
        this.type = type;
        this.dimensions = dimensions;
        this.bombCount = bombs;
    }

    @Override
    public int toInt() {
        return type;
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
        return this;
    }

    public static DifficultyPreset fromInt(int i) {
        DifficultyPreset[] range = DifficultyPreset.values();
        return range[i];
    }
}
