package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.players.GameComputerPlayer;
import edu.up.cs301.GameFramework.infoMessage.GameInfo;
import edu.up.cs301.GameFramework.utilities.Tickable;

/**
 * old computer player class for counter game
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
public class MuseumCaperComputerPlayer1 extends GameComputerPlayer implements Tickable {
	
    /**
     * Constructor for objects of class CounterComputerPlayer1
     * 
     * @param name
     * 		the player's name
     */
    public MuseumCaperComputerPlayer1(String name) {
        // invoke superclass constructor
        super(name);
        
        // start the timer, ticking 20 times per second
        getTimer().setInterval(50);
        getTimer().start();
    }
    
    /**
     * callback method--game's state has changed
     * 
     * @param info
     * 		the information (presumably containing the game's state)
     */
	@Override
	protected void receiveInfo(GameInfo info) {
		// Do nothing, as we ignore all state in deciding our next move. It
		// depends totally on the timer and random numbers.
	}
	
	/**
	 * callback method: the timer ticked
	 */
	protected void timerTicked() {
		// 5% of the time, increment or decrement the counter
		if (Math.random() >= 0.05) return; // do nothing 95% of the time

		// "flip a coin" to determine whether to increment or decrement
		int targetRow = (int)(Math.random() *8);
        int targetCol = (int)(Math.random() * 8);
		
		// send the move-action to the game
		game.sendAction(new MuseumCaperGuardMoveAction(this, targetRow, targetCol));
	}

    @Override
    public int getPlayerNum() {
        return 0;
    }
}
