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

public class MuseumCaperMarkStolenPaintingsAction extends GameAction
{
    private final int paintingId;
    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperMarkStolenPaintingsAction(GamePlayer player, int paintingId) {
        super(player);
        this.paintingId = paintingId;
    }
    public int getPaintingId()
    {
        return paintingId;
    }
}
