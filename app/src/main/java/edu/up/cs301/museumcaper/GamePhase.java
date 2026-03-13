package edu.up.cs301.museumcaper;
/**
 * GamePhase
 *
 * This class tells you what stage of the turn/round player is on, needed for GameState
 * - what actions are allowed
 * - what actions aren't allowed
 *
 * @author Jayden H.
 * @author Farid S.
 * @author Allison E.
 * @version Feb 2026
 */
public enum GamePhase {
    SETUP, // board initialization
    PLAY, // main gameplay loop
    THIEF_TURN, // AI thief is taking its turn
    GUARD_ROLL, // detective is rolling for movement
    GUARD_MOVE, // detective is choosing destination
    ENDGAME // game has ended
}
