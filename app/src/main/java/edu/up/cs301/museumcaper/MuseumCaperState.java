package edu.up.cs301.museumcaper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import edu.up.cs301.GameFramework.infoMessage.GameState;

/**
 * This contains the state for the Museum Caper game.
 * Tracks all game information
 *
 * Win conditions:
 * - Detective wins: lands on the same tile as the thief
 * - Thief wins: steals at least 3 paintings
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 *
 * @version March 2026
 */
public class MuseumCaperState extends GameState {

    // to satisfy Serializable interface
    private static final long serialVersionUID = 7737393762469851826L;

    // =====================================================================
    // CONSTANTS
    // =====================================================================

    // number of rows & columns in the grid
    public static final int NUM_ROWS = 11;
    public static final int NUM_COLS = 12;

    // =====================================================================
    // INSTANCE VARIABLES
    // =====================================================================

    // board layout
    private RoomType[][] roomGrid; // maps each tile to its RoomType enum
    private char[][] gameBoard; // raw character grid defining the board

    // player and turn tracking
    private String[] playerNames = new String[3];
    private int playerTurn; // 0 = thief (AI), 1 = detective (human)
    private int numPlayers;
    private GamePhase currentPhase;

    // painting positions — flat index (row * NUM_COLS + col)
    private int[] paintingPositions;

    // thief info, position is hidden from detective unless thiefVisible is true
    private int thiefRow;
    private int thiefCol;
    private int thiefRoomId;
    private boolean thiefVisible;
    private ArrayList<Integer> stolenPaintings;

    // detective info
    private int[] guardRow;
    private int[] guardCol;
    private int[] guardRoomId;

    // camera and alarm system
    private boolean[][] cameras; // true = camera active at that tile
    private boolean[] alarmsTriggered; // true = thief standing on active camera

    // dice
    private int movementRoll; // result of movement die (1-6)
    private int questionRoll; // result of question/camera die (1-6)

    // game status
    private boolean gameOver;
    private int winnerId; // -1 = no winner yet, 0 = thief, 1+ = detective index

    private transient Random rng;

    // =====================================================================
    // CONSTRUCTORS
    // =====================================================================

    /**
     * Default constructor — creates a 3-player game
     */
    public MuseumCaperState() {
        this(3);
    }

    /**
     * Main constructor — initializes all game state for a new game.
     * Game starts in SETUP phase so the human can place paintings and cameras
     * before the thief AI takes its first move.
     *
     * @param numPlayers total number of players (including the AI thief)
     */
    public MuseumCaperState(int numPlayers) {
        this.numPlayers = numPlayers;

        // human detective places pieces first during setup
        this.playerTurn = 1;
        this.currentPhase = GamePhase.SETUP;

        this.rng = new Random();

        // thief starts in the green room (bottom right)
        this.thiefRow = 7;
        this.thiefCol = 11;

        // FOR BETA: thief is visible so both players can see movement on the board
        // TODO: set to false for final release
        this.thiefVisible = true;

        this.stolenPaintings = new ArrayList<>();

        // all 9 paintings start unplaced (-1 = not on board yet)
        this.paintingPositions = new int[9];
        Arrays.fill(paintingPositions, -1);

        // guard starts in the white center room
        int numGuard = Math.max(0, numPlayers - 1);
        this.guardRow = new int[numGuard];
        this.guardCol = new int[numGuard];
        for (int i = 0; i < numGuard; i++) {
            guardRow[i] = 3;
            guardCol[i] = 4;
        }

        // 't' = inaccessible corner
        // 'p' = purple room
        // 'h' = hallway
        // 'g' = green room
        // 'd' = door/entrance
        // 'r' = red room
        // 'b' = blue room
        // 'w' = white room
        // 'y' = yellow room
        this.gameBoard = new char[][]{
                {'t', 't', 't', 'r', 'r', 'r', 'r', 'r', 'r', 't', 't', 't'},
                {'t', 't', 't', 'r', 'r', 'r', 'r', 'r', 'r', 't', 't', 't'},
                {'p', 'p', 'p', 'h', 'h', 'h', 'h', 'h', 'h', 'b', 'b', 'b'},
                {'p', 'p', 'p', 'h', 'w', 'w', 'w', 'w', 'h', 'b', 'b', 'b'},
                {'p', 'p', 'p', 'h', 'w', 'w', 'w', 'w', 'h', 'b', 'b', 'b'},
                {'h', 'h', 'h', 'h', 'w', 'w', 'w', 'w', 'h', 'h', 'h', 'h'},
                {'y', 'y', 'y', 'h', 'w', 'w', 'w', 'w', 'h', 'g', 'g', 'g'},
                {'y', 'y', 'y', 'h', 'w', 'w', 'w', 'w', 'h', 'g', 'g', 'g'},
                {'y', 'y', 'y', 'h', 'h', 'h', 'h', 'h', 'h', 'g', 'g', 'g'},
                {'t', 't', 't', 'h', 'h', 'h', 'h', 'd', 'd', 't', 't', 't'},
                {'t', 't', 't', 'h', 'h', 'h', 'h', 'd', 'd', 't', 't', 't'}
        };

        initRoomGrid();

        this.cameras = new boolean[NUM_ROWS][NUM_COLS];
        this.alarmsTriggered = new boolean[NUM_ROWS * NUM_COLS];

        this.movementRoll = 0;
        this.questionRoll = 0;

        this.gameOver = false;
        this.winnerId = -1;

        guardRoomId = new int[numGuard];
        updateRoomIds();
    } // Constructor

    /**
     * Copy constructor — makes a full deep copy of the original state.
     *
     * @param orig the state to copy from
     */
    public MuseumCaperState(MuseumCaperState orig) {
        this.playerTurn = orig.playerTurn;
        this.numPlayers = orig.numPlayers;
        this.currentPhase = orig.currentPhase;

        this.playerNames = new String[orig.playerNames.length];
        for (int i = 0; i < playerNames.length; i++) {
            this.playerNames[i] = orig.playerNames[i];
        }

        // deep copy painting positions array
        this.paintingPositions = orig.paintingPositions.clone();

        this.thiefVisible = orig.thiefVisible;
        if (orig.thiefVisible) {
            this.thiefRow = orig.thiefRow;
            this.thiefCol = orig.thiefCol;
        } else {
            // thief position hidden from detective when not visible
            this.thiefRow = -1;
            this.thiefCol = -1;
        }

        this.stolenPaintings = new ArrayList<>(orig.stolenPaintings);
        this.guardRow = orig.guardRow.clone();
        this.guardCol = orig.guardCol.clone();
        this.guardRoomId = orig.guardRoomId.clone();
        this.thiefRoomId = orig.thiefRoomId;

        this.gameBoard = new char[orig.gameBoard.length][orig.gameBoard[0].length];
        for (int i = 0; i < gameBoard.length; i++) {
            this.gameBoard[i] = orig.gameBoard[i].clone();
        }

        this.roomGrid = new RoomType[NUM_ROWS][NUM_COLS];
        for (int r = 0; r < NUM_ROWS; r++) {
            this.roomGrid[r] = orig.roomGrid[r].clone();
        }

        this.cameras = new boolean[NUM_ROWS][NUM_COLS];
        for (int r = 0; r < NUM_ROWS; r++) {
            this.cameras[r] = orig.cameras[r].clone();
        }

        this.alarmsTriggered = orig.alarmsTriggered.clone();
        this.movementRoll = orig.movementRoll;
        this.questionRoll = orig.questionRoll;
        this.gameOver = orig.gameOver;
        this.winnerId = orig.winnerId;
        this.rng = new Random();
    } // copy constructor

    /**
     * Player-perspective copy constructor — creates a copy filtered for a
     * specific player's view. The detective cannot see the thief's position
     * unless thiefVisible is true.
     *
     * @param orig     the full game state to copy from
     * @param playerId the player receiving this copy (0 = thief, 1 = detective)
     */
    public MuseumCaperState(MuseumCaperState orig, int playerId) {
        this.playerTurn = orig.playerTurn;
        this.numPlayers = orig.numPlayers;
        this.currentPhase = orig.currentPhase;

        this.playerNames = new String[orig.playerNames.length];
        for (int i = 0; i < playerNames.length; i++) {
            this.playerNames[i] = orig.playerNames[i];
        }

        // deep copy painting positions — visible to all players
        this.paintingPositions = orig.paintingPositions.clone();

        this.thiefVisible = orig.thiefVisible;
        if (playerId == 0) {
            // thief always sees their own position
            this.thiefRow = orig.thiefRow;
            this.thiefCol = orig.thiefCol;
        } else {
            // detective only sees thief position if thief is visible
            if (orig.thiefVisible) {
                this.thiefRow = orig.thiefRow;
                this.thiefCol = orig.thiefCol;
            } else {
                this.thiefRow = -1;
                this.thiefCol = -1;
            }
        }

        // stolen paintings are visible to all players
        this.stolenPaintings = new ArrayList<>(orig.stolenPaintings);

        this.guardRow = orig.guardRow.clone();
        this.guardCol = orig.guardCol.clone();
        this.guardRoomId = orig.guardRoomId.clone();
        this.thiefRoomId = orig.thiefRoomId;

        this.gameBoard = new char[orig.gameBoard.length][orig.gameBoard[0].length];
        for (int i = 0; i < gameBoard.length; i++) {
            this.gameBoard[i] = orig.gameBoard[i].clone();
        }

        this.roomGrid = new RoomType[NUM_ROWS][NUM_COLS];
        for (int r = 0; r < NUM_ROWS; r++) {
            this.roomGrid[r] = orig.roomGrid[r].clone();
        }

        this.cameras = new boolean[NUM_ROWS][NUM_COLS];
        for (int r = 0; r < NUM_ROWS; r++) {
            this.cameras[r] = orig.cameras[r].clone();
        }

        this.alarmsTriggered = orig.alarmsTriggered.clone();
        this.movementRoll = orig.movementRoll;
        this.questionRoll = orig.questionRoll;
        this.gameOver = orig.gameOver;
        this.winnerId = orig.winnerId;
        this.rng = new Random();
    } // copy constructor

    // =====================================================================
    // SETUP ACTIONS
    // =====================================================================

    /**
     * Handles the human player finishing the setup phase.
     * Transitions SETUP → THIEF_TURN and runs the thief's opening move.
     * This must be called AFTER paintings and cameras are placed so the
     * thief AI has real board data to work with.
     *
     * @param a the finish setup action
     * @return true if setup was successfully completed
     */
    public boolean makeFinishSetupAction(MuseumCaperFinishSetupAction a) {
        if (currentPhase != GamePhase.SETUP) return false;
        playerTurn = 0;
        currentPhase = GamePhase.THIEF_TURN;
        runThiefAI(); // thief moves first; ends by setting playerTurn=1, phase=GUARD_ROLL
        return true;
    } // makeFinishSetupAction


    /**
     * Records the position of a painting on the board during setup.
     * Stores the flat index (row * NUM_COLS + col) in paintingPositions
     * so the animator can draw it on the correct tile.
     *
     * @param a the place painting action containing paintingId, row, col
     * @return true if the painting was successfully placed
     */
    public boolean makePlacePaintingAction(MuseumCaperPlacePaintingAction a) {
        if (currentPhase != GamePhase.SETUP) return false;
        int r = a.getRow();
        int c = a.getCol();
        if (!inBounds(r, c)) return false;
        int id = a.getPaintingId(); // 1-indexed (1–9)
        paintingPositions[id - 1] = r * NUM_COLS + c; // convert to flat index
        return true;
    }// makePlacePaintingAction

    /**
     * Places a camera on the board during setup.
     * The thief AI uses the cameras array to avoid detection.
     *
     * @param a the place camera action containing row and col
     * @return true if the camera was successfully placed
     */
    public boolean makePlaceCameraAction(MuseumCaperPlaceCameraAction a) {
        if (currentPhase != GamePhase.SETUP) return false;
        int r = a.getRow();
        int c = a.getCol();
        if (!inBounds(r, c)) return false;
        cameras[r][c] = true;
        return true;
    } // makePlaceCameraAction

    // =====================================================================
    // GAME ACTIONS
    // =====================================================================

    /**
     * Verifies a player connection is valid — game must not be over
     * and the player ID must be within range.
     *
     * @param a the connect action
     * @return true if the connection is valid
     */
    public boolean makeConnectAction(MuseumCaperConnectAction a) {
        if (gameOver) return false;
        int playerId = a.getPlayer().getPlayerNum();
        return playerId >= 0 && playerId < numPlayers;
    }// makeConnectAction

    /**
     * Placeholder for choose-direction action — not yet implemented.
     *
     * @param a the choose direction action
     * @return always true for now
     */
    public boolean makeChooseDirectionAction(MuseumCaperChooseDirectionAction a) {
        return true;
    }

    /**
     * Sets a player's display name.
     *
     * @param a the set name action
     * @return true always
     */
    public boolean makeSetNameAction(MuseumCaperSetNameAction a) {
        int playerId = a.getPlayer().getPlayerNum();
        playerNames[playerId] = a.getName();
        return true;
    }

    /**
     * Marks a painting as stolen by the thief.
     * Cannot be called during SETUP. If the thief steals 3 or more
     * paintings, the game ends with the thief winning.
     *
     * @param a the mark stolen painting action
     * @return true if the painting was successfully marked
     */
    public boolean makeMarkStolenPaintingsAction(MuseumCaperMarkStolenPaintingsAction a) {
        if (currentPhase == GamePhase.SETUP) return false;
        int paintingId = a.getPaintingId();
        if (!stolenPaintings.contains(paintingId)) {
            stolenPaintings.add(paintingId);
        }
        // thief wins by stealing 3 or more paintings
        if (stolenPaintings.size() >= 3) {
            gameOver = true;
            winnerId = 0;
            currentPhase = GamePhase.ENDGAME;
        }
        return true;
    }// makeMarkStolenPaintingsAction

    /**
     * Ends the current player's turn.
     * If the detective ends their turn, switches to thief.
     * If the thief ends their turn, runs the AI movement.
     *
     * @param a the end turn action
     * @return true if the turn was successfully ended
     */
    public boolean makeEndTurnAction(MuseumCaperEndTurnAction a) {
        if (gameOver) return false;
        int actingPlayer = a.getPlayer().getPlayerNum();
        if (actingPlayer == 0) {
            runThiefAI();
            return true;
        }
        if (actingPlayer != playerTurn) return false;
        playerTurn = 0;
        currentPhase = GamePhase.THIEF_TURN;
        return true;
    }// makeEndTurnAction

    /**
     * Handles a dice roll from the detective.
     * MOVEMENT die: transitions GUARD_ROLL → GUARD_MOVE and stores the roll.
     * QUESTION die: transitions GUARD_QUESTION → GUARD_ASK and stores the roll.
     * Phase is checked instead of playerTurn to handle framework player numbering.
     *
     * @param a the roll dice action containing the dice type
     * @return true if the roll was valid and applied
     */
    public boolean makeRollDiceAction(MuseumCaperRollDiceAction a) {
        switch (a.getType()) {
            case MOVEMENT:
                if (currentPhase != GamePhase.GUARD_ROLL) return false;
                movementRoll = rng.nextInt(6) + 1;
                currentPhase = GamePhase.GUARD_MOVE;
                return true;
            case QUESTION:
                if (currentPhase != GamePhase.GUARD_QUESTION) return false;
                questionRoll = rng.nextInt(6) + 1;
                currentPhase = GamePhase.GUARD_ASK;
                return true;
            default:
                return false;
        }
    }// makeRollDiceAction

    /**
     * Handles the detective moving to a target tile.
     * Validates the move is within the rolled distance (manhattan distance).
     * If the detective lands on the thief's tile, the detective wins.
     * Phase is checked instead of playerTurn to handle framework player numbering.
     *
     * @param a the guard move action containing guardIndex, targetRow, targetCol
     * @return true if the move was valid and applied
     */
    public boolean makeGuardMoveAction(MuseumCaperGuardMoveAction a) {
        if (currentPhase != GamePhase.GUARD_MOVE) return false;

        int guardIndex = a.getGuardIndex();
        int tr = a.getTargetRow();
        int tc = a.getTargetCol();

        if (guardIndex < 0 || guardIndex >= guardRow.length) return false;
        if (!inBounds(tr, tc)) return false;

        // prevent moving to inaccessible tiles
        if (gameBoard[tr][tc] == 't') return false;

        // validate move distance using manhattan distance (no diagonal jumps)
        int dist = manhattan(guardRow[guardIndex], guardCol[guardIndex], tr, tc);
        if (dist > movementRoll) return false;

        // apply movement
        guardRow[guardIndex] = tr;
        guardCol[guardIndex] = tc;
        updateRoomIds();

        // check win condition — detective catches thief by landing on same tile
        if (tr == thiefRow && tc == thiefCol) {
            gameOver = true;
            winnerId = guardIndex + 1;
            currentPhase = GamePhase.ENDGAME;
            return true;
        }

        // hand turn back to thief AI
        playerTurn = 0;
        currentPhase = GamePhase.THIEF_TURN;
        runThiefAI();
        return true;
    }// makeGuardMoveAction

    /**
     * Handles the detective choosing a question to ask after rolling
     * the question die. Records the choice and advances to GUARD_ASK.
     *
     * @param a the choose question action
     * @return true if the question was selected successfully
     */
    public boolean makeChooseQuestionAction(MuseumCaperChooseQuestionAction a) {
        if (playerTurn == 0) return false;
        currentPhase = GamePhase.GUARD_ASK;
        return true;
    }

    // =====================================================================
    // THIEF AI
    // =====================================================================

    /**
     * Runs the thief AI movement for one turn.
     * The thief attempts up to 10 random paths of 1 to 3 steps,
     * avoiding out-of-bounds tiles and active cameras.
     * After moving, disables any camera on the landing tile,
     * updates alarms, and returns the turn to the detective.
     */
    void runThiefAI() {
        if (gameOver) return;

        int steps = rng.nextInt(3) + 1; // thief moves 1-3 steps per turn

        // attempt up to 10 random paths — accept the first valid one
        for (int a = 0; a < 10; a++) {
            int r = thiefRow;
            int c = thiefCol;

            for (int s = 0; s < steps; s++) {
                int dir = rng.nextInt(4);
                switch (dir) {
                    case 0: r--; break; // up
                    case 1: r++; break; // down
                    case 2: c--; break; // left
                    case 3: c++; break; // right
                }
                if (!inBounds(r, c)) break;

                // reject path if it hits an inaccessible tile
                if (gameBoard[r][c] == 't') break;
            }

            if (!inBounds(r, c)) continue;  // reject out-of-bounds path
            if (cameras[r][c]) continue;     // reject camera-blocked tile

            // accept this path
            thiefRow = r;
            thiefCol = c;
            break;
        }

        // disable camera if thief lands on one (stretch goal: show visual feedback)
        if (cameras[thiefRow][thiefCol]) {
            cameras[thiefRow][thiefCol] = false;
        }

        updateRoomIds();
        updateAlarms();

        // return turn to detective
        playerTurn = 1;
        currentPhase = GamePhase.GUARD_ROLL;
        movementRoll = 0;
    } //runThiefAI

    // =====================================================================
    // PRIVATE HELPER METHODS
    // =====================================================================

    /**
     * Converts the raw gameBoard char array into a RoomType grid.
     * Called once during construction.
     */
    private void initRoomGrid() {
        roomGrid = new RoomType[NUM_ROWS][NUM_COLS];
        for (int r = 0; r < NUM_ROWS; r++) {
            for (int c = 0; c < NUM_COLS; c++) {
                roomGrid[r][c] = RoomType.fromChar(gameBoard[r][c]);
            }
        }
    }

    /**
     * Recomputes room IDs for the thief and all guards based on
     * their current tile positions.
     */
    private void updateRoomIds() {
        thiefRoomId = roomGrid[thiefRow][thiefCol].ordinal();
        for (int i = 0; i < guardRow.length; i++) {
            guardRoomId[i] = roomGrid[guardRow[i]][guardCol[i]].ordinal();
        }
    }

    /**
     * Updates the alarm array. An alarm triggers only when a camera is
     * active on the exact tile the thief is currently standing on.
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
     * Returns true if the given row and column are within the board boundaries.
     *
     * @param r row index
     * @param c column index
     * @return true if in bounds
     */
    private boolean inBounds(int r, int c) {
        return r >= 0 && r < NUM_ROWS && c >= 0 && c < NUM_COLS;
    }

    /**
     * Computes the Manhattan distance between two tiles.
     * Used to validate guard movement — diagonal movement costs 2,
     * preventing single-step diagonal moves.
     *
     * External Citation: Copilot suggested using Math.abs for
     * Manhattan distance to prevent diagonal movement.
     *
     * @param r1 starting row
     * @param c1 starting column
     * @param r2 target row
     * @param c2 target column
     * @return the Manhattan distance between the two tiles
     */
    private int manhattan(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }

    // =====================================================================
    // GETTERS
    // =====================================================================

    public char[][] getGameBoard() { return gameBoard; }
    public int[] getPaintingPositions() { return paintingPositions.clone(); }
    public int getThiefRow() { return thiefRow; }
    public int getThiefCol() { return thiefCol; }
    public int[] getGuardRow() { return guardRow.clone(); }
    public int[] getGuardCol() { return guardCol.clone(); }
    public int getGuardRow(int guardIndex) { return guardRow[guardIndex]; }
    public int getGuardCol(int guardIndex) { return guardCol[guardIndex]; }
    public boolean[][] getCameras() { return cameras; }
    public boolean[] getAlarmTriggered() { return alarmsTriggered.clone(); }
    public int getMovementRoll() { return movementRoll; }
    public int getDiceValue() { return movementRoll; }
    public int getQuestionRoll() { return questionRoll; }
    public ArrayList<Integer> getStolenPaintings() { return stolenPaintings; }
    public int getThiefRoomId() { return thiefRoomId; }
    public int[] getGuardRoomId() { return guardRoomId.clone(); }
    public boolean isThiefVisible() { return thiefVisible; }
    public boolean isGameOver() { return gameOver; }
    public int getWinnerId() { return winnerId; }
    public int getPlayerTurn() { return playerTurn; }
    public GamePhase getCurrentPhase() { return currentPhase; }

    // =====================================================================
    // SETTERS
    // =====================================================================

    /** @param i the player turn to set (0 = thief, 1 = detective) */
    public void setPlayerTurn(int i) { playerTurn = i; }

    /** @param phase the game phase to set */
    public void setGamePhase(GamePhase phase) { currentPhase = phase; }

    /** @param i the movement roll value to set */
    public void setMovementRoll(int i) { movementRoll = i; }

    /** @param row thief row position */
    public void setThiefPosition(int row, int col) { thiefRow = row; thiefCol = col; }

    /** @param i total number of players */
    public void setNumPlayers(int i) { numPlayers = i; }

    /** @param index player index, @param name player display name */
    public void setPlayerNames(int index, String name) { playerNames[index] = name; }

    /**
     * Sets a guard's position — validates index and bounds before applying.
     *
     * @param guardIndex which guard to move
     * @param row        target row
     * @param col        target column
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

    // =====================================================================
    // TO STRING
    // =====================================================================

    @Override
    public String toString() {
        return "MuseumCaperState{" +
                "\n  playerTurn=" + playerTurn +
                ",\n  phase=" + currentPhase +
                ",\n  thief=(" + thiefRow + "," + thiefCol + ")" +
                ",\n  guards=" + Arrays.toString(guardRow) + " x " + Arrays.toString(guardCol) +
                ",\n  movementRoll=" + movementRoll +
                ",\n  gameOver=" + gameOver +
                ",\n  winnerId=" + winnerId +
                "\n}";
    }
}