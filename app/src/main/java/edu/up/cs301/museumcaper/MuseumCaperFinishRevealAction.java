package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;

/**
 * Action sent by the detective after reading the thief's answer.
 * Triggers the thief AI turn.
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 * @version March 2026
 */
public class MuseumCaperFinishRevealAction extends GameAction {
    public MuseumCaperFinishRevealAction(GamePlayer player) {
        super(player);
    }
}