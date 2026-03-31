package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;

/**
 * Action: human player signals they are done placing
 * paintings and cameras during setup phase.
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version March 2026
 */
public class MuseumCaperFinishSetupAction extends GameAction {
    public MuseumCaperFinishSetupAction(GamePlayer player) {
        super(player);
    }
}