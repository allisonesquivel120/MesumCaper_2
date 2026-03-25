package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.LocalGame;
import edu.up.cs301.GameFramework.players.GameHumanPlayer;
import edu.up.cs301.GameFramework.GameMainActivity;
import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.infoMessage.GameInfo;
import edu.up.cs301.GameFramework.Game;
import edu.up.cs301.GameFramework.players.GamePlayer;

import android.view.View;
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

    private ImageButton movementDieButton = null;

    private ImageButton cameraDieButton = null;


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
	public View getTopView() {
		return myActivity.findViewById(R.id.main_MuseumCaper);
	}

	/**
	 * sets the counter value in the text view
	 */
    // maybe omit
	protected void updateDisplay() {
        //
        if(state == null || myActivity == null) return;

        if (playerTurnTextView != null) {
            int turn = state.getPlayerTurn();
            // turn 0 = thief , turn 1+ = human
            if (turn == 0) {
                playerTurnTextView.setText("Thief's Turn");
            } else {
                playerTurnTextView.setText(this.name + "'s Turn");
            }
        }
        LocalGame local = (LocalGame) game;
        GamePlayer[] players = local.getPlayers();

        //determine who's turn it is
        int turn = state.getPlayerTurn();

        // safety check
        if(turn < 0 || turn >= players.length)
        {
            playerTurnTextView.setText("Unknown Player's Turn.");
            return;
        }
        String name = players[turn].toString();

        // set the text in the appropriate widget -- adjust player turn
        playerTurnTextView.setText(name + "'s Turn");

//        if (movementDieButton != null) {
//            int roll = state.getMovementRoll();
//
//            //movementDieButton.setImageResource(getDieDrawable(roll, false));
//            // only clickable during GUARD_ROLL on the guard's turn
////            boolean canRollMovement = state.getCurrentPhase() == GamePhase.GUARD_ROLL
////                    && state.getPlayerTurn() == getPlayerNum();
////            movementDieButton.setEnabled(canRollMovement);
////            movementDieButton.setAlpha(canRollMovement ? 1.0f : 0.4f);
//        }

        if (cameraDieButton != null) {
            int roll = state.getQuestionRoll();
            // camera die but not quite yet
            //cameraDieButton.setImageResource(getDieDrawable(roll, true));
            boolean canRollQuestion = state.getCurrentPhase() == GamePhase.GUARD_QUESTION
                    && state.getPlayerTurn() == getPlayerNum();
            cameraDieButton.setEnabled(canRollQuestion);
            cameraDieButton.setAlpha(canRollQuestion ? 1.0f : 0.4f);
        }

        GamePhase phase = state.getCurrentPhase();

//        if (movementDieButton != null) {
//            movementDieButton.setEnabled(phase == GamePhase.GUARD_ROLL
//                    && state.getPlayerTurn() == getPlayerNum());
//        }
        if (cameraDieButton != null) {
            cameraDieButton.setEnabled(phase == GamePhase.GUARD_QUESTION
                    && state.getPlayerTurn() == getPlayerNum());
        }

    }

		// set the text in the appropriate widget -- adjust player turn
        //playerTurnTextView.setText(name + "'s Turn");


    /**
	 * this method gets called when the user clicks the '+' or '-' button. It
	 * creates a new CounterMoveAction to return to the parent activity.
	 *
	 * @param button
	 * 		the button that was clicked
	 */
	public void onClick(View button) {
		// if we are not yet connected to a game, ignore
		// perchance if (game == null) return;
//        int row = clickedRow;
//        int col = clickedCol;
//        int guardIndex = getPlayerNum() - 1;
//        if(guardIndex >= 0)
//        {
//            game.sendAction(new MuseumCaperGuardMoveAction(this, guardIndex, row,col));
//        }
        // perchance GameAction cameraAction;


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
		// ignore the message if it's not a CounterState message
		if (!(info instanceof MuseumCaperState)) { return; };
        // set textviews

        if (state.getDiceValue() == 1) {
            movementDieButton.setImageResource(R.drawable.basedie1);
        } else if (state.getDiceValue() == 2) {
            movementDieButton.setImageResource(R.drawable.basedie2);
        } else if (state.getDiceValue() == 3) {
            movementDieButton.setImageResource(R.drawable.basedie3);
        } else if (state.getDiceValue() == 4) {
            movementDieButton.setImageResource(R.drawable.basedie4);
        } else if (state.getDiceValue() == 5) {
            movementDieButton.setImageResource(R.drawable.basedie5);
        } else if (state.getDiceValue() == 6) {
            movementDieButton.setImageResource(R.drawable.basedie6);
        }

		// update our state; then update the display
		//this.state = (MuseumCaperState)info;
        //check error for following line! What does it mean counter state?

        // perchance
        // state = new MuseumCaperState((MuseumCaperState) info);

		// perchance
        // updateDisplay();
	}

	/**
     * callback method--our game has been chosen/rechosen to be the GUI,
	 * called from the GUI thread
	 *
	 * @param activity
	 * 		the activity under which we are running
	 */
	public void setAsGui(GameMainActivity activity) {

		// remember the activity
		this.myActivity = activity;

	    // Load the layout resource for our GUI
		activity.setContentView(R.layout.museumcaper_human_player);
        // testing commit changes

        this.playerTurnTextView = (TextView)myActivity.findViewById(R.id.turnInfo);
        this.movementDieButton = (ImageButton)myActivity.findViewById(R.id.regulardie);
        this.cameraDieButton = (ImageButton)myActivity.findViewById(R.id.cameradie);


        // make both dice clickable
        movementDieButton.setOnClickListener(this);
        cameraDieButton.setOnClickListener(this);

        // also make the die ImageViews clickable in the XML sense
        //perchance uncomment, let's see it it does anythign!
//        movementDieButton.setClickable(true);
//        cameraDieButton.setClickable(true);

		// if we have a game state, "simulate" that we have just received
		// the state from the game so that the GUI values are updated
        //perchance
//		if (state != null) {
//			receiveInfo(state);
//		}
	}//blah blah

    @Override
    public int getPlayerNum() {
        return this.playerNum;
    }

    // overrriding the toString
    @Override
    public String toString() {
        return this.name;
    }

}// class CounterHumanPlayer

