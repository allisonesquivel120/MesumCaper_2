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
    SETUP,
    PLAY,
    THIEF_MOVE,
    DETECTIVE_MOVE,
    ALARM_PHASE,
    ENDGAME
}
