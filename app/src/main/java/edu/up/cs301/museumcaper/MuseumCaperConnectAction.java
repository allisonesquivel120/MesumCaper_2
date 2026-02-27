package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;
/**
 * General action class for players to connect
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */

public class MuseumCaperConnectAction extends GameAction
{

    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperConnectAction(GamePlayer player) {
        super(player);
    }
}
