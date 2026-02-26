package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.infoMessage.GameState;
import edu.up.cs301.GameFramework.players.GamePlayer;
import edu.up.cs301.GameFramework.LocalGame;
import edu.up.cs301.GameFramework.actionMessage.GameAction;
import android.util.Log;

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
        // only the current players turn can move
        return playerIdx == gameState.getPlayerTurn();
    }

    @Override
    protected boolean makeMove(GameAction action) {

        // ----- GUARD ------

        // general actions
        if (action instanceof MuseumCaperChooseNumberPlayerAction) {
            return gameState.makeChooseNumberPlayersAction((MuseumCaperChooseNumberPlayerAction) action);
        }
        if (action instanceof MuseumCaperSetNameAction) {
            return gameState.makeSetNameAction((MuseumCaperSetNameAction) action);
        }
        if (action instanceof MuseumCaperConnectAction) {
            return gameState.makeConnectAction((MuseumCaperConnectAction) action);
        }

        // move
        if (action instanceof MuseumCaperGuardMoveAction) {
            return gameState.makeGuardMoveAction((MuseumCaperGuardMoveAction) action);
        }
        // end player turn
        if (action instanceof MuseumCaperGuardEndTurnAction) {
            MuseumCaperGuardEndTurnAction a = (MuseumCaperGuardEndTurnAction) action;
            return gameState.makeGuardEndTurnAction(new MuseumCaperGuardEndTurnAction(a.getPlayer()));
        }
        // rolling dice for movement
        if (action instanceof MuseumCaperRollDiceForMovementAction) {
            return gameState.makeRollDiceForMovementAction((MuseumCaperRollDiceForMovementAction) action);
        }
        // rolling dice for camera
        if (action instanceof MuseumCaperRollDieForCamerasAction) {
            return gameState.makeRollDiceForCamerasAction((MuseumCaperRollDieForCamerasAction) action);
        }
        // marking the stolen paintings
        if (action instanceof MuseumCaperMarkStolenPaintingsAction)
        {
            return gameState.makeMakrStolenPaintingsAction((MuseumCaperMarkStolenPaintingsAction) action);
        }
        // choosing direction to move in
        if(action instanceof MuseumCaperChooseDirectionAction)
        {
            return gameState.makeChooseDirectionAction((MuseumCaperChooseDirectionAction) action);
        }
        // choosing question for camera/eye dice
        if(action instanceof MuseumCaperChooseQuestionAction)
        {
            return gameState.makeChooseQuestionAction((MuseumCaperChooseQuestionAction) action);

        }

        // ------ THIEF ------
        // rejecting the question of motion detector
        if(action instanceof MuseumCaperReject2MotionDetectorAction)
        {
            return gameState.makeReject2MotionDetectorAction((MuseumCaperReject2MotionDetectorAction) action);
        }
        // thief movement [ 3 steps only ]
        if (action instanceof MuseumCaperThiefMoveAction) {
            return gameState.makeThiefMoveAction((MuseumCaperThiefMoveAction) action);
        }
        // disabling camera(s)
        if (action instanceof MuseumCaperDisableCameraAction) {
            return gameState.makeDisableCameraAction((MuseumCaperDisableCameraAction) action);
        }
        // cutting all power --> cameras
        if (action instanceof MuseumCaperCutPowerAction) {
            return gameState.makeCutPowerAction((MuseumCaperCutPowerAction) action);
        }
        if (action instanceof MuseumCaperEndTurnAction) {
            MuseumCaperEndTurnAction a = (MuseumCaperEndTurnAction) action;
            return gameState.makeEndTurnAction(new MuseumCaperEndTurnAction(a.getPlayer()));
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
		p.sendInfo(new MuseumCaperState(this.gameState, p.getPlayerNum()));

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
