package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;
/**
 * Action class for the guard, choose between "can i see you" or "which camera sees you"
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */

public class MuseumCaperChooseQuestionAction extends GameAction
{
    private int questionIndex;
    /**
     * constructor for GameAction
     *
     * @param player the player who created the action
     * @param questionIndex
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
