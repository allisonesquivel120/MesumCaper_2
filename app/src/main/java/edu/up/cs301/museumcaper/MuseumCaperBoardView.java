package edu.up.cs301.museumcaper;

import android.content.Context;
import android.util.AttributeSet;
import edu.up.cs301.GameFramework.animation.AnimationSurface;

/**
 * The SurfaceView that renders the Museum Caper game board.
 *
 * @author Allison E.
 * @author Jayden H.
 * @author Farid S.
 * @version March 2026
 */
public class MuseumCaperBoardView extends AnimationSurface {

    private MuseumCaperBoardAnimator boardAnimator;

    public MuseumCaperBoardView(Context context) {
        super(context);
    }

    public MuseumCaperBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Call this from HumanPlayer to give the board its state
     */
    public void setState(MuseumCaperState state) {
        if (boardAnimator == null) {
            boardAnimator = new MuseumCaperBoardAnimator(state);
            setAnimator(boardAnimator);
        } else {
            boardAnimator.setState(state);
        }
    }

    /**
     * Returns the animator so HumanPlayer can get row/col from a touch
     */
    public MuseumCaperBoardAnimator getBoardAnimator() {
        return boardAnimator;
    }
}