package edu.up.cs301.museumcaper;

import static org.junit.Assert.*;

import org.junit.Test;

public class MuseumCaperStateTest {

    @Test
    public void testDefaultConstructor(){
        MuseumCaperState state = new MuseumCaperState();
        //thief is in row 11, col 11
        assertEquals(11, state.getThiefCol());
        assertEquals(7, state.getThiefRow());

        assertFalse(state.isGameOver());

        //no win = no winner yet
        assertEquals(-1,state.getWinnerId());

        //dies
        assertEquals(0,state.getQuestionRoll());
        assertEquals(0,state.getMovementRoll());

        //thief has to go first
        assertEquals(0, state.getPlayerTurn());

        int[] guardRows = state.getGuardRow();
        int[] guardCols = state.getGuardCol();
        assertEquals(3, guardRows[0]);
        assertEquals(4, guardCols[0]);
    }

    @Test
    public void makeMarkStolenPaintingsAction() {
        MuseumCaperState state = new MuseumCaperState();
        // Pass null for the GamePlayer parameter — the action only uses paintingId
        MuseumCaperMarkStolenPaintingsAction action = new MuseumCaperMarkStolenPaintingsAction(null, 42);

        // The action should succeed
        boolean result = state.makeMarkStolenPaintingsAction(action);
        assertTrue(result);

        // Calling again with the same id should NOT add a duplicate
        boolean duplicate = state.makeMarkStolenPaintingsAction(action);
        assertTrue(duplicate); // still returns true (not an error)

        // A second distinct painting can also be added
        MuseumCaperMarkStolenPaintingsAction action2 = new MuseumCaperMarkStolenPaintingsAction(null, 99);
        boolean result2 = state.makeMarkStolenPaintingsAction(action2);
        assertTrue(result2);
    }

    @Test
    public void testCopyConstructor() {
        MuseumCaperState firstInstance = new MuseumCaperState();
        firstInstance.setPlayerNames(0, "Allison");
        MuseumCaperState firstCopy = new MuseumCaperState(firstInstance, 0);

        // set number of players
        firstInstance.setNumPlayers(1);
//        firstInstance.setPlayerNames(0, "Allison");
//        firstInstance.setPlayerNames(1, "Farid");

        // set the thief's position
        firstInstance.setThiefPosition(9, 7);

        // check!
        assertEquals(9, firstInstance.getThiefRow());
        assertEquals(7, firstInstance.getThiefCol());

        // TEST ROLL DICE ACTION
        // sets playerTurn to the guard, set GamePhase to GUARD_ROLL
        firstInstance.setPlayerTurn(1);
        firstInstance.setGamePhase(GamePhase.GUARD_ROLL);

        MuseumCaperRollDiceAction yahtzee = new MuseumCaperRollDiceAction(null, DiceType.MOVEMENT);
        firstInstance.makeRollDiceAction(yahtzee);

        // check!
        assertEquals(GamePhase.GUARD_MOVE, firstInstance.getCurrentPhase());

        // hardcode the dice roll
        firstInstance.setMovementRoll(5);

        MuseumCaperGuardMoveAction onTheMove = new MuseumCaperGuardMoveAction(null, 5, 7);
        firstInstance.makeGuardMoveAction(onTheMove);

        // check!
        assertEquals(5, firstInstance.getGuardRow());
        assertEquals(7, firstInstance.getGuardCol());

        // set the thief's position
        firstInstance.setThiefPosition(9, 5);

        // check!
        assertEquals(9, firstInstance.getThiefRow());
        assertEquals(5, firstInstance.getThiefCol());

        // painting id eventually must be associated with the painting's position
        MuseumCaperMarkStolenPaintingsAction action = new MuseumCaperMarkStolenPaintingsAction(null, 42);
        firstInstance.makeMarkStolenPaintingsAction(action);

        // check!
        assertEquals(true, firstInstance.makeMarkStolenPaintingsAction(action));

        // roll die action
        // sets playerTurn to the guard, set GamePhase to GUARD_ROLL
        firstInstance.setPlayerTurn(1);
        firstInstance.setGamePhase(GamePhase.GUARD_ROLL);

        // the guard moves again and lands on the thief
        MuseumCaperRollDiceAction againAgain = new MuseumCaperRollDiceAction(null, DiceType.MOVEMENT);
        firstInstance.makeRollDiceAction(againAgain);

        // check!
        assertEquals(GamePhase.GUARD_MOVE, firstInstance.getCurrentPhase());

        // hardcode the dice roll
        firstInstance.setMovementRoll(6);

        MuseumCaperGuardMoveAction rollingOut = new MuseumCaperGuardMoveAction(null, 5, 9);
        firstInstance.makeGuardMoveAction(rollingOut);

        // check!
        assertEquals(5, firstInstance.getGuardRow());
        assertEquals(9, firstInstance.getGuardCol());

        // GAME OVER

        MuseumCaperState secondInstance = new MuseumCaperState();
        MuseumCaperState secondCopy = new MuseumCaperState(secondInstance, 1);

        assertEquals(firstCopy.toString(), secondCopy.toString());
    }
}