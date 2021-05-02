package minesweeper.game;

import minesweeper.GameStatus;
import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.cells.MinesweeperButton;

import java.util.ArrayList;
import java.util.Iterator;

public class MinesweeperGameManager {

    private int bombsFlagged = 0;
    private Difficulty difficulty = DifficultyPreset.EXPERT;
    private GameStatus gameStatus = GameStatus.WAITING;
    private final ArrayList<MinesweeperButton> emptyCells = new ArrayList<>();
    private boolean revealingNeighbors = false, changedPreset = false;

    private static MinesweeperGameManager instance = null;

    public static MinesweeperGameManager getInstance() {
        if (instance == null)
            instance = new MinesweeperGameManager();

        return instance;
    }

    public boolean isGameWaiting() {
        return gameStatus == GameStatus.WAITING;
    }

    public boolean isGameRunning() {
        return gameStatus == GameStatus.RUNNING;
    }

    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public boolean hasMarkedAll() {
        return bombsFlagged == difficulty.getBombCount();
    }

    public boolean hasPresetChanged() {
        return changedPreset;
    }

    public void setPresetChanged(boolean changedPreset) {
        this.changedPreset = changedPreset;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty.getType() == DifficultyPreset.CUSTOM) {
            this.difficulty = difficulty;
            changedPreset = true;
        } else if (this.difficulty != difficulty) {
            this.difficulty = difficulty;
            changedPreset = true;
        }

    }

    public int getBombsFlagged() {
        return bombsFlagged;
    }

    public void setBombsFlagged(int bombsFlagged) {
        this.bombsFlagged = bombsFlagged;
    }

    // When an empty cell is clicked, check its eligible neighbors and whether they are already revealed or not and reveal them
    public void revealNeighbors(MinesweeperButton cell) {
        if (revealingNeighbors) return;

        // Flag to avoid infinite recursion over the same elements in the list
        revealingNeighbors = true;

        cell.getNeighbors()
                .forEach(n -> {
                    if (n.shouldRevealNeighbors()) emptyCells.add(n);
                    n.reveal();
                });

        // Move to the next empty cell in the list
        revealingNeighbors = false;

        Iterator<MinesweeperButton> it = emptyCells.iterator();
        while (it.hasNext()) {
            MinesweeperButton temp = it.next();
            // Remove the current empty cell from the list and reveal it and its immediate neighbors
            it.remove();
            temp.reveal();
        }
    }

    public void reset() {
        gameStatus = GameStatus.WAITING;
        bombsFlagged = 0;
    }
}
