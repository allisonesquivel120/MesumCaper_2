package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.infoMessage.GameState;
import edu.up.cs301.GameFramework.players.GamePlayer;
import edu.up.cs301.GameFramework.LocalGame;
import edu.up.cs301.GameFramework.actionMessage.GameAction;

/**
 * The local game controller for Museum Caper.
 * Validates and routes all player actions to the game state,
 * enforces turn order, and checks win conditions.
 *
 * @author Allison E.
 * @author Jayden H.
 * @author Farid S.
 * @version March 2026
 */
public class MuseumCaperLocalGame extends LocalGame {

    private MuseumCaperState gameState;

    /**
     * Constructor — initializes the game state.
     * @param state the initial game state, or null to create a default 2-player game
     */
    public MuseumCaperLocalGame(GameState state) {
        if (!(state instanceof MuseumCaperState)) {
            state = new MuseumCaperState(2);
        }
        this.gameState = (MuseumCaperState) state;
        super.state = state;
    }

    /**
     * Determines whether a given player is allowed to act right now.
     * During SETUP, any player can place pieces.
     * During GUARD_ROLL and GUARD_MOVE, any player can act regardless of
     * playerTurn, this handles a framework mismatch where the human detective
     * is registered as player 0 but playerTurn is 1.
     * All other phases use strict turn matching.
     *
     * @param playerIdx the index of the player attempting to act
     * @return true if the player is allowed to move
     */
    @Override
    protected boolean canMove(int playerIdx) {
        if (gameState.getCurrentPhase() == GamePhase.SETUP) return true;
        if (gameState.getCurrentPhase() == GamePhase.GUARD_ROLL ||
                gameState.getCurrentPhase() == GamePhase.GUARD_MOVE) return true;
        return playerIdx == gameState.getPlayerTurn();
    }

    /**
     * Routes a player action to the correct handler in the game state.
     * Returns true if the action was valid and applied, false otherwise.
     *
     * @param action the action sent by a player
     * @return true if the action was legal and successfully applied
     */
    @Override
    protected boolean makeMove(GameAction action) {
        // setup actions
        if (action instanceof MuseumCaperFinishSetupAction) {
            return gameState.makeFinishSetupAction((MuseumCaperFinishSetupAction) action);
        }
        if (action instanceof MuseumCaperPlacePaintingAction) {
            return gameState.makePlacePaintingAction((MuseumCaperPlacePaintingAction) action);
        }
        if (action instanceof MuseumCaperPlaceCameraAction) {
            return gameState.makePlaceCameraAction((MuseumCaperPlaceCameraAction) action);
        }
        // connection and name actions
        if (action instanceof MuseumCaperSetNameAction) {
            return gameState.makeSetNameAction((MuseumCaperSetNameAction) action);
        }
        if (action instanceof MuseumCaperConnectAction) {
            return gameState.makeConnectAction((MuseumCaperConnectAction) action);
        }
        // gameplay actions
        if (action instanceof MuseumCaperRollDiceAction) {
            return gameState.makeRollDiceAction((MuseumCaperRollDiceAction) action);
        }
        if (action instanceof MuseumCaperGuardMoveAction) {
            return gameState.makeGuardMoveAction((MuseumCaperGuardMoveAction) action);
        }
        if (action instanceof MuseumCaperMarkStolenPaintingsAction) {
            return gameState.makeMarkStolenPaintingsAction((MuseumCaperMarkStolenPaintingsAction) action);
        }
        if (action instanceof MuseumCaperChooseQuestionAction) {
            return gameState.makeChooseQuestionAction((MuseumCaperChooseQuestionAction) action);
        }
        if (action instanceof MuseumCaperEndTurnAction) {
            return gameState.makeEndTurnAction((MuseumCaperEndTurnAction) action);
        }
        return false; // unknown action
    }

    /**
     * Sends a player-perspective copy of the game state to the given player.
     * The thief's position is hidden from the detective unless thiefVisible is true.
     *
     * @param p the player to send the state to
     */
    @Override
    protected void sendUpdatedStateTo(GamePlayer p) {
        p.sendInfo(new MuseumCaperState(this.gameState, p.getPlayerNum()));
    }

    /**
     * Checks if the game is over and returns the winner message.
     * Thief wins by stealing 3+ paintings.
     * Detective wins by landing on the thief's tile.
     *
     * @return a winner message string, or null if the game is still ongoing
     */
    @Override
    protected String checkIfGameOver() {
        if (gameState.isGameOver() && gameState.getWinnerId() != -1) {
            return gameState.getWinnerId() == 0
                    ? "Thief wins — escaped with the paintings!"
                    : "Detectives win — the thief was caught!";
        }
        return null;
    }
}