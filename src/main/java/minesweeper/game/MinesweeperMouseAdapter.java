package minesweeper.game;

import minesweeper.game.cells.MinesweeperButton;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Predicate;

public class MinesweeperMouseAdapter extends MouseAdapter {

    private MinesweeperButton selected;
    private static final MinesweeperGameManager gameManager = MinesweeperGameManager.getInstance();

    private final Predicate<MouseEvent> cellIsLeftClicked = e -> e.getButton() == MouseEvent.BUTTON1
            && (gameManager.isGameWaiting() || gameManager.isGameRunning())
            && selected.inInitialState();

    private final Predicate<MouseEvent> cellIsMiddleClicked = e -> e.getButton() == MouseEvent.BUTTON2
            && gameManager.isGameRunning()
            && !selected.isFlagged() && selected.isRevealed();

    private final Predicate<MouseEvent> cellIsRightClicked = e -> e.getButton() == MouseEvent.BUTTON3
            && gameManager.isGameRunning()
            && !selected.isRevealed();

    @Override
    public void mousePressed(MouseEvent e) {
        selected = (MinesweeperButton) e.getSource();

        if      (cellIsLeftClicked.test(e))   gameManager.checkAndReveal(selected); // Start game if first reveal, check if bomb and end game or reveal otherwise
        else if (cellIsMiddleClicked.test(e)) gameManager.checkFlags(selected);     // Reveal (if flagged neighbors equal to cell value) or highlight neighbors
        else if (cellIsRightClicked.test(e))  gameManager.toggleFlag(selected);     // Toggle cell flag; user can win if they mark the last bomb through this
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        selected = (MinesweeperButton) e.getSource();

        if (e.getButton() == MouseEvent.BUTTON2) gameManager.highlightNeighbors(selected, false);
    }
}
