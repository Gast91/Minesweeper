package minesweeper.game;

import minesweeper.Minesweeper;
import minesweeper.banner.BombIndicator;
import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.cells.CellValue;
import minesweeper.game.cells.MinesweeperButton;
import minesweeper.stats.GameStats;
import minesweeper.GameStatus;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import static java.util.function.Predicate.not;
import static minesweeper.utility.Icon.*;

public class MinesweeperGameManager {

    private int bombsFlagged = 0;
    private Difficulty difficulty = DifficultyPreset.EXPERT;
    private GameStatus gameStatus = GameStatus.WAITING;
    private int[] bombLoc;
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
            Minesweeper.menuBar.clearDifficultyMenuSelection();
            this.difficulty = difficulty;
            changedPreset = true;
        } else if (this.difficulty != difficulty) {
            this.difficulty = difficulty;
            changedPreset = true;
        }

    }

    public void toggleFlag(MinesweeperButton selected) {
        BombIndicator bi = Minesweeper.banner.getBombIndicator();
        if (selected.isFlagged()) {
            bi.increment();
            selected.setIcon(null);
            if (selected.isBomb()) --bombsFlagged;
        }
        else if (!selected.isRevealed()) {
            bi.decrement();
            selected.setIcon(FLAG.getIcon());
            if (selected.isBomb()) ++bombsFlagged;

            // If the user has marked all the bombs (and no extra!) they win
            if (hasMarkedAll() && !bi.counterIsNegative()) {
                bi.setForeground(new Color(0,153,0));
                Minesweeper.banner.getStatusIndicator().setIcon(WIN.getIcon());
                GameStats.getInstance().updateStats(true, difficulty.getType(), Minesweeper.banner.getTimeIndicator().stopTimer());
                Minesweeper.menuBar.setDifficultyMenuEnabled(true);
                gameStatus = GameStatus.WON;
            }
        }
    }

    public void highlightNeighbors(MinesweeperButton selected, boolean state) {
        selected.setHighlighted(state);

        selected.getNeighbors()
                .filter(MinesweeperButton::inInitialState)
                .forEach(n -> n.getModel().setRollover(state));
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

    public void checkFlags(MinesweeperButton selected) {
        long neighborsFlagged = selected.getNeighbors()
                .filter(MinesweeperButton::isFlagged)
                .count();

        if (neighborsFlagged < selected.getValue().asInt()) {
            highlightNeighbors(selected, true);
            return;
        }

        selected.getNeighbors()
                .filter(MinesweeperButton::inInitialState)
                .forEach(n -> {
                    if (n.isBomb()) gameOver(n);
                    else n.reveal();
                });
    }

    public void checkAndReveal(MinesweeperButton selected) {
        if (gameStatus == GameStatus.WAITING) {
            Minesweeper.menuBar.setDifficultyMenuEnabled(false);
            generateBombs(selected);
            selected.reveal();
            Minesweeper.banner.getTimeIndicator().startTimer();
            gameStatus = GameStatus.RUNNING;
        } else if (selected.isBomb()) gameOver(selected);
        else selected.reveal();
    }

    public void reset() {
        gameStatus = GameStatus.WAITING;
        bombsFlagged = 0;

        Minesweeper.menuBar.setDifficultyMenuEnabled(true);
    }

    private void generateBombs(MinesweeperButton selected) {
        bombLoc = new Random()
                .ints(0, difficulty.getDimensions().toArea())
                .filter(loc -> isValidBombLocation(selected, loc))
                .distinct()
                .limit(difficulty.getBombCount())
                .toArray();

        configureCells();
    }

    // Checks if the number generated is a valid location for a bomb
    private boolean isValidBombLocation(MinesweeperButton selected, int r)  {
        // First selection cannot be a bomb
        if (selected.getPosition() == r)  return false;

        // First selection neighbors must not be bombs to ensure a better start
        for (int n : selected.getNeighborsPositions())
            if (n == r) return false;

        return true;
    }

    // Update each cell's value depending on how many adjacent bombs there are
    private void configureCells() {
        Arrays.stream(bombLoc)
                .forEach( bl -> {
                    MinesweeperButton currentCell = Minesweeper.gameGrid.getCell(bl);
                    currentCell.setValue(CellValue.BOMB);
                    currentCell.getNeighbors()
                            .filter(not(MinesweeperButton::isBomb))
                            .forEach(MinesweeperButton::incrementValue);
                });
    }

    // Reveal all the bombs and stop the game if a bomb is revealed
    private void gameOver(MinesweeperButton selected) {
        selected.setIcon(BOMB_EXPLODED.getIcon());
        for (int bl : bombLoc) Minesweeper.gameGrid.getCell(bl).reveal();
        Minesweeper.banner.getStatusIndicator().setIcon(LOSE.getIcon());
        GameStats.getInstance().updateStats(false, difficulty.getType(), Minesweeper.banner.getTimeIndicator().stopTimer());
        Minesweeper.menuBar.setDifficultyMenuEnabled(true);
        gameStatus = GameStatus.LOST;
    }
}
