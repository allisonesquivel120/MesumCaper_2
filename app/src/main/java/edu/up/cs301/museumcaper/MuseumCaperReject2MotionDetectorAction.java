package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;
/**
 * Action class for the thief, allows thief to reject question for motion detector
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
public class MuseumCaperReject2MotionDetectorAction extends GameAction
{

    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperReject2MotionDetectorAction(GamePlayer player) {
        super(player);
    }
}
