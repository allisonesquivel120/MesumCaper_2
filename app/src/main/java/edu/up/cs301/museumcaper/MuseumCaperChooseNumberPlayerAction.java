package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;

public class MuseumCaperChooseNumberPlayerAction extends GameAction
{
    private int numPlayers;

    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperChooseNumberPlayerAction(GamePlayer player, int numPlayers) {
        super(player);
        this.numPlayers = numPlayers;
    }

    public int getNumPlayers()
    {
        return numPlayers;
    }
}
