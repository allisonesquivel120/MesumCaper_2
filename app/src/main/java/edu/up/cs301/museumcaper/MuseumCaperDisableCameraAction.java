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
public class MuseumCaperDisableCameraAction extends GameAction
{
    private int roomId;
    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperDisableCameraAction(GamePlayer player, int roomId) {
        super(player);
        this.roomId = roomId;
    }

    public int getRoomId()
    {
        return roomId;
    }
}
