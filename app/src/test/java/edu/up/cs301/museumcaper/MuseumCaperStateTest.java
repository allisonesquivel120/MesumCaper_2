package edu.up.cs301.museumcaper;

import static org.junit.Assert.*;

import org.junit.Test;


/**
 * This contains the test for the constructor, copy constructor,
 * and markStolenPainting method
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
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

    /**
     * External Cite: CO-PILOT
     * issue : asked if the order of the test was correct [before test passed]
     * we found out that the order [moving, making copy, and checking if it copied]
     * was wrong = test to fail continuously
     */

    @Test
    public void testCopyConstructor() {
        // create original game state
        MuseumCaperState firstInstance = new MuseumCaperState();

        firstInstance.setNumPlayers(2);
        firstInstance.setPlayerNames(0, "Allison");
        firstInstance.setPlayerNames(1, "GuardBob");

        // copy!
        MuseumCaperState firstCopy = new MuseumCaperState(firstInstance, 1);


        // set thief position and mark a stolen painting
        firstInstance.setThiefPosition(9, 7);
        MuseumCaperMarkStolenPaintingsAction steal = new MuseumCaperMarkStolenPaintingsAction(null, 42);
        firstInstance.makeMarkStolenPaintingsAction(steal);

        // check!
        assertEquals(true, firstInstance.makeMarkStolenPaintingsAction(steal));

        // copy from thief perspective
        MuseumCaperState thiefCopy = new MuseumCaperState(firstInstance, 0);

        // copy from guard perspective
        MuseumCaperState guardCopy = new MuseumCaperState(firstInstance, 1);

        // check that thief sees their own position
        assertEquals(9, thiefCopy.getThiefRow());
        assertEquals(7, thiefCopy.getThiefCol());

        // check that guard cannot see thief if hidden
        if (!firstInstance.isThiefVisible()) {
            assertEquals(-1, guardCopy.getThiefRow());
            assertEquals(-1, guardCopy.getThiefCol());
        }

        // stolen paintings should be visible to both thief and guard
        assertTrue(thiefCopy.getStolenPaintings().contains(42));
        assertTrue(guardCopy.getStolenPaintings().contains(42));

        // test guard action (roll + move)
        firstInstance.setPlayerTurn(1);
        firstInstance.setGamePhase(GamePhase.GUARD_ROLL);
        MuseumCaperRollDiceAction roll = new MuseumCaperRollDiceAction(null, DiceType.MOVEMENT);
        firstInstance.makeRollDiceAction(roll);

        // check!
        assertEquals(GamePhase.GUARD_MOVE, firstInstance.getCurrentPhase());

        // hardcode movement for testing
        firstInstance.setMovementRoll(5);
        // let's get rid of guard index
        MuseumCaperGuardMoveAction moveGuard = new MuseumCaperGuardMoveAction(null, 5, 7, 1);
        firstInstance.makeGuardMoveAction(moveGuard);

        // check!
        assertEquals(5, firstInstance.getGuardRow());
        assertEquals(7, firstInstance.getGuardCol());

        // thief moves
        firstInstance.setThiefPosition(9, 5);

        // check independence: thief copy should not be affected
        assertNotEquals(firstInstance.toString(), thiefCopy.toString());

        // verify positions and stolen paintings in copy remain unchanged
        assertEquals(9, thiefCopy.getThiefRow());
        assertEquals(5, thiefCopy.getThiefCol());
        assertTrue(thiefCopy.getStolenPaintings().contains(42));

        // verify guard copy still sees original stolen painting
        assertTrue(guardCopy.getStolenPaintings().contains(42));

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

        MuseumCaperGuardMoveAction rollingOut = new MuseumCaperGuardMoveAction(null, 5, 9, 1);
        firstInstance.makeGuardMoveAction(rollingOut);

        // check!
        assertEquals(5, firstInstance.getGuardRow());
        assertEquals(9, firstInstance.getGuardCol());

        // GAME OVER

        // create second instance
        MuseumCaperState secondInstance = new MuseumCaperState();
        MuseumCaperState secondCopy = new MuseumCaperState(secondInstance, 1);

        // final check!
        assertEquals(firstCopy.toString(), secondCopy.toString());
    }
}