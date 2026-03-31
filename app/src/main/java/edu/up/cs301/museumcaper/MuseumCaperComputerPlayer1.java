package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.players.GameComputerPlayer;
import edu.up.cs301.GameFramework.infoMessage.GameInfo;
import edu.up.cs301.GameFramework.utilities.Tickable;

/**
 * The AI computer player for Museum Caper.
 * Currently used as the thief (player 0) — movement is handled automatically
 * inside MuseumCaperState.runThiefAI(), so this class does nothing for now.
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 * @version March 2026
 */
public class MuseumCaperComputerPlayer1 extends GameComputerPlayer implements Tickable {

    // which guard this AI controls (0-indexed) — used if this AI plays as detective
    private int guardIndex;

    /**
     * Constructor with guard index.
     * @param name       the player's display name
     * @param guardIndex which guard this AI controls (0 = first guard)
     */
    public MuseumCaperComputerPlayer1(String name, int guardIndex) {
        super(name);
        this.guardIndex = guardIndex;
    }

    /**
     * Default constructor — assumes guard index 0.
     * @param name the player's display name
     */
    public MuseumCaperComputerPlayer1(String name) {
        super(name);
        this.guardIndex = 0;
    }

    /**
     * Receives a game state update and decides what action to take.
     * Since this AI is currently assigned as the thief (player 0),
     * and the thief moves automatically inside runThiefAI(), this method
     * does nothing. The guard logic below is kept for future use if this
     * AI is ever assigned as a detective instead.
     *
     * @param info the latest game state
     */
    @Override
    protected void receiveInfo(GameInfo info) {
        // thief AI movement is handled inside MuseumCaperState.runThiefAI()
        // nothing to do here for the thief role
    }

    /** @return this player's framework-assigned player number */
    @Override
    public int getPlayerNum() {
        return this.playerNum;
    }
}