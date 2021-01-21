/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package minesweeper;

/**
 *
 * @author James
 */

public class MinesweeperButton extends javax.swing.JButton
{
    public boolean bomb = false, revealed = false, highlight = false;
    
    /*Neighbors of the cell :
    {Top_Left (-10) , Top_Middle (-9)   , Top_Right(-8), 
     Left (-1)      , CELL (0)          , Right(+1),
    Bottom_Left (+8), Bottom_Middle (+9), Bottom_Right(+10)}
    */
    public int location, neighbors[];
}