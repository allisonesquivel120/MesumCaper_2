package edu.up.cs301.museumcaper;

/**
 * The three possible faces of the question/camera die.
 * Each face triggers a different question the detective can ask the thief.
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 * @version March 2026
 */
public enum QuestionType {
    MOTION, // "What color room are you in?"
    SCAN,   // "Are all cameras working? Can any camera see you?"
    EYE     // "Can I see you?"
}