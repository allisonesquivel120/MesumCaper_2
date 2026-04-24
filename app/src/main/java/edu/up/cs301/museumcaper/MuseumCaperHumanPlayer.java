package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.players.GameHumanPlayer;
import edu.up.cs301.GameFramework.GameMainActivity;
import edu.up.cs301.GameFramework.infoMessage.GameInfo;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PixelFormat;
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
    //TEMP flag:
    private boolean questionPopupShowing = false;
    private boolean answerPopupShowing = false;
    private ImageView selectedPieceView = null;
    private GameMainActivity myActivity;
    private TextView playerTurnTextView;
    private ImageButton movementDieButton;
    private ImageButton cameraDieButton;
    // tracks how many cameras have been successfully placed during setup
    private int camerasPlaced = 0;
    // prevents multiple placements from single tap
    private boolean canPlace = true;
    // tracks which camera have already been used [index = cameraId - 1]
    private boolean[] cameraUsed = new boolean[6];
    // prevent multiple clicks per turn
    private boolean movementDieUsed = false;
    private boolean cameraDieUsed = false;
    private GamePhase lastPhase = null;
    private static final int MAIN_GUARD = 0;


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

        GamePhase currentPhase = state.getCurrentPhase();
        // reset only when entering GUARD_TURN_START
        if (currentPhase == GamePhase.GUARD_TURN_START &&
                lastPhase != GamePhase.GUARD_TURN_START) {
            movementDieUsed = false;
            cameraDieUsed = false;
        }
        lastPhase = currentPhase;

        // update turn indicator text
        if (playerTurnTextView != null) {
            int turn = state.getPlayerTurn();
            playerTurnTextView.setText(turn == 0 ? "Thief's Turn" : this.name + "'s Turn");
        }
        boolean isGuardTurnStart = state.getCurrentPhase() == GamePhase.GUARD_TURN_START;

        // movement die
        if (movementDieButton != null) {
            boolean used = state.isMovementDieUsed();
            int roll = state.getMovementRoll();
            movementDieButton.setImageResource(getMovementDieDrawable(roll));

            movementDieButton.setEnabled(!used);
            movementDieButton.setAlpha(used ? 0.4f : 1.0f);
        }
        // camera die
        if (cameraDieButton != null) {
            boolean used = state.isQuestionDieUsed();
            int roll = state.getQuestionRoll();
            cameraDieButton.setImageResource(getCameraDieDrawable(roll));

            cameraDieButton.setEnabled(!used);
            cameraDieButton.setAlpha(used ? 0.4f : 1.0f);
        }
        // show done setup button only during SETUP phase
        Button doneSetupButton = myActivity.findViewById(R.id.doneSetupButton);
        if (doneSetupButton != null) {
            boolean inSetup = state.getCurrentPhase() == GamePhase.SETUP;
            doneSetupButton.setVisibility(inSetup ? View.VISIBLE : View.GONE);
        }
        // show question popup when detective has rolled the question die
        if (state.getCurrentPhase() == GamePhase.GUARD_ASK && !questionPopupShowing) {
            questionPopupShowing = true;
            showQuestionPopup();
        }
        // show answer popup when AI thief has answered
        if (state.getCurrentPhase() == GamePhase.DETECTIVE_REVEAL && !answerPopupShowing) {
            answerPopupShowing = true;
            showAnswerPopup();
        }
        // lock paintings after setup ends so they can't be moved
        int[] paintingViewIds = {
                R.id.painting1, R.id.painting2, R.id.painting3,
                R.id.painting4, R.id.painting5, R.id.painting6,
                R.id.painting7, R.id.painting8, R.id.painting9
        };
        for (int i = 0; i < paintingViewIds.length; i++) {

            ImageView v = myActivity.findViewById(paintingViewIds[i]);
            if (v == null) continue;

            boolean placed = state.isPaintingPlaced(i + 1);

            if (placed) {
                v.setAlpha(0.3f);   // locked [gray] - already placed
                v.setEnabled(false);
            } else {
                boolean inSetup = state.getCurrentPhase() == GamePhase.SETUP;
                v.setAlpha(1.0f);   // paintings still available
                v.setEnabled(inSetup);
            }
        }
        // lock cameras after setup ends so they can't be moved
        int[] cameraViewIds = {
                R.id.offcamera, R.id.offcamera1, R.id.oncamera4,
                R.id.oncamera1, R.id.oncamera2, R.id.oncamera3
        };
        for (int i = 0; i < cameraViewIds.length; i++) {
            ImageView v = myActivity.findViewById(cameraViewIds[i]);
            if (v == null) continue;

            boolean inSetup = state.getCurrentPhase() == GamePhase.SETUP;
            // camera i is placed if i < cameraCount
            boolean placed = (i < state.getCameraCount());
            cameraUsed[i] = placed;

            if (placed) {
                v.setAlpha(0.3f); // locked [gray] - already placed
                v.setEnabled(false);
            } else {
                v.setAlpha(1.0f); // cameras still available
                v.setEnabled(inSetup);
            }
        }
        // refresh the board so tiles, guard, thief, cameras, and paintings redraw
        if (boardSurfaceView != null) {
            boardSurfaceView.setState(state);
            //TODO: boardSurfaceView.setDetectiveIcons(getDetectiveIcons());
        }
    } // updateDisplay
    /**
     * Returns the drawable resource for the detective figure
     * TODO: Implement this method
     */
//    private int[] getDetectiveIcons() {
//        // returns drawable IDs for each detective
//        // Example: different colors/icons per detective
//        return new int[]{
//                R.drawable.detective_red,
//                R.drawable.detective_blue,
//                R.drawable.detective_green,
//                R.drawable.detective_yellow
//        };
//    }

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
    } // getMovementDieDrawabel

    /**
     * Returns the drawable resource for the camera/question die face.
     * @param roll the rolled value (1-6), defaults to eye die if unrolled
     */
    private int getCameraDieDrawable(int roll) {
        switch (roll) {
            case 1: return R.drawable.eyedie_m;  // 1-2 = MOTION
            case 2: return R.drawable.eyedie_m;
            case 3: return R.drawable.eyedie_s;  // 3-4 = SCAN
            case 4: return R.drawable.eyedie_s;
            case 5: return R.drawable.eyedie2; // 5-6 = EYE
            case 6: return R.drawable.eyedie2;
            default: return R.drawable.eyedie2;   // unrolled state
        }
    } // getCameraDieDrawable

    /**
     * Handles clicks on the movement and camera dice buttons.
     * @param button the button that was clicked
     */
    @Override
    public void onClick(View button) {
        if (game == null || state == null) return;

        // only allow clicks at start of guard turn
        if (state.getCurrentPhase() != GamePhase.GUARD_TURN_START) return;
        if (button.getId() == R.id.regulardie) {
            if (movementDieUsed) return;
            movementDieUsed = true;
            movementDieButton.setAlpha(0.4f);
            movementDieButton.setEnabled(false);
            game.sendAction(new MuseumCaperRollDiceAction(this, DiceType.MOVEMENT));
        } else if (button.getId() == R.id.cameradie) {
            if (cameraDieUsed) return;
            cameraDieUsed = true;
            cameraDieButton.setAlpha(0.4f);
            cameraDieButton.setEnabled(false);
            game.sendAction(new MuseumCaperRollDiceAction(this, DiceType.QUESTION));
        }
    }// onClick

    /**
     * Receives a new game state from the local game and refreshes the display.
     * @param info the game info object (expected to be MuseumCaperState)
     */
    @Override
    public void receiveInfo(GameInfo info) {
        if (!(info instanceof MuseumCaperState)) return;
        this.state = (MuseumCaperState) info;
        // update board visuals first
        updateDisplay();
    } // receiveInfo

    /**
     * Shows a popup dialog with the full game rules.
     * Content matches the rules from the project presentation.
     */
    private void showRulesDialog() {
        String rules =
                "OBJECTIVE\n" +
                        "• Thief: Steal at least 3 paintings.\n" +
                        "• Detective: Stop the thief by landing on their space.\n\n" +

                        "SETUP\n" +
                        "• Place paintings and cameras on the board.\n" +
                        "• Tap a painting or camera to select it, then tap a tile to place it.\n" +
                        "• Press 'Done Setting Up' when ready.\n\n" +

                        "THIEF'S TURN\n" +
                        "• The thief moves automatically 1–3 spaces.\n" +
                        "• The thief avoids cameras and can disable them by stepping on them.\n" +
                        "• The thief can steal paintings on their tile.\n\n" +

                        "DETECTIVE'S TURN\n" +
                        "• Roll the movement die — move up to that many spaces.\n" +
                        "• Tap a tile on the board to move there.\n" +
                        "• Landing on the thief's space is an INSTANT WIN!\n\n" +

                        "CAMERA DIE\n" +
                        "• MOTION (M): Ask what color room the thief is in.\n" +
                        "• SCAN (S): Ask if any cameras can see the thief.\n" +
                        "• EYE: Ask if you can see the thief — if yes, thief becomes visible.\n\n" +

                        "WIN CONDITIONS\n" +
                        "• Thief wins by stealing 3 or more paintings.\n" +
                        "• Detective wins by landing on the thief's tile.";

        new android.app.AlertDialog.Builder(myActivity)
                .setTitle("Game Rules")
                .setMessage(rules)
                .setPositiveButton("Got it!", null)
                .show();
    } // showRulesDialog

    /**
     * Shows a popup telling the detective which question to ask based on
     * the question die roll. Automatically determines question type from
     * the rolled value and sends the ask action to get the AI answer.
     */
    private void showQuestionPopup() {
        int roll = state.getQuestionRoll();

        // map die roll to question type
        // 1-2 = MOTION, 3-4 = SCAN, 5-6 = EYE
        QuestionType questionType;
        String questionText;
        if (roll <= 2) {
            questionType = QuestionType.MOTION;
            questionText = "MOTION\n\nAsk the thief:\n\"What color room are you in?\"";
        } else if (roll <= 4) {
            questionType = QuestionType.SCAN;
            questionText = "SCAN\n\nAsk the thief:\n\"Are all cameras working?\"\n\"Can any camera see you?\"";
        } else {
            questionType = QuestionType.EYE;
            questionText = "EYE\n\nAsk the thief:\n\"Can I see you?\"";
        }

        final QuestionType finalType = questionType;
        new android.app.AlertDialog.Builder(myActivity)
                .setTitle("Question Die — Roll: " + roll)
                .setMessage(questionText)
                .setCancelable(false)
                .setPositiveButton("Ask!", (dialog, which) -> {
                    questionPopupShowing = false;
                    game.sendAction(new MuseumCaperAskQuestionAction(
                            MuseumCaperHumanPlayer.this, finalType));
                })
                .show();
    } // showQuestionPopup

    /**
     * Shows the detective the AI thief's automatic answer.
     * After dismissing, sends FinishRevealAction to trigger the thief's turn.
     */
    private void showAnswerPopup() {
        String answer = state.getLastQuestionAnswer();
        new android.app.AlertDialog.Builder(myActivity)
                .setTitle("Thief's Answer")
                .setMessage(answer)
                .setCancelable(false)
                .setPositiveButton("Got it!", (dialog, which) -> {
                    answerPopupShowing = false;
                    game.sendAction(new MuseumCaperFinishRevealAction(
                            MuseumCaperHumanPlayer.this));
                })
                .show();
    } // showAnswerPopup

    /**
     * Sets up the GUI when this player becomes the active GUI player.
     * Wires up all interactive elements: dice buttons, board touch listener,
     * painting/camera tap-to-select, and the Done Setup button.
     *
     * @param activity the main game activity
     */
    @SuppressLint("ClickableViewAccessibility")
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
        boardSurfaceView.setBackgroundColor(Color.TRANSPARENT);

        /** EXTERNAL CITATION
         * ChatGPT
         * 17 April 2026
         *
         * Problem: Difficulties making the corners of the board transparent.
         * Solution: Making the board see-through, putting it over the background
         * image, and supporting transparency
         */

        boardSurfaceView.setZOrderOnTop(true);
        boardSurfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);

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
                R.id.oncamera1, R.id.oncamera2, R.id.oncamera3
        };


        for (int i = 0; i < cameraViewIds.length; i++) {
            final int cameraId = i + 1; // identifies which camera

           ImageView v = myActivity.findViewById(cameraViewIds[i]);
            if (v == null) continue;

            v.setOnClickListener(view -> {
                if (state == null || state.getCurrentPhase() != GamePhase.SETUP) return;
                // deselect previous
                if (selectedPieceView != null) {
                    selectedPieceView.setAlpha(1.0f);
                }
                // select this camera
                selectedCameraId = cameraId;
                selectedPaintingId = -1; // makes sure only 1 type is selected

                selectedPieceView = (ImageView) view;
                selectedPieceView.setAlpha(0.5f); // visual feedback
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
                    // prevent multiple triggers from one tap
                    if (!canPlace) return true;
                    // place the selected painting or camera on the tapped tile
                    if (selectedPaintingId == -1 && selectedCameraId == -1) return true;
                    // only lock when placed
                    canPlace = false;

                    if (selectedPaintingId != -1) {
                        game.sendAction(new MuseumCaperPlacePaintingAction(
                                MuseumCaperHumanPlayer.this, selectedPaintingId, row, col));
                        selectedPaintingId = -1;
                        selectedPieceView = null;
                    } else {
                        if (selectedCameraId < 1 || selectedCameraId > 6) return true;
                        if (cameraUsed[selectedCameraId - 1]) return true; // already used

                        game.sendAction(new MuseumCaperPlaceCameraAction(
                            MuseumCaperHumanPlayer.this, selectedCameraId, row, col));

                        camerasPlaced++;
                }
                    // reset selection immediately (UI state only)
                    if (selectedPieceView != null) {
                        selectedPieceView.setAlpha(1.0f); // keep neutral until state confirms
                        selectedPieceView.setEnabled(false);
                        selectedPieceView = null;
                    }
                    selectedPaintingId = -1;
                    selectedCameraId = -1;
                    // small delay to stop double triggering
                    v.postDelayed(() -> canPlace = true, 150);

                } else if (state.getCurrentPhase() == GamePhase.GUARD_MOVE) {
                    //int guardIndex = getPlayerNum() - 1;
                    int guardIndex = MAIN_GUARD;
                    // move the guard dot to the tapped tile after rolling
                    game.sendAction(new MuseumCaperGuardMoveAction(
                            MuseumCaperHumanPlayer.this, guardIndex, row, col));
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

        // rules button: shows a popup with game rules
        Button rulesButton = myActivity.findViewById(R.id.rulesBotton);
        if (rulesButton != null) {
            rulesButton.setOnClickListener(v -> showRulesDialog());
        }

        if (state != null) {
            receiveInfo(state);
        }
    } // setAsGui

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