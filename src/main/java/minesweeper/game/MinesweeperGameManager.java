package minesweeper.game;

import minesweeper.difficulty.Difficulty;
import minesweeper.difficulty.DifficultyPreset;
import minesweeper.game.cells.MinesweeperButton;
import minesweeper.statusbar.GameStatus;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Iterator;

import static minesweeper.utility.Icon.FLAG;

public class MinesweeperGameManager {

    private int bombsFlagged = 0;
    private Difficulty difficulty = DifficultyPreset.EXPERT;
    private int potentialBombsLeft = difficulty.getBombCount();
    private GameStatus gameStatus = GameStatus.WAITING;
    private final ArrayList<MinesweeperButton> emptyCells = new ArrayList<>();
    private boolean revealingNeighbors = false, changedPreset = false;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
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
        return bombsFlagged == difficulty.getBombCount() && potentialBombsLeft == 0;
    }

    public boolean hasPresetChanged() {
        return changedPreset;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty.getType() == DifficultyPreset.CUSTOM || this.difficulty != difficulty)
            notifyDifficultyChanged(difficulty);
        potentialBombsLeft = difficulty.getBombCount();
    }

    public void addPropertyChangeListener(PropertyChangeListener... propertyChangeListener) {
        for (PropertyChangeListener pcl : propertyChangeListener)
            support.addPropertyChangeListener(pcl);
    }

    public void toggleFlag(MinesweeperButton selected) {
        if (selected.isFlagged()) {
            ++potentialBombsLeft;
            selected.setIcon(null);
            if (selected.isBomb()) --bombsFlagged;
        }
        else if (!selected.isRevealed()) {
            --potentialBombsLeft;
            selected.setIcon(FLAG.getIcon());
            if (selected.isBomb()) ++bombsFlagged;

            // If the user has marked all the bombs (and no extra!) they win
            if (hasMarkedAll()) notifyGameStatusChanged(GameStatus.WON);
        }
        support.firePropertyChange("bombMark", null, potentialBombsLeft);
    }

    public void highlightNeighbors(MinesweeperButton selected, boolean state) {
        selected.getNeighbors()
                .filter(MinesweeperButton::inInitialState)
                .forEach(n -> n.setHighlighted(state));
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
                    if (n.isBomb()) notifyGameStatusChanged(GameStatus.LOST, selected);
                    else n.reveal();
                });
    }

    public void checkAndReveal(MinesweeperButton selected) {
        if (gameStatus == GameStatus.WAITING) notifyGameStatusChanged(GameStatus.RUNNING, selected);
        else if (selected.isBomb())           notifyGameStatusChanged(GameStatus.LOST, selected);
        else selected.reveal();
    }

    public void reset() {
        bombsFlagged = 0;
        potentialBombsLeft = difficulty.getBombCount();
        notifyGameStatusChanged(GameStatus.WAITING);
    }

    private void notifyGameStatusChanged(GameStatus gameStatus) {
        support.firePropertyChange("gameStatus", this.gameStatus, gameStatus);
        this.gameStatus = gameStatus;
    }

    private void notifyGameStatusChanged(GameStatus gameStatus, MinesweeperButton selected) {
        support.firePropertyChange("gameStatus", selected, gameStatus);
        this.gameStatus = gameStatus;
    }

    private void notifyDifficultyChanged(Difficulty difficulty) {
        support.firePropertyChange("difficulty", this.difficulty, difficulty);
        this.difficulty = difficulty;
        changedPreset = true;
    }
}
