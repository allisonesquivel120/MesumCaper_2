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

    }

    @Test
    public void MuseumCaperState() {

    }
}