package edu.up.cs301.museumcaper;

import java.util.ArrayList;
import java.util.Arrays;

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

    // alarm system
    private boolean[] alarmTriggered;

    // board info
    private Room[] room;

    // die info
    private int[] diceValues;

    // game status
    private boolean gameOver;
    private int winnerId; // -1 = no ones won yet

    private char[][] gameBoard;

    public MuseumCaperState()
    {
        this(3);
    }

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
        int numDetectives = Math.max(0, numPlayers -1);
        this.detectiveRoomId = new int[numPlayers - 1];
        for (int i = 0; i < detectiveRoomId.length; i++) {
            detectiveRoomId[i] = 6; // assigns each detective a starting room
        }

        // rooms
        this.room = new Room[7];
        for(int i =0; i < room.length; i++)
        {
            room[i] = new Room(i);
        }
        // board
        this.gameBoard = new char[][]
                {
                        {'t', 't', 't', 'r', 'r', 'r', 'r', 'r', 'r', 't', 't', 't'},
                        {'t', 't', 't', 'r', 'r', 'r', 'r', 'r', 'r', 't', 't', 't'},
                        {'p', 'p', 'p', 'h', 'h', 'h', 'h', 'h', 'h', 'b', 'b', 'b'},
                        {'p', 'p', 'p', 'h', 'w', 'w', 'w', 'w', 'h', 'b', 'b', 'b'},
                        {'p', 'p', 'p', 'h', 'w', 'w', 'w', 'w', 'h', 'b', 'b', 'b'},
                        {'p', 'p', 'p', 'h', 'w', 'w', 'w', 'w', 'h', 'h', 'h', 'h'},
                        {'h', 'h', 'h', 'h', 'w', 'w', 'w', 'w', 'h', 'g', 'g', 'g'},
                        {'y', 'y', 'y', 'h', 'w', 'w', 'w', 'w', 'h', 'g', 'g', 'g'},
                        {'y', 'y', 'y', 'h', 'w', 'w', 'w', 'w', 'h', 'g', 'g', 'g'},
                        {'y', 'y', 'y', 'h', 'h', 'h', 'h', 'h', 'h', 'g', 'g', 'g'},
                        {'t', 't', 't', 'd', 'd', 'h', 'h', 'h', 'h', 't', 't', 't'},
                        {'t', 't', 't', 'd', 'd', 'h', 'h', 'h', 'h', 't', 't', 't'}
                };

        // alarms
        this.alarmTriggered = new boolean[8];
        // dice
        this.diceValues = new int[]{1,1};

        // game status
        this.gameOver = false;
        this.winnerId = -1;
    }
    /**
     * copy constructor; makes a copy of the original object
     *
     * @param orig
     * 		the object from which the copy should be made
     */
    public MuseumCaperState(MuseumCaperState orig, int playerId) {
        this.playerTurn = orig.playerTurn;
        this.numPlayers = orig.numPlayers;
        this.currentPhase = orig.currentPhase;

        // thief visibility
        if (playerId == 0) {
            this.thiefRoomId = orig.thiefRoomId;
            this.stolenPaintings = new ArrayList<>(orig.stolenPaintings);
        } else {
            if(orig.thiefVisible)
            {
                // guards only see the thief if visible
                this.thiefRoomId = orig.thiefRoomId;
            }
            else
            {
                this.thiefRoomId = -1; // hidden from guards
            }
            this.stolenPaintings = new ArrayList<>();
        }


        // detectives
        this.detectiveRoomId = orig.detectiveRoomId.clone();

        // deep copy of rooms
        this.room = new Room[orig.room.length];
        for (int a = 0; a < room.length; a++) {
            this.room[a] = new Room(orig.room[a]);
        }

        // alarms
        this.alarmTriggered = orig.alarmTriggered.clone();

        // dice
        this.diceValues = orig.diceValues.clone();

//        // game board (deep copy)
        this.gameBoard = new char[orig.gameBoard.length][orig.gameBoard[0].length];
        for (int i = 0; i < gameBoard.length; i++) {
            this.gameBoard[i] = orig.gameBoard[i].clone();
        }

        this.gameOver = orig.gameOver;
        this.winnerId = orig.winnerId;


    }

    /**
     * Action Methods
     */

    // GENERAL ACTIONS
    public boolean makeConnectAction(MuseumCaperConnectAction a)
    {
        return true;
    }
    public boolean makeChooseDirectionAction(MuseumCaperChooseDirectionAction a)
    {
        return true;
    }
    public boolean makeMakrStolenPaintingsAction(MuseumCaperMarkStolenPaintingsAction s)
    {
        return true;
    }
    public boolean makeChooseNumberPlayersAction(MuseumCaperChooseNumberPlayerAction a)
    {
        if(currentPhase != GamePhase.SETUP) {
            return false;
        }
        int n = a.getNumPlayers();
        // ranges
        if (n < 2 || n > 3) {
            return false;
        }
        // update state
        this.numPlayers = n;
        return true;
    }

    public boolean makeSetNameAction(MuseumCaperSetNameAction a)
    {
        return true;
    }
    public boolean makeEndTurnAction(MuseumCaperEndTurnAction a)
    {
        if(gameOver)
        {
            return false;
        }
        playerTurn = (playerTurn + 1) % numPlayers;
        return true;
    }
    // GUARD ACTIONS [ human players ]

    public boolean makeRestorePowerAction(MuseumCaperRestorePowerAction a)
    {
        for(Room r : room)
        {
            r.setPowerOn(true);
        }
        return true;
    }
    public boolean makeRollDiceForMovementAction(MuseumCaperRollDiceForMovementAction a)
    {
        diceValues[0] = 3;
        diceValues[1] = 4;
        return true;
    }
    public boolean makeRollDiceForCamerasAction(MuseumCaperRollDieForCamerasAction a)
    {
        diceValues[0] = 2;
        diceValues[1] =5;
        return true;
    }
    public boolean makeGuardEndTurnAction(MuseumCaperGuardEndTurnAction a)
    {
        return makeEndTurnAction(new MuseumCaperEndTurnAction(a.getPlayer()));
    }
    public boolean makeGuardMoveAction(MuseumCaperGuardMoveAction a)
    {
        int guardIndex = a.getPlayer().getPlayerNum() - 1;
        if(guardIndex < 0 || guardIndex >= detectiveRoomId.length)
        {
            return false;
        }
        detectiveRoomId[guardIndex] = a.getTargetRoomId();
        return true;
    }
    public boolean makeChooseQuestionAction(MuseumCaperChooseQuestionAction a)
    {
        return true;
    }
    // THIEF ACTIONS [ AI ]
    public boolean makeThiefMoveAction(MuseumCaperThiefMoveAction a)
    {
        if(a.getPlayer().getPlayerNum() != 0)
        {
            return false;
        }
        thiefRoomId = a.getTargetRoomId();
        return true;
    }
    public boolean makeCutPowerAction(MuseumCaperCutPowerAction a)
    {
        for(Room r : room)
        {
            r.setPowerOn(false);
        }
        return true;
    }
    public boolean makeDisableCameraAction(MuseumCaperDisableCameraAction a)
    {
        int roomId = a.getRoomId();
        if(roomId < 0 || roomId >= room.length)
        {
            return true;
        }
        room[roomId].setHasCamera(false);
        return true;
    }
    public boolean makeReject2MotionDetectorAction(MuseumCaperReject2MotionDetectorAction a)
    {
        return true;
    }

    /**
     * Getter Methods
     */
    public char[][] getGameBoard()
    {
        return gameBoard;
    }
    public int getPlayerTurn()
    {
        return playerTurn;
    }
    public boolean isGameOver()
    {
        return gameOver;
    }
    public int getWinnerId()
    {
        return winnerId;
    }

    @Override
    public String toString()
    {
        return "MuseumCaperState{" +
                "\n  playerTurn=" + playerTurn +
                ",\n  currentPhase=" + currentPhase +
                ",\n  thiefRoomId=" + thiefRoomId +
                ",\n  thiefVisible=" + thiefVisible +
                ",\n  stolenPaintings=" + stolenPaintings +
                ",\n  detectiveRoomId=" + Arrays.toString(detectiveRoomId) +
                ",\n  gameOver=" + gameOver +
                ",\n  winnerId=" + winnerId +
                "\n}";
    }
}

