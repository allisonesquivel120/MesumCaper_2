package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;

public class MuseumCaperChooseDirectionAction extends GameAction
{
    private int direction;

    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperChooseDirectionAction(GamePlayer player) {
        super(player);
        this.direction = direction;
    }

    public int getDirection()
    {
        return direction;
    }
}
