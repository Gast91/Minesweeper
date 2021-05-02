package minesweeper.game.cells;

import javax.swing.JButton;

import static minesweeper.utility.Icon.FLAG;

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