package edu.up.cs301.museumcaper;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

/**
 * this is the logic behind the drag and drop action
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 * @version March 2026
 */

/**
 * External Citation
 * Date: 1 Feb 2026
 * Problem: could not get the image to drag and drop onto game board
 * Resource: gerardo esquivel
 * Solution: used the drag and drop pdf provided + copilot
 * to figure out the components needed for actions down, move, and up
 */


public class BoardDragTouchListener implements View.OnTouchListener {

    // handler = listener = responds to an object action (drag and drop)
    private float dX, dY;

    // Pieces live here (FrameLayout piecesLayer is best)
    private final ViewGroup piecesLayer;

    // Grid overlay used for snapping
    private final ImageView boardImage;

    private final OnPieceDroppedListener dropListener;
    private RectF displayedRect = new RectF();

    public BoardDragTouchListener(ViewGroup piecesLayer, ImageView boardImage, OnPieceDroppedListener dropListener) {
        this.piecesLayer = piecesLayer;
        this.boardImage = boardImage;
        this.dropListener = dropListener;
    }
    /** Compute the actual displayed rectangle of the board image */
    private void computeDisplayedRect() {
        Drawable drawable = boardImage.getDrawable();
        if (drawable == null) {
            displayedRect = new RectF(0, 0, 0, 0);
            return;
        }

        Matrix matrix = boardImage.getImageMatrix();
        RectF drawableRect = new RectF(
                0, 0,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight()
        );

        displayedRect = new RectF();
        matrix.mapRect(displayedRect, drawableRect);

        // Offset by ImageView's position on screen
        displayedRect.offset(boardImage.getX(), boardImage.getY());
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction()) {

            // action down = when you first press the image
            case MotionEvent.ACTION_DOWN:
                if (view.getTag(R.id.tag_original_parent) == null) {
                    view.setTag(R.id.tag_original_parent, view.getParent());
                }

                if (view.getTag(R.id.tag_original_x) == null || view.getTag(R.id.tag_original_y) == null) {
                    view.setTag(R.id.tag_original_x, view.getX());
                    view.setTag(R.id.tag_original_y, view.getY());
                }

                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();

                Log.d("DRAG", "Touch DOWN on: " + view.getId());
                return true;

            // event that actually moves the image
            case MotionEvent.ACTION_MOVE:
                //getRawX() = fingers position on the entire screen
                float newX = event.getRawX();
                float newY = event.getRawY();

                // Clamp inside board image
                float minX = displayedRect.left;
                float maxX = displayedRect.right - view.getWidth();
                float minY = displayedRect.top;
                float maxY = displayedRect.bottom - view.getHeight();

                newX = Math.max(minX, Math.min(newX, maxX));
                newY = Math.max(minY, Math.min(newY, maxY));


                // updates the coordinates of the image = moves
                view.setX(newX);
                view.setY(newY);

                return true;

            // event that finalizes what happens to the piece
            case MotionEvent.ACTION_UP:
                // If dropped inside the grid area, snap to a tile
                //  - restore original position
                if (!isInsideBoard(event)) {
                    restoreOriginalPosition(view);
                    return true;
                }

                // Notify GameState (row/col optional for free drag)
                if (dropListener != null) {
                    dropListener.onPieceDropped(view, -1, -1);
                }

                // Pawn special rule
                if (view.getId() == R.id.yellow_Pawn) {
                    ((ImageView) view).setImageResource(R.drawable.yellow_dot);
                    ViewGroup.LayoutParams params = view.getLayoutParams();
                    int sizePx = dpToPx(view, 24);
                    params.width = sizePx;
                    params.height = sizePx;
                    view.setLayoutParams(params);
                }

                view.bringToFront();
                return true;
        }
        return false;
    }

    // helper method = checks whether the image is inside a target view's bounds
    private boolean isInsideBoard(MotionEvent event) {
        // in grid
        float x = event.getRawX();
        float y = event.getRawY();
        return displayedRect.contains(x, y);
    }
    /** Restore piece to original parent + position */
    private void restoreOriginalPosition(View view) {
        ViewGroup originalParent = (ViewGroup) view.getTag(R.id.tag_original_parent);
        Float originalX = (Float) view.getTag(R.id.tag_original_x);
        Float originalY = (Float) view.getTag(R.id.tag_original_y);

        if (originalParent != null && originalX != null && originalY != null) {
            ViewGroup currentParent = (ViewGroup) view.getParent();
            currentParent.removeView(view);
            originalParent.addView(view);

            view.setX(originalX);
            view.setY(originalY);
            view.bringToFront();
        }
    }
    private int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}