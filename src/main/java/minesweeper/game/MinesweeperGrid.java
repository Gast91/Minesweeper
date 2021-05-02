package minesweeper.game;

import minesweeper.difficulty.Difficulty;
import minesweeper.game.cells.MinesweeperButton;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.stream.IntStream;

public class MinesweeperGrid extends JPanel {

    private MinesweeperButton[] cells;
    private Difficulty difficulty;

    public MinesweeperGrid(Difficulty difficulty) {
        this.difficulty = difficulty;
        createGrid();
    }

    public MinesweeperButton getCell(int position)
    {
        return cells[position];
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

    private void createGrid() {
        setLayout(new GridLayout(difficulty.getRows(), difficulty.getCols()));
        cells = IntStream.range(0, difficulty.getDimensions().toArea())
                .mapToObj(MinesweeperButton::new)
                .peek(cell -> {
                    cell.addMouseListener(new MinesweeperMouseAdapter());
                    add(cell);
                })
                .toArray(MinesweeperButton[]::new);
        setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.BLACK));
    }
}
