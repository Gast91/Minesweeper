package minesweeper.game.cells;

import minesweeper.Minesweeper;
import minesweeper.game.MinesweeperGameManager;

import javax.swing.JButton;

import java.awt.Color;
import java.util.Arrays;
import java.util.stream.Stream;

import static minesweeper.utility.Icon.*;

public class MinesweeperButton extends JButton {
    private boolean revealed = false;
    private boolean highlighted = false;
    private final int position;
    private CellValue value;
    private int[] neighborsPositions;

    public MinesweeperButton(int position, int rows, int cols) {
        setPreferredSize(new java.awt.Dimension(42,40));
        setFocusable(false);
        value = CellValue.EMPTY;
        this.position = position;
        findNeighborPositions(rows, cols);
    }

    public boolean isBomb() {
        return value == CellValue.BOMB;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public boolean isFlagged() {
        return getIcon() == FLAG.getIcon();
    }

    public boolean inInitialState() {
        return !revealed && !isFlagged();
    }

    public boolean isEmpty() {
        return value == CellValue.EMPTY;
    }

    public boolean shouldRevealNeighbors() {
        return isEmpty() && !revealed;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }

    public int getPosition() {
        return position;
    }

    public CellValue getValue() {
        return value;
    }

    public void setValue(CellValue value) {
        this.value = value;
    }

    public void incrementValue() {
        this.value = value.increment();
    }

    public int[] getNeighborsPositions() {
        return neighborsPositions;
    }

    public Stream<MinesweeperButton> getNeighbors() {
        return Arrays.stream(neighborsPositions)
                .filter(pos -> pos != -1)
                .mapToObj(Minesweeper.getInstance()::getCell);
    }

    // Reveal the cell clicked by the user and change the visuals of the cell depending on its value
    public void reveal() {

        setBackground(new Color(222, 219, 219));
        revealed = true;

        if (value == CellValue.BOMB) {
            if (getIcon() == null)  setIcon(BOMB.getIcon());
            else if (isFlagged())   setIcon(BOMB_DEFUSED.getIcon());

        } else if (value == CellValue.EMPTY) {
            setEnabled(false);
            MinesweeperGameManager.getInstance().revealNeighbors(this);

        } else {
            setText(value.getText());
            setForeground(value.getTextColor());
            // Disable the select highlight
            setRolloverEnabled(false);
            // Override the isPressed() method of the default button model to prevent it from ever identifying the button as clicked/mimicking setEnabled()
            setModel(new javax.swing.DefaultButtonModel() {
                @Override
                public boolean isPressed() {
                    return false;
                }
            });
        }
    }

    public void reset() {
        value = CellValue.EMPTY;
        setText("");
        setBackground(null);
        setIcon(null);
        revealed = false;
        setEnabled(true);
        setRolloverEnabled(true);
    }

    /*Neighbors of the cell :
    {Top_Left (-10) , Top_Middle (-9)   , Top_Right(-8),
     Left (-1)      , CELL (0)          , Right(+1),
    Bottom_Left (+8), Bottom_Middle (+9), Bottom_Right(+10)}
    */
    private void findNeighborPositions(int rows, int cols) {
        //TOP_LEFT_CORNER
        if (position == 0)
            neighborsPositions = new int[] {-1            , -1      , -1,
                                            -1            ,           position + 1,
                                            -1            , position + cols, position + (cols + 1)};
            //TOP_RIGHT_CORNER
        else if (position == cols -1)
            neighborsPositions = new int[] {-1             , -1      , -1,
                    position - 1          ,           -1,
                    position + (cols - 1) , position + cols, -1};
            //BOTTOM_LEFT_CORNER
        else if (position == ((rows * cols - 1) - (cols - 1)))
            neighborsPositions = new int[] {-1            , position - cols, position - (cols - 1),
                    -1                      , position + 1,
                    -1            , -1      , -1};
            //BOTTOM_RIGHT_CORNER
        else if ( position == rows * cols - 1)
            neighborsPositions = new int[] {position - (cols + 1), position - cols, -1,
                    position - 1         ,           -1,
                    -1            , -1      , -1};
            //WEST_EDGE
        else if (position % cols == 0 && position != ((rows * cols - 1) - (cols - 1)))
            neighborsPositions = new int[] {-1            , position - cols, position - (cols - 1),
                    -1            ,           position + 1,
                    -1            , position + cols, position + (cols + 1)};
            //NORTH_EDGE
        else if (position > 0 && position < cols -1)
            neighborsPositions = new int[] {-1            , -1      , -1,
                    position - 1         ,           position + 1,
                    position + (cols - 1), position + cols, position + (cols + 1)};
            //EAST_EDGE
        else if ((position + 1) % cols == 0 && position != cols - 1 && position != (rows * cols - 1))
            neighborsPositions = new int[] {position - (cols + 1), position - cols, -1,
                    position - 1         ,           -1,
                    position + (cols - 1), position + cols, -1};
            //SOUTH_EDGE
        else if (position > ((rows * cols - 1) - (cols - 1)) && position < (rows * cols - 1))
            neighborsPositions = new int[] {position - (cols + 1), position - cols, position - (cols - 1),
                    position - 1         ,           position + 1,
                    -1            , -1      , -1};
            //NOT_ON_THE_EDGE
        else
            neighborsPositions = new int[] {position - (cols + 1), position - cols, position - (cols - 1),
                    position - 1 ,                   position + 1,
                    position + (cols - 1), position + cols, position + (cols + 1)};
    }

}