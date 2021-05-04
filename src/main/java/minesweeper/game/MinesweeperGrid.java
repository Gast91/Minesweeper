package minesweeper.game;

import minesweeper.difficulty.Difficulty;
import minesweeper.game.cells.CellValue;
import minesweeper.game.cells.MinesweeperButton;
import minesweeper.statusbar.GameStatus;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;

import static java.util.function.Predicate.not;
import static minesweeper.utility.Icon.BOMB_EXPLODED;

public class MinesweeperGrid extends JPanel implements PropertyChangeListener {

    private MinesweeperButton[] cells;
    private int[] bombLoc;
    private Difficulty difficulty;

    public MinesweeperGrid(Difficulty difficulty) {
        this.difficulty = difficulty;
        createGrid();
        setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.BLACK));
    }

    public void reset(Difficulty difficulty) {
        if (this.difficulty != difficulty) {
            this.difficulty = difficulty;
            removeAll();
            createGrid();
        }
        else
            for (MinesweeperButton cell : cells) cell.reset();
    }

    private final Function<Integer, MinesweeperButton> getCell = position -> cells[position];

    private void createGrid() {
        setLayout(new GridLayout(difficulty.getRows(), difficulty.getCols()));
        cells = IntStream.range(0, difficulty.getDimensions().toArea())
                .mapToObj(MinesweeperButton::new)
                .peek(cell -> {
                    cell.addMouseListener(new MinesweeperMouseAdapter());
                    add(cell);
                })
                .toArray(MinesweeperButton[]::new);
        MinesweeperButton.addNeighborCallback(getCell);
    }

    private void generateBombs(MinesweeperButton selected) {
        bombLoc = new Random()
                .ints(0, difficulty.getDimensions().toArea())
                .filter(loc -> isValidBombLocation(selected, loc))
                .distinct()
                .limit(difficulty.getBombCount())
                .toArray();

        configureCells();
        selected.reveal();
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
                    MinesweeperButton currentCell = cells[bl];
                    currentCell.setValue(CellValue.BOMB);
                    currentCell.getNeighbors()
                            .filter(not(MinesweeperButton::isBomb))
                            .forEach(MinesweeperButton::incrementValue);
                });
    }

    // Reveal all the bombs and stop the game if a bomb is revealed
    private void gameOver(MinesweeperButton selected) {
        selected.setIcon(BOMB_EXPLODED.getIcon());
        for (int bl : bombLoc) cells[bl].reveal();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals("gameStatus")) return;
        switch ((GameStatus) evt.getNewValue()) {
            case RUNNING -> generateBombs((MinesweeperButton) evt.getOldValue());
            case LOST -> gameOver((MinesweeperButton) evt.getOldValue());
        }
    }
}
