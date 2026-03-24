package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;
/**
 * Action class for the guard to move to a specific tile on  board
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
public class MuseumCaperGuardMoveAction extends GameAction
{
    private final int targetRow;
    private final int targetCol;
    private final int guardIndex;
    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     * @param guardIndex
     * @param targetRow
     * @param targetCol
     */
    public MuseumCaperGuardMoveAction(GamePlayer player, int guardIndex, int targetRow, int targetCol) {
        super(player);
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.guardIndex = guardIndex;
    }

    public int getTargetRow()
    {
        return targetRow;
    }
    public int getTargetCol()
    {
        return targetCol;
    }
    public int getGuardIndex()
    {
        return guardIndex;
    }

}
