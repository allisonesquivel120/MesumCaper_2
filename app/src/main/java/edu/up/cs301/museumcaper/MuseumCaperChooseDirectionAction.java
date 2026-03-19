package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;
/**
 * General action class for player, cna chose which direction to go in
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
public class MuseumCaperChooseDirectionAction extends GameAction
{
    private final int direction;

    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     * @param direction
     */
    public MuseumCaperChooseDirectionAction(GamePlayer player, int direction) {
        super(player);
        this.direction = direction;
    }

    public int getDirection()
    {
        return direction;
    }
}
