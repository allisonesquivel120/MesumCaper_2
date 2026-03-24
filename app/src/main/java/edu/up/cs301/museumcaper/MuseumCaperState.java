package edu.up.cs301.museumcaper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import edu.up.cs301.GameFramework.infoMessage.GameState;


/**
 * This contains the state for the Museum Caper game. The state consist of simply
 * the value of the counter.
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version Feb. 2026
 */
public class MuseumCaperState extends GameState {

    // to satisfy Serializable interface
    private static final long serialVersionUID = 7737393762469851826L;
    /**
     * Fastest sequence of moves to end the game:
     * 1. The guard lands on the same room/tile as the thief.
     * This is the shortest legal win condition in this implementation.
     */

    /**
     * instance variables
     */
    // board information
    public static final int NUM_ROWS = 11; // number of rows in the grid
    public static final int NUM_COLS = 12; // number of cols in the grid

    // board info
    private RoomType[][] roomGrid; // room system (for dialogue box)
    private char[][] gameBoard; // game board we created

    // player + turn info
    private String[] playerNames = new String[3];
    private int playerTurn; // which player's turn it currently is
    private int numPlayers; // the number of players currently playing the game
    private GamePhase currentPhase; // the most current phase of the game

    // thief info
    private int thiefRow; // the current room (row) thief is inside [ detectives can't see this ]
    private int thiefCol; // the current room (col) thief is inside [ detectives can't see this ]
    private int thiefRoomId; // compute from tile
    private boolean thiefVisible; // notifies if the thief is visible or not
    private ArrayList<Integer> stolenPaintings; // list of paintings the thief as stolen

    // guard info [tile based]
    private int[] guardRow; // the current room (row) the guard is in
    private int[] guardCol; // the current room (col) the guard is in
    private int[] guardRoomId;


    // cameras + alarm system
    private boolean[][] cameras;

    private boolean[] alarmsTriggered;

    // die 1 + die 2 info
    private int movementRoll; // die 1
    private int questionRoll; // die 2

    // game status
    private boolean gameOver; // notifies if game is over
    private int winnerId; // -1 = no ones won yet

    private transient Random rng;



    public MuseumCaperState()
    {
        this(3); // how many players are allowed to play
    }
    /**
     * default constructor; initialize the variables
     *
     * @param numPlayers
     */
    public MuseumCaperState(int numPlayers) {
        this.numPlayers = numPlayers;
        this.playerTurn = 0; // thief always starts [thief = 0 , guard = 1]
        this.currentPhase = GamePhase.SETUP;

        this.rng = new Random();

        // thief starts from the green room entrance [default]
        this.thiefRow = 7;
        this.thiefCol = 11;
        this.thiefVisible = false;
        this.stolenPaintings = new ArrayList<>();

        // guard start int the top left of the white room [default]
        // randomize eventually
        int numGuard = Math.max(0, numPlayers - 1);
        this.guardRow = new int[numGuard];
        this.guardCol = new int[numGuard];
        for (int i = 0; i < numGuard; i++) {
            guardRow[i] = 3;
            guardCol[i] = 4;
        }

        this.gameBoard = new char[][]{
                {'t', 't', 't', 'r', 'r', 'r', 'r', 'r', 'r', 't', 't', 't'},
                {'t', 't', 't', 'r', 'r', 'r', 'r', 'r', 'r', 't', 't', 't'},
                {'p', 'p', 'p', 'h', 'h', 'h', 'h', 'h', 'h', 'b', 'b', 'b'},
                {'p', 'p', 'p', 'h', 'w', 'w', 'w', 'w', 'h', 'b', 'b', 'b'},
                {'p', 'p', 'p', 'h', 'w', 'w', 'w', 'w', 'h', 'b', 'b', 'b'},
                {'p', 'p', 'p', 'h', 'w', 'w', 'w', 'w', 'h', 'h', 'h', 'h'},
                {'h', 'h', 'h', 'h', 'w', 'w', 'w', 'w', 'h', 'g', 'g', 'g'},
                {'y', 'y', 'y', 'h', 'w', 'w', 'w', 'w', 'h', 'g', 'g', 'g'},
                {'y', 'y', 'y', 'h', 'h', 'h', 'h', 'h', 'h', 'g', 'g', 'g'},
                {'t', 't', 't', 'd', 'd', 'h', 'h', 'h', 'h', 't', 't', 't'},
                {'t', 't', 't', 'd', 'd', 'h', 'h', 'h', 'h', 't', 't', 't'}
        };
        // convert board char into roomType grid
        initRoomGrid();

        // alarmsTriggered + cameras
        this.cameras = new boolean[NUM_ROWS][NUM_COLS];
        this.alarmsTriggered = new boolean[NUM_ROWS * NUM_COLS];

        // dice
        this.movementRoll = 0;
        this.questionRoll = 0;

        // game status
        this.gameOver = false;
        this.winnerId = -1;

        // find initial room Ids
        guardRoomId = new int[numGuard];
        updateRoomIds();
    }

    /**
     * copy constructor; makes a copy of the original object
     *
     * @param orig from which the copy should be made
     */
    public MuseumCaperState(MuseumCaperState orig) {
        this.playerTurn = orig.playerTurn;
        this.numPlayers = orig.numPlayers;
        this.currentPhase = orig.currentPhase;

        // copy player names
        this.playerNames = new String[orig.playerNames.length];
        for(int i = 0; i < playerNames.length; i++)
        {
            this.playerNames[i] = orig.playerNames[i];
        }

        // thief visibility
        this.thiefVisible = orig.thiefVisible;
            if(orig.thiefVisible)
            {
                // guards only see the thief if visible
                this.thiefRow = orig.thiefRow;
                this.thiefCol = orig.thiefCol;
            }
            else
            {
                this.thiefRow = -1; // hidden from guards
                this.thiefCol = -1;
            }
            // guard also sees stolen paintings
        this.stolenPaintings = new ArrayList<>(orig.stolenPaintings);

        // guard position [deep copy]
        this.guardRow = orig.guardRow.clone();
        this.guardCol = orig.guardCol.clone();

        // room ids [computed from tiles]
        this.guardRoomId = orig.guardRoomId.clone();
        this.thiefRoomId = orig.thiefRoomId;

        // game board (deep copy)
        this.gameBoard = new char[orig.gameBoard.length][orig.gameBoard[0].length];
        for (int i = 0; i < gameBoard.length; i++) {
            this.gameBoard[i] = orig.gameBoard[i].clone();
        }

        // room grid [deep copy]
        this.roomGrid = new RoomType[NUM_ROWS][NUM_COLS];
        for (int r = 0; r < NUM_ROWS; r++) {
            this.roomGrid[r] = orig.roomGrid[r].clone();
        }

        // cameras [deep copy]
        this.cameras = new boolean[NUM_ROWS][NUM_COLS];
        for(int r = 0; r < NUM_ROWS; r++)
        {
            this.cameras[r] = orig.cameras[r].clone();
        }

        // alarms
        this.alarmsTriggered = orig.alarmsTriggered.clone();

        // dice
        this.movementRoll = orig.movementRoll;
        this.questionRoll = orig.questionRoll;

        // game status
        this.gameOver = orig.gameOver;
        this.winnerId = orig.winnerId;

        // for future rolls
        this.rng = new Random();
    }
    // HELPER METHODS
    /**
     * convert gameBoard characters into RoomType(s)
     */
    private void initRoomGrid()
    {
        roomGrid = new RoomType[NUM_ROWS][NUM_COLS];
        for(int r = 0; r < NUM_ROWS; r++)
        {
            for(int c = 0; c < NUM_COLS; c++)
            {
                roomGrid[r][c] = RoomType.fromChar(gameBoard[r][c]);
            }
        }
    }

    /**
     * compute room ids from tile positions
     * - updates room ID's of the thief and guard based on thief current tile position
     */
    private void updateRoomIds()
    {
        thiefRoomId = roomGrid[thiefRow][thiefCol].ordinal();
        for(int i = 0; i < guardRow.length; i++)
        {
            guardRoomId[i] = roomGrid[guardRow[i]][guardCol[i]].ordinal();
        }
    }

    /**
     * General Action Methods
     */

    /**
     * verifies that game is != gameOver and that connecting playerId is valid
     */
    public boolean makeConnectAction(MuseumCaperConnectAction a)
    {
        if(gameOver)
        {
            return false;
        }
        // getting plater number of the player who sent action
        int playerId = a.getPlayer().getPlayerNum();
        // checking if player ID is valid
        if(playerId < 0 || playerId >= numPlayers)
        {
            return false;
        }
        // means that all checks have been passed = action is allowed
        return true;
    }
    /**
     * handles choose-direction action - currently a placeholder
     */
    public boolean makeChooseDirectionAction(MuseumCaperChooseDirectionAction a)
    {
        int dir = a.getDirection();
        // placeholder : chosen direction
        return true;
    }
    /**
     * marks painting as stolen
     * - if paintingID not in stolenPainting list it adds it
     */
    public boolean makeMarkStolenPaintingsAction(MuseumCaperMarkStolenPaintingsAction a)
    {
        int paintingId = a.getPaintingId();
        if(!stolenPaintings.contains(paintingId))
        {
            stolenPaintings.add(paintingId);
        }
        return true;
    }
    /**
     * sets the player's display name
     */
    public boolean makeSetNameAction(MuseumCaperSetNameAction a)
    {
        int playerId = a.getPlayer().getPlayerNum();
        String name = a.getName();
        playerNames[playerId] = name;
        return true;
    }
    /**
     * ends the current player's turn
     * - ensure game is not over
     * - ensure only active player can end their turn
     */
    public boolean makeEndTurnAction(MuseumCaperEndTurnAction a)
    {
        if(gameOver)
        {
            return false;
        }
        if(a.getPlayer().getPlayerNum() != playerTurn)
        {
            return false;
        }
        // goes to next player
        playerTurn = (playerTurn + 1) % numPlayers;
        // rest phase to START_TURN
        currentPhase = GamePhase.START_TURN;
        return true;
    }
    /**
     * handles dice roll action for the guard(s)
     * - depending on roll type [movement or question], rolls right die,
     *   updates phase, and stores result
     */
    public boolean makeRollDiceAction(MuseumCaperRollDiceAction a)
    {
        if(playerTurn != 1)
        {
            return false; // only guard can roll dice
        }

        switch (a.getType())
        {
            case MOVEMENT:
                // guard MUST be in GUARD_ROLL phase to roll movement die
                if(currentPhase != GamePhase.GUARD_ROLL) return false;
                movementRoll = rng.nextInt(6) + 1; // roll 1-6
                currentPhase = GamePhase.GUARD_MOVE;
                return true;
            case QUESTION:
                // guard rolls question die before asking a question
                if(currentPhase != GamePhase.GUARD_QUESTION)
                {
                    questionRoll = rng.nextInt(6)+1;
                    currentPhase = GamePhase.GUARD_ASK;
                    return true;
                }
            default:
                return false;
        }
    }

    public int getDiceValue () {
        return this.movementRoll;
    }
    /**
     * handles guard movement action
     * - validates turn, phase, guardIndex, and movement distance
     * - moves guard + checks win condition
     * - hands the turn to thief
     */
    public boolean makeGuardMoveAction(MuseumCaperGuardMoveAction a)
    {
        if(currentPhase != GamePhase.GUARD_MOVE)
        {
            return false; // must be in movement phase
        }
        if(playerTurn != 1)
        {
            return false; // only guard can move here
        }
        // identifies which guard is moving
        int guardIndex = a.getGuardIndex();
        int tr = a.getTargetRow();
        int tc = a.getTargetCol();

        if(guardIndex < 0 || guardIndex >= guardRow.length)
        {
            return false; // = invalid guardIndex
        }


        if(!inBounds(tr,tc))
        {
            return false; // can not move off the board
        }

        int dist = manhattan(guardRow[guardIndex], guardCol[guardIndex],tr,tc);
        if(dist > movementRoll)
        {
            return false; // movement goes over rolled distance
        }
        // apply movement
        guardRow[guardIndex] = tr;
        guardCol[guardIndex] = tc;
        updateRoomIds();

        // winner check [checks if guard caught thief]
        if (tr == thiefRow && tc == thiefCol) {
            gameOver = true;
            winnerId = guardIndex + 1;
            currentPhase = GamePhase.ENDGAME;
            return true;
        }

        // end guard turn move to thief AI
        playerTurn = 0;
        currentPhase = GamePhase.THIEF_TURN;
        runThiefAI();

        return true;
    }
    /**
     * handles guard choosing which question to ask after rolling the question die
     * - records the chosen question
     * - moves onto the GUARD_ASK phase
     */
    public boolean makeChooseQuestionAction(MuseumCaperChooseQuestionAction a)
    {
        if(playerTurn == 0)
        {
            return false;
        }
        int index = a.getQuestionIndex();
        System.out.println("Guard chose question index" + index);
        currentPhase = GamePhase.GUARD_ASK;
        return true;
    }
    // thief AI movement
    /**
     * runs the thief movement
     * - attempts random paths + avoiding out of bounds tiles and cameras
     * - after moving --> thief disables cameras on their tile + updates alarms
     * - thief returns
     */
    void runThiefAI()
    {
        if(gameOver)
        {
            return;
        }

        int steps = rng.nextInt(3) + 1; // thief move 1-3 steps
        // attempt up to 10 random movement paths
        for(int a = 0; a < 10; a++)
        {
            int r = thiefRow;
            int c = thiefCol;

            for(int s = 0; s < steps; s++)
            {
                int dir = rng.nextInt(4);
                switch (dir){
                    case 0: r--; break;
                    case 1: r++; break;
                    case 2: c--; break;
                    case 3: c++; break;
                }
                if(!inBounds(r,c))
                {
                    break; // invalid path
                }
            }
            if(!inBounds(r,c))
            {
                continue; // reject path
            }
            if(cameras[r][c])
            {
                continue; // avoid cameras
            }
            // accept this path
            thiefRow = r;
            thiefCol = c;
            break;
        }
        // disable camera if standing on one
        if (cameras[thiefRow][thiefCol]) {
            cameras[thiefRow][thiefCol] = false;
        }

        updateRoomIds();
        updateAlarms();

        // return turn to guard
        playerTurn = 1;
        currentPhase = GamePhase.GUARD_ROLL;
        movementRoll = 0;
    }

    // HELPER METHODS
    /**
     * updates the alarm array so each alarm is triggers only if camera is active on tile
     * and thief is currently standing on said tile
     */
    private void updateAlarms() {
        for (int r = 0; r < NUM_ROWS; r++) {
            for (int c = 0; c < NUM_COLS; c++) {
                int idx = r * NUM_COLS + c;
                alarmsTriggered[idx] = cameras[r][c] && (r == thiefRow && c == thiefCol);
            }
        }
    }
    /**
     * returns true if the given row/col is within the board
     */
    private boolean inBounds(int r, int c) {
        return r >= 0 && r < NUM_ROWS && c >= 0 && c < NUM_COLS;
    }
    /**
     * External Cite : CO-PILOT
     * asked how i could control that the guards could not move diagonally
     * suggested using Math.abs to set limitations
     */

    /**
     * computes manhattan distance between two tiles (no diagonals)
     */
    private int manhattan(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }

    /**
     * Getter Methods
     */
    public char[][] getGameBoard()
    {
        return gameBoard;
    }
    public int getThiefRow()
    {
        return thiefRow;
    }
    public int getThiefCol()
    {
        return thiefCol;
    }
    public int[] getGuardRow()
    {
        return guardRow.clone();
    }
    public int[] getGuardCol()
    {
        return guardCol.clone();
    }
    public boolean[][] getCameras()
    {
        return cameras;
    }
    public boolean[] getAlarmTriggered()
    {
        return alarmsTriggered.clone();
    }
    public int getMovementRoll()
    {
        return movementRoll;
    }
    public ArrayList<Integer> getStolenPaintings()
    {
        return stolenPaintings;
    }

    public int getQuestionRoll()
    {
        return questionRoll;
    }
    public int getThiefRoomId()
    {
        return thiefRoomId;
    }
    public int[] getGuardRoomId()
    { return guardRoomId.clone(); }

    public boolean isThiefVisible()
    {
        return thiefVisible;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getWinnerId() {
        return winnerId;
    }
    public int getPlayerTurn() {
        return playerTurn;
    }
    public int getGuardRow(int guardIndex)
    {
        return guardRow[guardIndex];
    }
    public int getGuardCol(int guardIndex)
    {
        return guardCol[guardIndex];
    }
    public GamePhase getCurrentPhase() {
        return currentPhase;
    }

    @Override
    public String toString()
    {
        return "MuseumCaperState{" +
                "\n  playerTurn=" + playerTurn +
                ",\n  thief=(" + thiefRow + "," + thiefCol + ")" +
                ",\n  guards=" + Arrays.toString(guardRow) + " x " + Arrays.toString(guardCol) +
                ",\n  movementRoll=" + movementRoll +
                // ",\n  questionRoll=" + questionRoll +
                ",\n  gameOver=" + gameOver +
                ",\n  winnerId=" + winnerId +
                "\n}";

    }

    // USED FOR THE UNIT TEST, omit before submission
    public void setPlayerTurn(int i) {
        playerTurn = i;
    }
    // USED FOR THE UNIT TEST, omit before submission
    public void setGamePhase(GamePhase unfazed) {
        currentPhase = unfazed;
    }
    // USED FOR THE UNIT TEST, omit before submission
    public void setMovementRoll(int i) {
        movementRoll = i;
    }
    // USED FOR THE UNIT TEST, omit before submission
    public void setThiefPosition(int row, int col) {
        thiefRow = row;
        thiefCol = col;
    }
    /**
     * sets the guard's position for JUNIT test
     * - ensures test doesn't assigns invalid/out of bound tiles
     */
    public void setGuardPosition(int guardIndex, int row, int col) {
        if (guardIndex < 0 || guardIndex >= guardRow.length) {
            throw new IllegalArgumentException("Invalid guard index: " + guardIndex);
        }
        if (row < 0 || row >= NUM_ROWS || col < 0 || col >= NUM_COLS) {
            throw new IllegalArgumentException("Invalid row/col: " + row + "/" + col);
        }
        guardRow[guardIndex] = row;
        guardCol[guardIndex] = col;
    }
    public void setNumPlayers(int i) {
        numPlayers = i;
    }
    public void setPlayerNames(int index, String name) {
        playerNames[index] = name;
    }
}

