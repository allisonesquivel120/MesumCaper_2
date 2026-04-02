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
    START_TURN, // main gameplay loop
    THIEF_TURN, // AI thief is taking its turn
    THIEF_MOVE, // AI is advancing
    GUARD_ROLL, // detective is rolling for movement
    GUARD_ASK, // detective is asking where thief is
    GUARD_MOVE, // detective is choosing destination
    GUARD_TURN_START, // detective can roll either die
    DETECTIVE_REVEAL, // detective reads the answer
    GUARD_QUESTION, // detective is getting asked a question
    ENDGAME // game has ended
}
