package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.LocalGame;
import edu.up.cs301.GameFramework.players.GameHumanPlayer;
import edu.up.cs301.GameFramework.GameMainActivity;
import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.infoMessage.GameInfo;
import edu.up.cs301.GameFramework.Game;
import edu.up.cs301.GameFramework.players.GamePlayer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;


/**
 * A GUI of a counter-player. The GUI displays the current value of the counter,
 * and allows the human player to press the '+' and '-' buttons in order to
 * send moves to the game.
 *
 * Just for fun, the GUI is implemented so that if the player presses either button
 * when the counter-value is zero, the screen flashes briefly, with the flash-color
 * being dependent on whether the player is player 0 or player 1.
 *
 * @author Steven R. Vegdahl
 * @author Andrew M. Nuxoll
 * @version July 2013
 */
public class MuseumCaperHumanPlayer extends GameHumanPlayer implements OnClickListener {

	/* instance variables */

	// The TextView the displays the current counter value
	private TextView playerTurnTextView;

	// the most recent game state, as given to us by the CounterLocalGame
	private MuseumCaperState state;

	// the android activity that we are running
	private GameMainActivity myActivity;

    private ImageButton movementDieButton;

    private ImageButton cameraDieButton;


	/**
     * constructor
     *
     * @param name the player's name
     * @param i
     */
	public MuseumCaperHumanPlayer(String name, int i) {
        super(name);
    }

	/**
	 * Returns the GUI's top view object
	 *
	 * @return
	 * 		the top object in the GUI's view heirarchy
	 */
    @Override
	public View getTopView() {
		return myActivity.findViewById(R.id.main_MuseumCaper);
	}

    protected void updateDisplay() {
        if (state == null || myActivity == null) return;

        // --- whose turn is it ---
        if (playerTurnTextView != null) {
            int turn = state.getPlayerTurn();
            // turn 0 = thief (AI), turn 1+ = guard (human)
            if (turn == 0) {
                playerTurnTextView.setText("Thief's Turn");
            } else {
                playerTurnTextView.setText(this.name + "'s Turn");
            }
        }
// --- movement die image ---
        if (movementDieButton != null) {
            int roll = state.getMovementRoll();
            movementDieButton.setImageResource(getMovementDieDrawable(roll));
            boolean canRoll = state.getCurrentPhase() == GamePhase.GUARD_ROLL
                    && state.getPlayerTurn() == getPlayerNum();
            movementDieButton.setEnabled(canRoll);
            movementDieButton.setAlpha(canRoll ? 1.0f : 0.4f);
        }

        // --- camera die image ---
        if (cameraDieButton != null) {
            int roll = state.getQuestionRoll();
            cameraDieButton.setImageResource(getCameraDieDrawable(roll));
            boolean canRoll = state.getCurrentPhase() == GamePhase.GUARD_QUESTION
                    && state.getPlayerTurn() == getPlayerNum();
            cameraDieButton.setEnabled(canRoll);
            cameraDieButton.setAlpha(canRoll ? 1.0f : 0.4f);
        }

    }
    private int getMovementDieDrawable(int roll) {
        switch (roll) {
            case 1: return R.drawable.basedie1;
            case 2: return R.drawable.basedie2;
            case 3: return R.drawable.basedie3;
            case 4: return R.drawable.basedie4;
            case 5: return R.drawable.basedie5;
            case 6: return R.drawable.basedie6;
            default: return R.drawable.basedie1; // unrolled state
        }
    }

    private int getCameraDieDrawable(int roll) {
        switch (roll) {
            case 1:
                return R.drawable.basedie1;
            case 2:
                return R.drawable.basedie2;
            case 3:
                return R.drawable.basedie3;
            case 4:
                return R.drawable.basedie4;
            case 5:
                return R.drawable.basedie5;
            case 6:
                return R.drawable.basedie6;
            default:
                return R.drawable.eyedie2; // unrolled state
        }
    }
    /**
	 *
	 * @param button
	 * 		the button that was clicked
	 */
	public void onClick(View button) {
        if (game == null) return;

        if (button.getId() == R.id.regulardie) {
            // send a MOVEMENT dice roll action
            game.sendAction(new MuseumCaperRollDiceAction(this, DiceType.MOVEMENT));
        }
        else if (button.getId() == R.id.cameradie) {
            // send a QUESTION dice roll action
            game.sendAction(new MuseumCaperRollDiceAction(this, DiceType.QUESTION));
        }
	}// onClick

	/**
	 * callback method when we get a message (e.g., from the game)
	 *
	 * @param info
	 * 		the message
	 */
	@Override
	public void receiveInfo(GameInfo info) {
            if (!(info instanceof MuseumCaperState)) return;
            this.state = (MuseumCaperState) info;
            updateDisplay();
		/*
		if (info instanceof MuseumCaperState) {
            MuseumCaperState newState = new MuseumCaperState((MuseumCaperState) info);

            if (newState.getDiceValue() == 1) {
                movementDieButton.setImageResource(R.drawable.basedie1);
            } else if (newState.getDiceValue() == 2) {
                movementDieButton.setImageResource(R.drawable.basedie2);
            } else if (newState.getDiceValue() == 3) {
                movementDieButton.setImageResource(R.drawable.basedie3);
            } else if (newState.getDiceValue() == 4) {
                movementDieButton.setImageResource(R.drawable.basedie4);
            } else if (newState.getDiceValue() == 5) {
                movementDieButton.setImageResource(R.drawable.basedie5);
            } else if (newState.getDiceValue() == 6) {
                movementDieButton.setImageResource(R.drawable.basedie6);
            }
		 */
	}

	/**
     * callback method--our game has been chosen/rechosen to be the GUI,
	 * called from the GUI thread
	 *
	 * @param activity
	 * 		the activity under which we are running
	 */
    public void setAsGui(GameMainActivity activity) {
        myActivity = activity;
        activity.setContentView(R.layout.museumcaper_human_player);

        // --- find board & layers ---
        ImageView boardImage = activity.findViewById(R.id.gameBoardImageView);
        ViewGroup piecesLayer = activity.findViewById(R.id.piecesLayer);

        // creates a drag listener for all pieces
        BoardDragTouchListener dragListener = new BoardDragTouchListener(piecesLayer, boardImage, null);

        // wire up instance variables (NOT local variables)
        playerTurnTextView = myActivity.findViewById(R.id.turnInfo);
        movementDieButton = myActivity.findViewById(R.id.regulardie);  // matches layout ID
        cameraDieButton = myActivity.findViewById(R.id.cameradie);     // matches layout ID

        movementDieButton.setOnClickListener(this);
        cameraDieButton.setOnClickListener(this);

        // --- FIND ALL DRAGGABLE PIECES ---
        ImageView camera1 = activity.findViewById(R.id.oncamera1);
        ImageView camera2 = activity.findViewById(R.id.oncamera2);
        ImageView camera3 = activity.findViewById(R.id.oncamera3);
        ImageView camera4 = activity.findViewById(R.id.oncamera4);

        ImageView p1 = activity.findViewById(R.id.painting1);
        ImageView p2 = activity.findViewById(R.id.painting2);
        ImageView p3 = activity.findViewById(R.id.painting3);
        ImageView p4 = activity.findViewById(R.id.painting4);
        ImageView p5 = activity.findViewById(R.id.painting5);
        ImageView p6 = activity.findViewById(R.id.painting6);
        ImageView p7 = activity.findViewById(R.id.painting7);
        ImageView p8 = activity.findViewById(R.id.painting8);
        ImageView p9 = activity.findViewById(R.id.painting9);

        ImageView yellowPawn = activity.findViewById(R.id.yellow_Pawn);

        View[] draggables = {
                camera1, camera2, camera3, camera4,
                p1, p2, p3, p4, p5, p6, p7, p8, p9,
                yellowPawn
        };

        for (View v : draggables) {
            if (v != null) {
                v.setOnTouchListener(dragListener);
            }
        }

        if (state != null) {
            receiveInfo(state);
        }

        piecesLayer.bringToFront();
    }//blah blah

    @Override
    public int getPlayerNum() {
        return this.playerNum;
    }

    // overriding the toString
    @Override
    public String toString() {
        return this.name;
    }
}// class MuseumCaperHumanPlayer


