package edu.up.cs301.museumcaper;

import static org.junit.Assert.*;

import org.junit.Test;

public class MuseumCaperStateTest {

    @Test
    public void testDefaultConstructor(){
        MuseumCaperState state = new MuseumCaperState();
        //thief is in row 11, col 11
        assertEquals(11, state.getThiefCol());
        assertEquals(11, state.getThiefRow());

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
        assertEquals(0, guardRows[0]);
        assertEquals(0, guardCols[0]);
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
        MuseumCaperState firstCopy = new MuseumCaperState(firstInstance, 0);

        MuseumCaperState secondInstance = new MuseumCaperState();
        MuseumCaperState secondCopy = new MuseumCaperState(secondInstance, 0);

    }
}