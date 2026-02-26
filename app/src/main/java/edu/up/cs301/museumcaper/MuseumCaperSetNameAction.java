package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;

public class MuseumCaperSetNameAction extends GameAction
{
    private String name;
    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperSetNameAction(GamePlayer player, String name) {
        super(player);
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
