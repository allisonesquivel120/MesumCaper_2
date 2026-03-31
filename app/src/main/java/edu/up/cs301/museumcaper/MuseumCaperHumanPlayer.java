package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.players.GameHumanPlayer;
import edu.up.cs301.GameFramework.GameMainActivity;
import edu.up.cs301.GameFramework.infoMessage.GameInfo;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;

/**
 * The human player GUI for Museum Caper.
 * Handles the detective's turn: placing paintings/cameras during setup,
 * rolling dice, and moving the guard dot on the board.
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 * @version March 2026
 */
public class MuseumCaperHumanPlayer extends GameHumanPlayer implements OnClickListener {

    // current game state received from the local game
    private MuseumCaperState state;
    // the SurfaceView that draws the board tiles, guard, thief, cameras, and paintings
    private MuseumCaperBoardView boardSurfaceView;
    // tracks which painting or camera is currently selected during setup (-1 = none)
    private int selectedPaintingId = -1;
    private int selectedCameraId = -1;
    private ImageView selectedPieceView = null;
    private GameMainActivity myActivity;
    private TextView playerTurnTextView;
    private ImageButton movementDieButton;
    private ImageButton cameraDieButton;

    /**
     * Constructor
     * @param name the player's display name
     * @param i unused parameter kept for compatibility
     */
    public MuseumCaperHumanPlayer(String name, int i) {
        super(name);
    }

    /** Returns the root view of the GUI hierarchy */
    @Override
    public View getTopView() {
        return myActivity.findViewById(R.id.main_MuseumCaper);
    }

    /**
     * Updates all GUI elements to reflect the current game state.
     * Called every time a new state is received from the game.
     */
    protected void updateDisplay() {
        if (state == null || myActivity == null) return;

        // update turn indicator text
        if (playerTurnTextView != null) {
            int turn = state.getPlayerTurn();
            playerTurnTextView.setText(turn == 0 ? "Thief's Turn" : this.name + "'s Turn");
        }

        // enable movement die only during GUARD_ROLL phase
        if (movementDieButton != null) {
            int roll = state.getMovementRoll();
            movementDieButton.setImageResource(getMovementDieDrawable(roll));
            boolean canRoll = state.getCurrentPhase() == GamePhase.GUARD_ROLL;
            movementDieButton.setEnabled(canRoll);
            movementDieButton.setAlpha(canRoll ? 1.0f : 0.4f);
        }

        // enable camera die only during GUARD_QUESTION phase
        if (cameraDieButton != null) {
            int roll = state.getQuestionRoll();
            cameraDieButton.setImageResource(getCameraDieDrawable(roll));
            boolean canRoll = state.getCurrentPhase() == GamePhase.GUARD_QUESTION
                    && state.getPlayerTurn() == getPlayerNum();
            cameraDieButton.setEnabled(canRoll);
            cameraDieButton.setAlpha(canRoll ? 1.0f : 0.4f);
        }

        // show Done Setup button only during SETUP phase
        Button doneSetupButton = myActivity.findViewById(R.id.doneSetupButton);
        if (doneSetupButton != null) {
            boolean inSetup = state.getCurrentPhase() == GamePhase.SETUP;
            doneSetupButton.setVisibility(inSetup ? View.VISIBLE : View.GONE);
        }

        // lock paintings and cameras after setup ends so they can't be moved
        boolean inSetup = state.getCurrentPhase() == GamePhase.SETUP;
        int[] paintingViewIds = {
                R.id.painting1, R.id.painting2, R.id.painting3,
                R.id.painting4, R.id.painting5, R.id.painting6,
                R.id.painting7, R.id.painting8, R.id.painting9
        };
        for (int id : paintingViewIds) {
            ImageView v = myActivity.findViewById(id);
            if (v != null) v.setEnabled(inSetup);
        }
        int[] cameraViewIds = {
                R.id.offcamera, R.id.offcamera1, R.id.oncamera4,
                R.id.oncamera1, R.id.oncamera2,  R.id.oncamera3
        };
        for (int id : cameraViewIds) {
            ImageView v = myActivity.findViewById(id);
            if (v != null) v.setEnabled(inSetup);
        }

        // refresh the board so tiles, guard, thief, cameras, and paintings redraw
        if (boardSurfaceView != null) {
            boardSurfaceView.setState(state);
        }
    }

    /**
     * Returns the drawable resource for the movement die face.
     * @param roll the rolled value (1-6), defaults to face 1 if unrolled
     */
    private int getMovementDieDrawable(int roll) {
        switch (roll) {
            case 1: return R.drawable.basedie1;
            case 2: return R.drawable.basedie2;
            case 3: return R.drawable.basedie3;
            case 4: return R.drawable.basedie4;
            case 5: return R.drawable.basedie5;
            case 6: return R.drawable.basedie6;
            default: return R.drawable.basedie1;
        }
    }

    /**
     * Returns the drawable resource for the camera/question die face.
     * @param roll the rolled value (1-6), defaults to eye die if unrolled
     */
    private int getCameraDieDrawable(int roll) {
        switch (roll) {
            case 1: return R.drawable.basedie1;
            case 2: return R.drawable.basedie2;
            case 3: return R.drawable.basedie3;
            case 4: return R.drawable.basedie4;
            case 5: return R.drawable.basedie5;
            case 6: return R.drawable.basedie6;
            default: return R.drawable.eyedie2;
        }
    }

    /**
     * Handles clicks on the movement and camera dice buttons.
     * @param button the button that was clicked
     */
    @Override
    public void onClick(View button) {
        if (game == null) return;
        if (button.getId() == R.id.regulardie) {
            game.sendAction(new MuseumCaperRollDiceAction(this, DiceType.MOVEMENT));
        } else if (button.getId() == R.id.cameradie) {
            game.sendAction(new MuseumCaperRollDiceAction(this, DiceType.QUESTION));
        }
    }

    /**
     * Receives a new game state from the local game and refreshes the display.
     * @param info the game info object (expected to be MuseumCaperState)
     */
    @Override
    public void receiveInfo(GameInfo info) {
        if (!(info instanceof MuseumCaperState)) return;
        this.state = (MuseumCaperState) info;
        updateDisplay();
    }

    /**
     * Sets up the GUI when this player becomes the active GUI player.
     * Wires up all interactive elements: dice buttons, board touch listener,
     * painting/camera tap-to-select, and the Done Setup button.
     *
     * @param activity the main game activity
     */
    public void setAsGui(GameMainActivity activity) {
        myActivity = activity;
        activity.setContentView(R.layout.museumcaper_human_player);

        // wire up text and dice buttons
        playerTurnTextView = myActivity.findViewById(R.id.turnInfo);
        movementDieButton  = myActivity.findViewById(R.id.regulardie);
        cameraDieButton    = myActivity.findViewById(R.id.cameradie);
        movementDieButton.setOnClickListener(this);
        cameraDieButton.setOnClickListener(this);

        // wire up the SurfaceView board
        boardSurfaceView = myActivity.findViewById(R.id.boardSurfaceView);

        // paintings: tap a painting to select it for placement during setup
        int[] paintingViewIds = {
                R.id.painting1, R.id.painting2, R.id.painting3,
                R.id.painting4, R.id.painting5, R.id.painting6,
                R.id.painting7, R.id.painting8, R.id.painting9
        };
        for (int i = 0; i < paintingViewIds.length; i++) {
            final int paintingId = i + 1;
            ImageView painting = myActivity.findViewById(paintingViewIds[i]);
            if (painting == null) continue;
            painting.setOnClickListener(v -> {
                if (state == null || state.getCurrentPhase() != GamePhase.SETUP) return;
                // deselect previous piece
                if (selectedPieceView != null) selectedPieceView.setAlpha(1.0f);
                selectedPaintingId = paintingId;
                selectedCameraId   = -1;
                selectedPieceView  = (ImageView) v;
                selectedPieceView.setAlpha(0.5f); // dim to indicate selected
            });
        }

        // cameras: tap a camera to select it for placement during setup
        int[] cameraViewIds = {
                R.id.offcamera, R.id.offcamera1, R.id.oncamera4,
                R.id.oncamera1, R.id.oncamera2,  R.id.oncamera3
        };
        for (int i = 0; i < cameraViewIds.length; i++) {
            final int cameraId = i + 1;
            ImageView camera = myActivity.findViewById(cameraViewIds[i]);
            if (camera == null) continue;
            camera.setOnClickListener(v -> {
                if (state == null || state.getCurrentPhase() != GamePhase.SETUP) return;
                // deselect previous piece
                if (selectedPieceView != null) selectedPieceView.setAlpha(1.0f);
                selectedCameraId   = cameraId;
                selectedPaintingId = -1;
                selectedPieceView  = (ImageView) v;
                selectedPieceView.setAlpha(0.5f); // dim to indicate selected
            });
        }

        // board touch listener: handles both setup placement and guard movement
        if (boardSurfaceView != null) {
            boardSurfaceView.setOnTouchListener((v, event) -> {
                if (event.getAction() != android.view.MotionEvent.ACTION_UP) return true;
                if (state == null) return true;
                MuseumCaperBoardAnimator anim = boardSurfaceView.getBoardAnimator();
                if (anim == null) return true;

                int row = anim.yToRow(event.getY());
                int col = anim.xToCol(event.getX());

                if (state.getCurrentPhase() == GamePhase.SETUP) {
                    // place the selected painting or camera on the tapped tile
                    if (selectedPaintingId == -1 && selectedCameraId == -1) return true;
                    if (selectedPaintingId != -1) {
                        game.sendAction(new MuseumCaperPlacePaintingAction(
                                MuseumCaperHumanPlayer.this, selectedPaintingId, row, col));
                    } else {
                        game.sendAction(new MuseumCaperPlaceCameraAction(
                                MuseumCaperHumanPlayer.this, selectedCameraId, row, col));
                    }
                    // dim and disable the placed piece so it can't be moved again
                    if (selectedPieceView != null) {
                        selectedPieceView.setAlpha(0.3f);
                        selectedPieceView.setEnabled(false);
                        selectedPieceView = null;
                    }
                    selectedPaintingId = -1;
                    selectedCameraId   = -1;

                } else if (state.getCurrentPhase() == GamePhase.GUARD_MOVE) {
                    // move the guard dot to the tapped tile after rolling
                    game.sendAction(new MuseumCaperGuardMoveAction(
                            MuseumCaperHumanPlayer.this, 0, row, col));
                }
                return true;
            });
        }

        // Done Setup button: signals end of setup, triggers thief's first move
        Button doneSetupButton = myActivity.findViewById(R.id.doneSetupButton);
        if (doneSetupButton != null) {
            doneSetupButton.setOnClickListener(v -> {
                if (state != null && state.getCurrentPhase() == GamePhase.SETUP) {
                    game.sendAction(new MuseumCaperFinishSetupAction(
                            MuseumCaperHumanPlayer.this));
                }
            });
        }

        if (state != null) {
            receiveInfo(state);
        }
    }

    /** @return this player's assigned player number from the framework */
    @Override
    public int getPlayerNum() {
        return this.playerNum;
    }

    /** @return this player's display name */
    @Override
    public String toString() {
        return this.name;
    }
}