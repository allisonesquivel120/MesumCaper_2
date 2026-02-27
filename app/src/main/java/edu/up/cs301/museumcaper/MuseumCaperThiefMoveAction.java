package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;
/**
 * Action class for the thief
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
public class MuseumCaperThiefMoveAction extends GameAction
{
    private int targetRoomId;
    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperThiefMoveAction(GamePlayer player, int targetRoomId) {
        super(player);
        this.targetRoomId = targetRoomId;
    }

    public int getTargetRoomId()
    {
        return targetRoomId;
    }
}
