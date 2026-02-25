package edu.up.cs301.museumcaper;

import java.util.ArrayList;

import edu.up.cs301.GameFramework.infoMessage.GameState;


/**
 * This contains the state for the Museum Caper game. The state consist of simply
 * the value of the counter.
 * 
 * @author Farid, Jayden, and Allison
 * @version Feb. 2026
 */
public class MuseumCaperState extends GameState {
	
	// to satisfy Serializable interface
	private static final long serialVersionUID = 7737393762469851826L;

    /**
     * instance variables
     */
    // player + turn info
    private int playerTurn;
    private int numPlayers;
    private GamePhase currentPhase;

    // thief info
    private int thiefRoomId; // the current room thief is inside [ detectives can't see this ]
    private boolean thiefVisible;
    private ArrayList<Integer> stolenPaintings; // list of paintings the thief as stolen

    // detective info
    private int[] detectiveRoomId; // detective's current room location

    // board info
    private Room[] rooms;
    // private int[][]adjacecyMatrix; which rooms connect to which

    // die info
    private int[] diceValues;

    // game status
    private boolean gameOver;
    private int winnerId; // -1 = no ones won yet

	
	/**
	 * constructor, initializing the counter value from the parameter
	 * 
	 * @param
	 * 		the value to which the counter's value should be initialized
	 */
	public MuseumCaperState(int numPlayers)
    {
		this.numPlayers = numPlayers;
        this.playerTurn = 0; // thief always starts
        this.currentPhase = GamePhase.SETUP;

        // thief
        this.thiefRoomId = 0;
        this.thiefVisible = false;
        this.stolenPaintings = new ArrayList<>();

        // detectives
        this.detectiveRoomId = new int[numPlayers - 1];

        // board
//        this.room = new Room[20];
//        for(int i =0; i < 20; i++)
//        {
//            this.room[i] = new Room(i);
//        }
	}
	
	/**
	 * copy constructor; makes a copy of the original object
	 * 
	 * @param orig
	 * 		the object from which the copy should be made
	 */


	public MuseumCaperState(MuseumCaperState orig) {
		// set the counter to that of the original
	}

	/**
	 * getter method for the counter
	 * 
	 * @return
	 * 		the value of the counter
	 */
	public int getCounter() {
		return counter;
	}
	
	/**
	 * setter method for the counter
	 * 
	 * @param counter
	 * 		the value to which the counter should be set
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}
}
