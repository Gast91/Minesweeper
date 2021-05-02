package minesweeper.difficulty;

import minesweeper.game.Dimensions;

public interface Difficulty {

    int toInt();

    int getRows();

    int getCols();

    Dimensions getDimensions();

    int getBombCount();

    DifficultyPreset getType();
}
