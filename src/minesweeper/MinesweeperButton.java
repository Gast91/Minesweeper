package minesweeper;

import javax.swing.JButton;

public class MinesweeperButton extends JButton
{
    public boolean bomb = false, revealed = false, highlight = false;

    /*Neighbors of the cell :
    {Top_Left (-10) , Top_Middle (-9)   , Top_Right(-8),
     Left (-1)      , CELL (0)          , Right(+1),
    Bottom_Left (+8), Bottom_Middle (+9), Bottom_Right(+10)}
    */
    public int[] neighbors;

    public int location;
}