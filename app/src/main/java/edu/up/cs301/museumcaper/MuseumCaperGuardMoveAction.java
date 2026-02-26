package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;

public class MuseumCaperGuardMoveAction extends GameAction
{
    private int targetRoomId;
    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperGuardMoveAction(GamePlayer player, int targetRoomId) {
        super(player);
        this.targetRoomId = targetRoomId;
    }

    public int getTargetRoomId()
    {
        return targetRoomId;
    }
}
