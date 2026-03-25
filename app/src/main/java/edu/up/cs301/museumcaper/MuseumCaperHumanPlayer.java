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
	 * this method gets called when the user clicks the '+' or '-' button. It
	 * creates a new CounterMoveAction to return to the parent activity.
	 *
	 * @param button
	 * 		the button that was clicked
	 */
	public void onClick(View button) {
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

            // update our state; then update the display
            //this.state = (MuseumCaperState)info;
        }
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
		myActivity = activity;

	    // Load the layout resource for our GUI
        activity.setContentView(R.layout.museumcaper_human_player);

        //Initialize the widget reference member variables
        this.playerTurnTextView = (TextView)activity.findViewById(R.id.turnInfo);
        this.movementDieButton = (ImageButton)activity.findViewById(R.id.regulardie);
        this.cameraDieButton = (ImageButton)activity.findViewById(R.id.cameradie);

        // make both dice clickable
        movementDieButton.setOnClickListener(this);
        cameraDieButton.setOnClickListener(this);

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
//

