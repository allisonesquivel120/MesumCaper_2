package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;

public class MuseumCaperChooseQuestionAction extends GameAction
{
    private int questionIndex;
    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     */
    public MuseumCaperChooseQuestionAction(GamePlayer player, int questionIndex) {
        super(player);
        this.questionIndex = questionIndex;
    }

    public int getQuestionIndex()
    {
        return questionIndex;
    }
}
