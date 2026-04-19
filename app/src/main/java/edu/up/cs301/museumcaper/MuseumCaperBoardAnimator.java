package edu.up.cs301.museumcaper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceView;

import java.util.Arrays;

import edu.up.cs301.GameFramework.animation.Animator;

/**
 * Draws the Museum Caper board as colored tiles matching room types.
 *
 * @author Allison E.
 * @author Jayden H.
 * @author Farid S.
 * @version March 2026
 */
public class MuseumCaperBoardAnimator implements Animator {

    private volatile MuseumCaperState state;

    //private SurfaceView sv = findViewById(R.id.boardSurfaceView);
    private final Paint paint = new Paint();
    private final Paint gridPaint = new Paint();
    private final Paint guardPaint = new Paint();
    private final Paint thiefPaint = new Paint();
    private final Paint cameraPaint = new Paint();

    // tile dimensions — computed on first draw
    private float cellW, cellH;

    public MuseumCaperBoardAnimator(MuseumCaperState state) {
        this.state = state;

        gridPaint.setColor(Color.BLACK);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);

        guardPaint.setColor(Color.YELLOW);
        guardPaint.setStyle(Paint.Style.FILL);

        thiefPaint.setColor(Color.BLACK);
        thiefPaint.setStyle(Paint.Style.FILL);

        cameraPaint.setColor(Color.RED);
        cameraPaint.setStyle(Paint.Style.FILL);
    }

    public synchronized void setState(MuseumCaperState state) {
        this.state = state;
    }

    /**
     * Convert a raw touch X coordinate to a board column
     */
    public int xToCol(float x) {
        if (cellW == 0) return -1;
        return Math.max(0, Math.min((int)(x / cellW), MuseumCaperState.NUM_COLS - 1));
    }

    /**
     * Convert a raw touch Y coordinate to a board row
     */
    public int yToRow(float y) {
        if (cellH == 0) return -1;
        return Math.max(0, Math.min((int)(y / cellH), MuseumCaperState.NUM_ROWS - 1));
    }

    @Override
    public void tick(Canvas canvas) {
        if (state == null) return;

        int width  = canvas.getWidth();
        int height = canvas.getHeight();

        //  THIS TEMPORARILY
        android.util.Log.d("BOARD_DEBUG", "canvas size: " + width + "x" + height);
        android.util.Log.d("BOARD_DEBUG", "paintingPositions: " + Arrays.toString(state.getPaintingPositions()));

        cellW = (float) width  / MuseumCaperState.NUM_COLS;  // 12 cols
        cellH = (float) height / MuseumCaperState.NUM_ROWS;  // 11 rows

        char[][] board = state.getGameBoard();

        // --- draw tiles ---
        for (int r = 0; r < MuseumCaperState.NUM_ROWS; r++) {
            for (int c = 0; c < MuseumCaperState.NUM_COLS; c++) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(colorForTile(board[r][c]));

                float left   = c * cellW;
                float top    = r * cellH;
                float right  = left + cellW;
                float bottom = top  + cellH;

                canvas.drawRect(left, top, right, bottom, paint);
            }
        }

        // --- draw P label on power room tile (row 10, col 8) ---
        Paint labelPaint = new Paint();
        labelPaint.setColor(Color.BLACK);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(cellH * 0.6f);
        labelPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);

        float px = 8 * cellW + cellW / 2f;
        float py = 10 * cellH + cellH * 0.65f;
        canvas.drawText("P", px, py, labelPaint);

        // --- draw borders (thin between same room, thick between different rooms) ---
        Paint thinBorder = new Paint();
        thinBorder.setColor(Color.BLACK);
        thinBorder.setStyle(Paint.Style.STROKE);
        thinBorder.setStrokeWidth(1f);

        Paint thickBorder = new Paint();
        thickBorder.setColor(Color.BLACK);
        thickBorder.setStyle(Paint.Style.STROKE);
        thickBorder.setStrokeWidth(5f);

        Paint outerBorder = new Paint();
        outerBorder.setColor(Color.BLACK);
        outerBorder.setStyle(Paint.Style.STROKE);
        outerBorder.setStrokeWidth(8f);

        for (int r = 0; r < MuseumCaperState.NUM_ROWS; r++) {
            for (int c = 0; c < MuseumCaperState.NUM_COLS; c++) {
                float left = c * cellW;
                float top = r * cellH;
                float right = left + cellW;
                float bottom = top + cellH;

                if (board[r][c] != 't') {
                    // draws borders between rooms
                    // thin borders within rooms, thick borders separating rooms

                    // check right neighbor — draw thick border if different room type
                    if ((c + 1) < MuseumCaperState.NUM_COLS) {
                        Paint p = board[r][c] != board[r][c+1] ? thickBorder : thinBorder;
                        canvas.drawLine(right, top, right, bottom, p);
                    }

                    // check bottom neighbor — draw thick border if different room type
                    if ((r + 1) < MuseumCaperState.NUM_ROWS) {
                        Paint p = board[r][c] != board[r+1][c] ? thickBorder : thinBorder;
                        canvas.drawLine(left, bottom, right, bottom, p);
                    }

                    // draws right outer border
                    if (c == MuseumCaperState.NUM_COLS - 1) {
                        canvas.drawLine(right, top, right, bottom, outerBorder);
                    }

                    // draws bottom outer border
                    if (r == MuseumCaperState.NUM_ROWS - 1) {
                        canvas.drawLine(left, bottom, right, bottom, outerBorder);
                    }

                    // draws left outer border
                    if (c == 0) {
                        canvas.drawLine(right - cellW, bottom, right - cellW, top, outerBorder);
                    }
                } else {
                    if (((c + 1) < MuseumCaperState.NUM_COLS) && board[r][c+1] != 't') {
                        canvas.drawLine(right, top, right, bottom, thickBorder);
                    }
                    if (((r + 1) < MuseumCaperState.NUM_ROWS) && board[r+1][c] != 't') {
                        canvas.drawLine(left, bottom, right, bottom, thickBorder);
                    }
                }
            }
        }

        // --- draw cameras ---
        boolean[][] cameras = state.getCameras();
        for (int r = 0; r < MuseumCaperState.NUM_ROWS; r++) {
            for (int c = 0; c < MuseumCaperState.NUM_COLS; c++) {
                if (cameras[r][c]) {
                    float cx = c * cellW + cellW / 2f;
                    float cy = r * cellH + cellH / 2f;
                    canvas.drawCircle(cx, cy, Math.min(cellW, cellH) * 0.25f, cameraPaint);
                }
            }
        }

        // --- draw guard ---
        int[] guardRows = state.getGuardRow();
        int[] guardCols = state.getGuardCol();
        for (int i = 0; i < guardRows.length; i++) {
            float cx = guardCols[i] * cellW + cellW / 2f;
            float cy = guardRows[i] * cellH + cellH / 2f;
            canvas.drawCircle(cx, cy, Math.min(cellW, cellH) * 0.35f, guardPaint);
        }

        // --- draw thief (only if visible) ---
        if (state.isThiefVisible()) {
            float cx = state.getThiefCol() * cellW + cellW / 2f;
            float cy = state.getThiefRow() * cellH + cellH / 2f;
            canvas.drawCircle(cx, cy, Math.min(cellW, cellH) * 0.3f, thiefPaint);
        }
        // --- draw paintings ---
        Paint paintingPaint = new Paint();
        paintingPaint.setColor(Color.rgb(139, 90, 43)); // brown
        paintingPaint.setStyle(Paint.Style.FILL);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(cellH * 0.4f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        int[] paintingPositions = state.getPaintingPositions();
        for (int i = 0; i < paintingPositions.length; i++) {
            if (paintingPositions[i] == -1) continue; // not placed yet
            int r = paintingPositions[i] / MuseumCaperState.NUM_COLS;
            int c = paintingPositions[i] % MuseumCaperState.NUM_COLS;

            float left   = c * cellW + cellW * 0.1f;
            float top    = r * cellH + cellH * 0.1f;
            float right  = left + cellW * 0.8f;
            float bottom = top  + cellH * 0.8f;

            canvas.drawRect(left, top, right, bottom, paintingPaint);
            canvas.drawText(
                    String.valueOf(i + 1),       // painting number
                    c * cellW + cellW / 2f,
                    r * cellH + cellH * 0.65f,
                    textPaint
            );
        }
    }

    /**
     * Maps board char to room color — matches your presentation's color scheme
     */
    private int colorForTile(char tile) {
        switch (tile) {
            case 'r': return Color.rgb(220, 80,  80);  // red room
            case 'p': return Color.rgb(180, 100, 200); // purple room
            case 'b': return Color.rgb(100, 150, 220); // blue room
            case 'y': return Color.rgb(255, 186, 8);  // yellow room
            case 'g': return Color.rgb(106, 153, 78); // green room
            case 'w': return Color.rgb(230, 230, 230); // white room (center)
            case 'h': return Color.rgb(214, 204, 194); // hallway
            case 'd': return Color.rgb(213, 189, 175);  // door
            case 't': return Color.TRANSPARENT;  // inaccessible (dark)
            default:  return Color.rgb(237, 237, 233);
        }
    }

    @Override public int interval() { return 100; } // 10fps is enough
    @Override public int backgroundColor() { return Color.TRANSPARENT; }
    @Override public boolean doPause() { return false; }
    @Override public boolean doQuit() { return false; }
    @Override public void onTouch(MotionEvent event) { }
}