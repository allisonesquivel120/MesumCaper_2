package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.infoMessage.GameState;
import edu.up.cs301.GameFramework.players.GamePlayer;
import edu.up.cs301.GameFramework.LocalGame;
import edu.up.cs301.GameFramework.actionMessage.GameAction;

/**
 * A class that represents the state of a game.
 *
 * @author Allison E.
 * @author Jayden H.
 * @author Farid S.
 * @version Feb 2026
 */
public class MuseumCaperLocalGame extends LocalGame {

	// the game's state
	private MuseumCaperState gameState;

	/**
	 * This ctor should be called when a new counter game is started
	 */
	public MuseumCaperLocalGame(GameState state) {
		// initialize the game state, with the counter value starting at 0
		if (! (state instanceof MuseumCaperState)) {
			state = new MuseumCaperState();
		}
		this.gameState = (MuseumCaperState)state;
		super.state = state;
	}
    @Override
    protected boolean canMove(int playerIdx)
    {
        // thief cant move manually
        // careful when implementing AI!
        if(playerIdx == 0)
        {
            return false;
        }
        // only the current players turn can move
        return playerIdx == gameState.getPlayerTurn();
    }

    @Override
    protected boolean makeMove(GameAction action) {

        // ----- GUARD ------

        // general actions

        if (action instanceof MuseumCaperSetNameAction) {
            return gameState.makeSetNameAction((MuseumCaperSetNameAction) action);
        }
        if (action instanceof MuseumCaperConnectAction) {
            return gameState.makeConnectAction((MuseumCaperConnectAction) action);
        }
        // move
        if (action instanceof MuseumCaperGuardMoveAction) {
            boolean success = gameState.makeGuardMoveAction((MuseumCaperGuardMoveAction) action);

            if(success && !gameState.isGameOver())
            {
                gameState.runThiefAI();
            }
            return success;
        }
        // dice
        if (action instanceof MuseumCaperRollDiceAction) {
            return gameState.makeRollDiceAction((MuseumCaperRollDiceAction) action);
        }
        // marking the stolen paintings
        if (action instanceof MuseumCaperMarkStolenPaintingsAction)
        {
            return gameState.makeMarkStolenPaintingsAction((MuseumCaperMarkStolenPaintingsAction) action);
        }
        // choosing question for camera/eye dice
        if(action instanceof MuseumCaperChooseQuestionAction)
        {
            return gameState.makeChooseQuestionAction((MuseumCaperChooseQuestionAction) action);

        }
        if (action instanceof MuseumCaperEndTurnAction)
        {
            boolean success = gameState.makeEndTurnAction((MuseumCaperEndTurnAction)action);
            if(success && !gameState.isGameOver())
            {
                gameState.runThiefAI();
            }
            return success;
        }
        // unknown action
        return false;

    }
	/**
	 * send the updated state to a given player
	 */
	@Override
	protected void sendUpdatedStateTo(GamePlayer p) {
		// this is a perfect-information game, so we'll make a
		// complete copy of the state to send to the player
		p.sendInfo(new MuseumCaperState(this.gameState));

	}//sendUpdatedSate

	/**
	 * Check if the game is over. It is over, return a string that tells
	 * who the winner(s), if any, are. If the game is not over, return null;
     *
     * thief == 0
	 *
	 * @return
	 * 		a message that tells who has won the game, or null if the
	 * 		game is not over
	 */
	@Override
	protected String checkIfGameOver()
    {

        if (gameState.isGameOver()) {
            int winner = gameState.getWinnerId();
            if (winner == 0) {
                return "Thief wins — escaped with the painting!";
            } else {
                return "Detectives win - the thief was not caught!";
            }
        }

        return null;
    }

}// class CounterLocalGame
