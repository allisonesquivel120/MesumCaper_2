package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;
/**
 * Action: guard requests a dice roll for movement
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
public class MuseumCaperRollDiceAction extends GameAction {
    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     * @param type the type of dice rolled
     */
    private final DiceType type; // stores what kind of die to roll [question or movement]
    public MuseumCaperRollDiceAction(GamePlayer player, DiceType type)
    {
        super(player);
        this.type = type;

    }
    public DiceType getType()
    {
        return type;
    }

}
