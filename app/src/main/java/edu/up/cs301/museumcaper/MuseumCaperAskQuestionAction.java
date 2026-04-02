package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;

/**
 * Action sent by the detective after rolling the question die.
 * Carries the question type so the game state can compute the AI thief's answer.
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 * @version March 2026
 */
public class MuseumCaperAskQuestionAction extends GameAction {

    private final QuestionType questionType;

    /**
     * @param player       the detective player sending this action
     * @param questionType which question is being asked (MOTION, SCAN, or EYE)
     */
    public MuseumCaperAskQuestionAction(GamePlayer player, QuestionType questionType) {
        super(player);
        this.questionType = questionType;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }
}