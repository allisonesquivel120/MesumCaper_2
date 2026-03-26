package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.players.GameComputerPlayer;
import edu.up.cs301.GameFramework.infoMessage.GameInfo;
import edu.up.cs301.GameFramework.utilities.Tickable;

/**
 * old computer player class for counter game
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
public class MuseumCaperComputerPlayer1 extends GameComputerPlayer implements Tickable {

    /**
     * Constructor for objects of class CounterComputerPlayer1
     *
     * @param name
     * the player's name
     */
    private int guardIndex; // which guard the AI controls

    public MuseumCaperComputerPlayer1(String name, int guardIndex) {
        // invoke superclass constructor
        super(name);
        this.guardIndex = guardIndex;
    }

    public MuseumCaperComputerPlayer1(String name) {
        super(name);
        this.guardIndex = 0;
    }

    /**
     * callback method--game's state has changed
     *
     * @param info the information (presumably containing the game's state)
     */
    @Override
    protected void receiveInfo(GameInfo info) {
        if (!(info instanceof MuseumCaperState)) return;

        MuseumCaperState state = (MuseumCaperState) info;

        GamePhase phase = state.getCurrentPhase();

        // Bug fix: handle GUARD_ROLL phase — computer must roll before it can move
        if (phase == GamePhase.GUARD_ROLL) {
            game.sendAction(new MuseumCaperRollDiceAction(this, DiceType.MOVEMENT));
            return;
        }

        // now handle the move after rolling
        if (phase == GamePhase.GUARD_MOVE) {
            int row = state.getGuardRow(guardIndex);
            int col = state.getGuardCol(guardIndex);
            int thiefRow = state.getThiefRow();
            int thiefCol = state.getThiefCol();

            int targetRow = row;
            int targetCol = col;

            // move toward thief if visible, else move randomly
            if (thiefRow != -1 && thiefCol != -1) {
                if (row < thiefRow) targetRow++;
                else if (row > thiefRow) targetRow--;
                else if (col < thiefCol) targetCol++;
                else if (col > thiefCol) targetCol--;
            } else {
                if (Math.random() < 0.5) targetRow += (Math.random() < 0.5 ? -1 : 1);
                else targetCol += (Math.random() < 0.5 ? -1 : 1);
            }

            // keep within bounds
            if (targetRow < 0) targetRow = 0;
            if (targetRow >= MuseumCaperState.NUM_ROWS) targetRow = MuseumCaperState.NUM_ROWS - 1;
            if (targetCol < 0) targetCol = 0;
            if (targetCol >= MuseumCaperState.NUM_COLS) targetCol = MuseumCaperState.NUM_COLS - 1;

            game.sendAction(new MuseumCaperGuardMoveAction(this, guardIndex, targetRow, targetCol));
        }
    }

        @Override
        public int getPlayerNum() {
            return this.playerNum;
        }
    }

