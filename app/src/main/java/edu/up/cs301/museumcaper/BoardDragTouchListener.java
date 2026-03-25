package edu.up.cs301.museumcaper;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

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
    private final GridLayout boardGrid;

    private final OnPieceDroppedListener dropListener;

    public BoardDragTouchListener(ViewGroup piecesLayer, GridLayout boardGrid, OnPieceDroppedListener dropListener) {
        this.piecesLayer = piecesLayer;
        this.boardGrid = boardGrid;
        this.dropListener = dropListener;
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
                return true;

            // event that actually moves the image
            case MotionEvent.ACTION_MOVE:

                //getRawX() = fingers position on the entire screen
                // updates the coordinates of the image = moves
                view.setX(event.getRawX() + dX);
                view.setY(event.getRawY() + dY);

                return true;

            // event that finalizes what happens to the piece
            case MotionEvent.ACTION_UP:

                // If dropped inside the grid area, snap to a tile
                if (isInsideView(event, boardGrid)) {

                    int[] gridLoc = new int[2];
                    boardGrid.getLocationOnScreen(gridLoc);

                    float xInGrid = event.getRawX() - gridLoc[0];
                    float yInGrid = event.getRawY() - gridLoc[1];

                    int cols = boardGrid.getColumnCount(); // 12
                    int rows = boardGrid.getRowCount();    // 11

                    float cellW = boardGrid.getWidth() / (float) cols;
                    float cellH = boardGrid.getHeight() / (float) rows;

                    int col = (int) (xInGrid / cellW);
                    int row = (int) (yInGrid / cellH);

                    // clamp to valid grid indices
                    col = Math.max(0, Math.min(col, cols - 1));
                    row = Math.max(0, Math.min(row, rows - 1));

                    // snap to center of the tile
                    float snappedX = (col + 0.5f) * cellW - (view.getWidth() / 2f);
                    float snappedY = (row + 0.5f) * cellH - (view.getHeight() / 2f);

                    // clamp so the piece never ends up partially outside
                    snappedX = Math.max(0, Math.min(snappedX, piecesLayer.getWidth() - view.getWidth()));
                    snappedY = Math.max(0, Math.min(snappedY, piecesLayer.getHeight() - view.getHeight()));

                    // Move into piecesLayer if needed
                    ViewGroup oldParent = (ViewGroup) view.getParent();
                    if (oldParent != piecesLayer) {
                        oldParent.removeView(view);
                        piecesLayer.addView(view);
                    }

                    view.setX(snappedX);
                    view.setY(snappedY);
                    // notify GameState
                    if (dropListener != null) {
                        dropListener.onPieceDropped(view, row, col);
                    }

                    view.bringToFront();
                }
                // this else and if statement will snap the pieces back when dropped outside board
                else {
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

                // Special-case rule for pawn drop: convert to dot and resize
                if (view.getId() == R.id.yellow_Pawn) {
                    ((ImageView) view).setImageResource(R.drawable.yellow_dot);
                    ViewGroup.LayoutParams params = view.getLayoutParams();

                    // dp-safe sizing instead of raw 80px
                    int sizePx = dpToPx(view, 24); // pick your dot size in dp
                    params.width = sizePx;
                    params.height = sizePx;

                    view.setLayoutParams(params);
                }

                return true;

            default:
                return false;
        }
    }

    // helper method = checks whether the image is inside a target view's bounds
    private boolean isInsideView(MotionEvent event, View target) {
        int[] loc = new int[2];
        target.getLocationOnScreen(loc);
        // in grid
        float x = event.getRawX();
        float y = event.getRawY();

        return x >= loc[0] &&
                x <= loc[0] + target.getWidth() &&
                y >= loc[1] &&
                y <= loc[1] + target.getHeight();
    }

    private int dpToPx(View view, int dp) {
        float density = view.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}