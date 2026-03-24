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
	protected void updateDisplay() {
        //
        if(state == null || playerTurnTextView == null || myActivity == null)
        {
            return;
        }
        // get player name from framework
        LocalGame local = myActivity.getGame();
        GamePlayer[] players = local.getPlayers();

        //determine who's turn it is
        int turn = state.getPlayerTurn();

        // safety check
        if(turn < 0 || turn >= players.length)
        {
            playerTurnTextView.setText("Unknown Player's Turn.");
            return;
        }
        String name = players[turn].getName();

		// set the text in the appropriate widget -- adjust player turn
        playerTurnTextView.setText(name + "'s Turn");
	}

	/**
	 * this method gets called when the user clicks the '+' or '-' button. It
	 * creates a new CounterMoveAction to return to the parent activity.
	 *
	 * @param button
	 * 		the button that was clicked
	 */
	public void onClick(View button) {
		// if we are not yet connected to a game, ignore
		if (game == null) return;
//        int row = clickedRow;
//        int col = clickedCol;
//        int guardIndex = getPlayerNum() - 1;
//        if(guardIndex >= 0)
//        {
//            game.sendAction(new MuseumCaperGuardMoveAction(this, guardIndex, row,col));
//        }
        GameAction cameraAction;


//	    NO LONGER RELEVANT?
		if (button.getId() == R.id.cameradie) {
			// camer die button :
			MuseumCaperRollDiceAction action = new MuseumCaperRollDiceAction(this, DiceType.QUESTION);
            game.sendAction(action);
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
		if (!(info instanceof MuseumCaperState)) return;

		// update our state; then update the display
		this.state = (MuseumCaperState)info;
		updateDisplay();
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

		// make this object the listener for both the '+' and '-' 'buttons
//	    IRRELEVANT
        ImageView cameraDieButton = myActivity.findViewById(R.id.cameradie);
        cameraDieButton.setOnClickListener(this);

        playerTurnTextView = myActivity.findViewById(R.id.turnInfo);

		// if we have a game state, "simulate" that we have just received
		// the state from the game so that the GUI values are updated
		if (state != null) {
			receiveInfo(state);
		}
	}//blah blah

    @Override
    public int getPlayerNum() {
        return 0;
    }

}// class CounterHumanPlayer

