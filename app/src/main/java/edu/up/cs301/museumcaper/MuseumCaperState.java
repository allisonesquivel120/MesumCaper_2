package edu.up.cs301.museumcaper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import edu.up.cs301.GameFramework.infoMessage.GameState;

/**
 * This contains the state for the Museum Caper game.
 * Tracks all game information
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
    private final char[][] gameBoard; // raw character grid defining the board

    // player and turn tracking -- not being read
    private String[] playerNames = new String[4];
    private boolean questionDieUsed = false;
    private boolean movementDieUsed = false;
    private int playerTurn; // 0 = thief (AI), 1 = detective (human)
    private int numPlayers;
    private GamePhase currentPhase;
    // stores the AI thief's last answer to the detective's question
    private String lastQuestionAnswer = "";

    // painting positions — flat index (row * NUM_COLS + col)
    private final int[] paintingPositions;

    // thief info, position is hidden from detective unless thiefVisible is true
    private int thiefRow;
    private int thiefCol;
    private int thiefRoomId;
    private boolean thiefVisible;
    private final ArrayList<Integer> stolenPaintings;

    // detective info
    private int[] guardRow;
    private int[] guardCol;
    private int[] guardRoomId;
    // camera and alarm system
    private final boolean[][] cameras; // true = camera active at that tile
    private int cameraCount = 0;
    private boolean[] alarmsTriggered; // true = thief standing on active camera

    // dice
    private int movementRoll; // result of movement die (1-6)
    private int questionRoll; // result of question/camera die (1-6)

    // game status
    private boolean gameOver;
    private int winnerId; // -1 = no winner yet, 0 = thief, 1+ = detective index

    // AI
    public enum AIType { DUMB, SMART }
    private AIType aiType = AIType.SMART; // default, can be changed
    // extra
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
        //this.thiefRow = 7;
        //this.thiefCol = 11;

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
        // '+' = door/entrance
        // 'r' = red room
        // 'b' = blue room
        // 'w' = white room
        // 'y' = yellow room
        // 'o' = gray room
        // 'v' = power room
        this.gameBoard = new char[][]{
                {'t', 't', 't', 'r', 'r', 'r', 'r', 'r', 'r', 't', 't', 't'},
                {'t', 't', 't', 'r', '+', 'r', 'r', '+', 'r', 't', 't', 't'},
                {'p', 'p', 'p', 'h', 'h', 'h', 'h', 'h', 'h', 'b', 'b', 'b'},
                {'p', 'p', 'p', 'h', 'w', '+', '+', 'w', 'h', '+', 'b', 'b'},
                {'p', 'p', '+', 'h', 'w', 'w', 'w', 'w', 'h', 'h', 'h', 'h'},
                {'h', 'h', 'h', 'h', 'w', 'w', 'w', 'w', 'h', 'g', 'g', 'g'},
                {'y', 'y', 'y', 'h', 'w', 'w', 'w', 'w', 'h', 'g', 'g', 'g'},
                {'y', 'y', '+', 'h', 'w', '+', '+', 'w', 'h', '+', 'g', 'g'},
                {'y', 'y', 'y', 'h', 'h', 'h', 'h', 'h', 'h', 'g', 'g', 'g'},
                {'t', 't', 't', 'o', '+', 'h', 'h', '+', 'v', 't', 't', 't'},
                {'t', 't', 't', 'o', 'o', 'h', 'h', 'v', 'v', 't', 't', 't'},
        };
        // initialize room grid
        initRoomGrid();

        // Ai thief starts in a random valid room
        int[] start = randomRoomTile();
        this.thiefRow = start[0];
        this.thiefCol = start[1];

        // camera system
        this.cameras = new boolean[NUM_ROWS][NUM_COLS];
        this.alarmsTriggered = new boolean[NUM_ROWS * NUM_COLS];
        this.movementRoll = 0;
        this.questionRoll = 0;

        // game status
        this.gameOver = false;
        this.winnerId = -1;

        // AI thief
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
        this.questionDieUsed = orig.questionDieUsed;
        this.movementDieUsed = orig.movementDieUsed;
        // deep copy painting positions array
        this.paintingPositions = orig.paintingPositions.clone();

        this.thiefVisible = orig.thiefVisible;
        this.thiefRow = orig.thiefRow;
        this.thiefCol = orig.thiefCol;

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
        this.cameraCount = 0;

        this.alarmsTriggered = orig.alarmsTriggered.clone();
        this.movementRoll = orig.movementRoll;
        this.questionRoll = orig.questionRoll;
        this.gameOver = orig.gameOver;
        this.winnerId = orig.winnerId;
        this.lastQuestionAnswer = orig.lastQuestionAnswer;
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
        this.questionDieUsed = orig.questionDieUsed;
        this.movementDieUsed = orig.movementDieUsed;
        // deep copy painting positions — visible to all players
        this.paintingPositions = orig.paintingPositions.clone();

        this.thiefVisible = true;
        // always keep true position internally
        this.thiefRow = orig.thiefRow;
        this.thiefCol = orig.thiefCol;

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
        this.cameraCount = orig.cameraCount;

        this.alarmsTriggered = orig.alarmsTriggered.clone();
        this.movementRoll = orig.movementRoll;
        this.questionRoll = orig.questionRoll;
        this.gameOver = orig.gameOver;
        this.winnerId = orig.winnerId;
        this.lastQuestionAnswer = orig.lastQuestionAnswer;
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
        runThiefAI(); // ends at GUARD_TURN_START now
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
        if (!isValidTile(r, c)) return false;
        if (gameBoard[r][c] == 't') return false; //prevents placing in inaccessible tiles
        if (gameBoard[r][c] == '+') return false; //prevents placing in "doors"
        if (cameras[r][c]) return false; // prevents placing on cameras
        int newPos = r * NUM_COLS + c;
        // prevent stacking paintings
        if (isPaintingAt(r, c)) return false;
        int id = a.getPaintingId(); // 1-indexed (1–9)
        if (paintingPositions[id - 1] != -1) return false; // prevent placing same painting twice
        paintingPositions[id - 1] = newPos; // convert to flat index
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
        if (!isValidTile(r, c)) return false;
        if (gameBoard[r][c] == 't' || gameBoard[r][c] == '+') return false;
        if (cameras[r][c]) return false; // prevent stacking cameras
        if (cameraCount >= 6) return false; // limit to 6 cameras
        cameras[r][c] = true;
        cameraCount++;
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
        // NO LONGER USED FOR GAME LOGIC
        // keep only for compatibility or remove entirely
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
        if (playerTurn != 1 && actingPlayer != playerTurn) return false;
        enterThiefTurn(); // allows thief to continue moving
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
        if (currentPhase != GamePhase.GUARD_TURN_START) return false;
        switch (a.getType()) {
            case MOVEMENT:
                if (movementDieUsed) return false;
                movementRoll = rng.nextInt(6) + 1;
                movementDieUsed = true;
                currentPhase = GamePhase.GUARD_MOVE;
                return true;
            case QUESTION:
                if (questionDieUsed) return false;
                questionRoll = rng.nextInt(6) + 1;
                questionDieUsed = true;
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
        if (movementRoll <= 0) return false; // prevents moving without dice

        int guardIndex = a.getGuardIndex();
        int tr = a.getTargetRow();
        int tc = a.getTargetCol();

        if (guardIndex < 0 || guardIndex >= guardRow.length) return false;
        if (!isValidTile(tr, tc)) return false;

        // prevent moving to inaccessible tiles
        if (!isValidTile(tr, tc)) return false;

        // validate move distance using manhattan distance
        int dist = manhattan(guardRow[guardIndex], guardCol[guardIndex], tr, tc);
        if (dist > movementRoll) return false;

        // enforces walls / path rules
        if (!guardCanReach(guardRow[guardIndex], guardCol[guardIndex], tr, tc, movementRoll)) {
            return false;
        }

        // apply movement
        guardRow[guardIndex] = tr;
        guardCol[guardIndex] = tc;
        updateRoomIds();

        // win condition [only once]
        if (tr == thiefRow && tc == thiefCol) {
            gameOver = true;
            winnerId = guardIndex + 1;
            currentPhase = GamePhase.ENDGAME;
            return true;
        }

        // turn handling
        movementDieUsed = true; // mark movement action as completed
        // if both dice have been used, thief gets a turn
        if (questionDieUsed) {
            // both dice done → thief moves
            movementRoll = 0;
            questionRoll = 0;
            enterThiefTurn();
        } else {
            // still need to roll the question die
            movementRoll = 0;
            currentPhase = GamePhase.GUARD_TURN_START;
        }
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
     * main controller for thief turn
     * - checks if gameOver
     * - runs either smart or dumb AI
     * - applies tile effect after movement [stealPainting, shutCameraOff]\
     * - updates rooms + alarms
     * - resets turn state
     */
    void runThiefAI() {
        System.out.println("BEFORE MOVE: " + thiefRow + "," + thiefCol);
        movementDieUsed = false;
        questionDieUsed = false;
        movementRoll = 0;
        questionRoll = 0;
        if (gameOver) return;
        // selected AI types
        if (aiType == AIType.SMART) {
            runSmartThiefAI();
        } else {
            runDumbThiefAI();
        }
        // applies tile effect after movement
        handleThiefLanding();
        updateRoomIds();
        updateAlarms();
        System.out.println("AFTER MOVE: " + thiefRow + "," + thiefCol);
        // resets turn cleanly
        endThiefTurn();
        // resets dice after next turn
        movementDieUsed = false;
        questionDieUsed = false;
        movementRoll = 0;
        questionRoll = 0;

        System.out.println("THIEF TURN EXECUTED");
        System.out.println("SWITCHING TO GUARD TURN");
        System.out.println("END THIEF TURN → phase=" + currentPhase + " playerTurn=" + playerTurn);


    }

    /**
     * random ai for thief
     * - picks random steps [1-3]
     * - tries random directions
     * - validates moves with basic rules
     * - rejects moves that hit camera / illegal tiles
     * - goes with the first valid move
     */
    void runDumbThiefAI() {

        ArrayList<int[]> moves = getPossibleThiefMoves();

        if (moves.isEmpty()) {
            thiefRow += rng.nextBoolean() ? 1 : -1;
            thiefCol += rng.nextBoolean() ? 1 : -1;
            return;
        }

        // remove unsafe moves
        ArrayList<int[]> safe = new ArrayList<>();

        for (int[] m : moves) {
            int r = m[0];
            int c = m[1];

            if (cameras[r][c]) continue;
            if (!isValidThiefMove(thiefRow, thiefCol, r, c)) continue;

            safe.add(m);
        }

        if (!safe.isEmpty()) {
            int[] chosen = safe.get(rng.nextInt(safe.size()));
            thiefRow = chosen[0];
            thiefCol = chosen[1];
        } else {
            // last resort: pick ANY move
            int[] chosen = moves.get(rng.nextInt(moves.size()));
            thiefRow = chosen[0];
            thiefCol = chosen[1];
        }
    }

    /**
     * main ai controller for thief
     * - generates possible moves
     * - filers moves --> full path validation
     * - evaluates each move --> scoring system
     * - chooses best score move
     * - updates thief position
     */
    void runSmartThiefAI() {

        // ── decide mode ──────────────────────────────────────────────
        boolean inDanger = thiefVisible || isGuardNearby(4);
        boolean hasPaintings = hasRemainingPaintings();

        int[] target;

        if (inDanger) {
            // FLEE — head to nearest exit
            target = bfsNearestExit();
        } else if (hasPaintings) {
            // STEAL — head to nearest painting
            target = bfsNearestPainting();
        } else {
            // WANDER — move away from guards
            target = null;
        }

        // ── get all reachable tiles (up to 3 steps) ──────────────────
        ArrayList<int[]> moves = getPossibleThiefMoves();
        if (moves.isEmpty()) return;

        // ── score each candidate move ─────────────────────────────────
        int bestScore = Integer.MIN_VALUE;
        ArrayList<int[]> bestMoves = new ArrayList<>();

        for (int[] move : moves) {
            int r = move[0];
            int c = move[1];

            if (!isReachableByThief(thiefRow, thiefCol, r, c, 3)) continue;

            int score = scoreSmartMove(r, c, target, inDanger);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }

        if (bestMoves.isEmpty()) {
            // fallback: any legal move
            for (int[] m : moves) {
                if (isReachableByThief(thiefRow, thiefCol, m[0], m[1], 3)) {
                    thiefRow = m[0];
                    thiefCol = m[1];
                    return;
                }
            }
            return;
        }

        // tie-break randomly to avoid predictable behavior
        int[] chosen = bestMoves.get(rng.nextInt(bestMoves.size()));
        thiefRow = chosen[0];
        thiefCol = chosen[1];
    }

    // =====================================================================
    // QUESTIONS
    // =====================================================================

    /**
     * Handles the detective asking a question after rolling the question die.
     * The AI thief answers automatically based on the current game state.
     * Stores the answer string so the detective's popup can display it.
     * After the question is answered, runs the thief AI turn.
     *
     * @param a the ask question action containing the question type
     * @return true if the question was valid and answered
     */
    public boolean makeAskQuestionAction(MuseumCaperAskQuestionAction a) {
        if (currentPhase != GamePhase.GUARD_ASK) return false;

        switch (a.getQuestionType()) {
            case MOTION:
                // thief must reveal their room color unless in hallway
                char tile = gameBoard[thiefRow][thiefCol];
                if (tile == 'h') {
                    lastQuestionAnswer = "The thief is in the hallway — no answer required.";
                } else {
                    lastQuestionAnswer = "The thief is in the " + roomColorName(tile) + " room.";
                }
                break;

            case SCAN:
                // count active cameras and check if any can see the thief
                int activeCount = 0;
                boolean thiefSeen = false;

                for (int r = 0; r < NUM_ROWS; r++) {
                    for (int c = 0; c < NUM_COLS; c++) {

                        if (!cameras[r][c]) continue;
                        activeCount++;
                        if (hasLineOfSight(r, c, thiefRow, thiefCol)) {
                            thiefSeen = true;
                        }
                    }
                }
                lastQuestionAnswer = activeCount + " camera(s) are active.\n" +
                        (thiefSeen ? "A camera CAN see the thief!" : "No camera can see the thief.");
                break;
            case EYE:
                thiefVisible = false;

                for (int i = 0; i < guardRow.length; i++) {
                    int gr = guardRow[i];
                    int gc = guardCol[i];
                    // straight line vision only (no diagonal)
                    if (hasLineOfSight(gr, gc, thiefRow, thiefCol)) {
                        thiefVisible = true;
                        break;
                    }
                }
                lastQuestionAnswer = thiefVisible
                        ? "YES — the thief is visible! The chase begins!"
                        : "No — the thief cannot be seen from here.";
                break;
        }
        questionDieUsed = true;
        currentPhase = GamePhase.DETECTIVE_REVEAL;
        return true;
    }

    /**
     * Called after the detective reads the answer popup.
     * Transitions to the thief's turn and runs the AI.
     *
     * @param a the finish reveal action
     * @return true always
     */
    public boolean makeFinishRevealAction(MuseumCaperFinishRevealAction a) {
        if (currentPhase != GamePhase.DETECTIVE_REVEAL) return false;
        //android.util.Log.d("TURN_DEBUG", "movementDieUsed=" + movementDieUsed + " questionDieUsed=" + questionDieUsed);

        // both dice done then thief moves
        questionDieUsed = true;
        // if both dice have been used  =  thief turn
        if (movementDieUsed) {
            movementRoll = 0;
            questionRoll = 0;
            enterThiefTurn();
        } else {
            // still need to roll the movement die
            currentPhase = GamePhase.GUARD_TURN_START;
        }
        return true;
    }

    /**
     * Returns a human readable color name for a board tile character.
     * Used in question answer strings shown to the detective.
     */
    private String roomColorName(char tile) {
        switch (tile) {
            case 'r': return "Red";
            case 'p': return "Purple";
            case 'b': return "Blue";
            case 'y': return "Yellow";
            case 'g': return "Green";
            case 'w': return "White";
            case 'o': return "Dark Gray";
            case 'v': return "Power";
            default:  return "Unknown";
        }
    }

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
     * Picks a random valid tile inside a colored room for the thief to spawn on.
     * We exclude walls (t), doors (+), hallways (h), and the special rooms
     * (o, v) so the thief always starts somewhere sensible
     *
     * We loop until we find a valid tile, this is fine because most of
     * the board is valid room tiles.
     */
    private int[] randomRoomTile() {
        Random r = new Random();
        while (true) {
            int row = r.nextInt(NUM_ROWS);
            int col = r.nextInt(NUM_COLS);
            char tile = gameBoard[row][col];
            if (tile != 't' && tile != '+' && tile != 'h' && tile != 'o' && tile != 'v') {
                return new int[]{row, col};
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

    /**
     * BFS check to see if the guard can legally reach a target tile
     * within their allowed number of steps.
     *
     * We use BFS (breadth-first search) instead of just Manhattan distance
     * because Manhattan distance ignores walls and doors. BFS actually
     * walks the path step by step and checks each move is legal.
     * @param sr       guard start row
     * @param sc       guard start col
     * @param tr       target row
     * @param tc       target col
     * @param maxSteps maximum steps allowed (from the dice roll)
     * @return true if a valid path exists within maxSteps
     */
    private boolean guardCanReach(int sr, int sc, int tr, int tc, int maxSteps) {
        boolean[][] visited = new boolean[NUM_ROWS][NUM_COLS];
        ArrayList<int[]> frontier = new ArrayList<>();
        frontier.add(new int[]{sr, sc, 0});
        visited[sr][sc] = true;

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        while (!frontier.isEmpty()) {
            int[] cur = frontier.remove(0);
            int r = cur[0], c = cur[1], steps = cur[2];

            if (r == tr && c == tc) return true;
            if (steps == maxSteps) continue;

            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];

                if (!isValidTile(nr, nc)) continue;
                if (visited[nr][nc]) continue;
                if (!isValidTile(nr, nc)) continue;

                // door rules for guard:
                if (!isLegalMove(r, c, nr, nc)) continue;

                visited[nr][nc] = true;
                frontier.add(new int[]{nr, nc, steps + 1});
            }
        }
        return false;
    }

    /**
     * generates possible legal moves for thief
     * uses BFS expansion :
     *  - thief can move up to 3 tiles per turn
     *  - no diagonal moves
     *  - stops action of revisiting tiles
     * @return list of valid destination coordinates
     */
    /**
     * External Cite : Co-Pilot
     * There was a problem with the thief "moving" but it actually
     * stayed in the same coordinates again and again
     * solution : suggested the use of BFS expansion to
     * avoid generating "stay still moves"
     */
    private ArrayList<int[]> getPossibleThiefMoves() {
        ArrayList<int[]> moves = new ArrayList<>();

        int[][] dirs = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}
        };
        ArrayList<int[]> frontier = new ArrayList<>();
        frontier.add(new int[]{thiefRow, thiefCol});

        boolean[][] visited = new boolean[NUM_ROWS][NUM_COLS];
        visited[thiefRow][thiefCol] = true;
        for (int step = 0; step < 3; step++) {
            ArrayList<int[]> next = new ArrayList<>();
            for (int[] node : frontier) {

                int r = node[0];
                int c = node[1];

                for (int[] d : dirs) {

                    int nr = r + d[0];
                    int nc = c + d[1];

                    if (!isValidTile(nr, nc)) continue;
                    if (visited[nr][nc]) continue;

                    // use thief validation, so it actually moves
                    if (!isValidThiefMove(r, c, nr, nc)) continue;

                    visited[nr][nc] = true;
                    moves.add(new int[]{nr, nc});
                    next.add(new int[]{nr, nc});
                }
            }
            frontier = next;
        }
        return moves;
    }

    /**
     * switches the game state to the thief's turn and then
     * runs the thief AI, this is called after detective finishes
     * rolling both dices
     */
    private void enterThiefTurn() {
        playerTurn = 0;
        currentPhase = GamePhase.THIEF_TURN;
        runThiefAI();
    }

    /**
     * ends the thief's turn and hands control back to the detective
     * resets dice usage + transitions to guard turn start
     */
    private void endThiefTurn() {
        playerTurn = 1; // detective
        currentPhase = GamePhase.GUARD_TURN_START;

        movementDieUsed = false;
        questionDieUsed = false;
        movementRoll = 0;
        questionRoll = 0;
    }

    /**
     * AI evaluation function - scores on tiles for thief
     * = higher the score --> more likely to go towards spot
     * = thief actually moves around
     * @param r
     * @param c
     * @return
     *
     * External Cite : Chatgpt
     * did not know how to control making the AI more likely to
     * move towards paintings + cameras
     * solution : suggested using a point system --> like a game
     * for AI
    private int evaluatePositionSmart(int r, int c) {
        int score = 0;

        char tile = gameBoard[r][c];

        // main setpoint is paintings
        int nearestPaintingDist = Integer.MAX_VALUE;

        for (int i = 0; i < paintingPositions.length; i++) {
            if (paintingPositions[i] == -1) continue; // not placed
            if (stolenPaintings.contains(i)) continue; // already stolen

            int pr = paintingPositions[i] / NUM_COLS;
            int pc = paintingPositions[i] % NUM_COLS;

            int dist = Math.abs(pr - r) + Math.abs(pc - c);
            nearestPaintingDist = Math.min(nearestPaintingDist, dist);
        }
        if (nearestPaintingDist < Integer.MAX_VALUE) {
            score += 200 - nearestPaintingDist * 10;
        }
        // goes more for turning cameras off
        if (cameras[r][c]) {
            score += 150; // stepping on a camera disables it
        }
        // avoids guard
        for (int i = 0; i < guardRow.length; i++) {
            int dist = Math.abs(guardRow[i] - r) + Math.abs(guardCol[i] - c);
            if (dist <= 1) score -= 500; // danger zone
            else score -= (10 / dist);  // mild penalty
        }
        // avoids active cameras
        if (alarmsTriggered[r * NUM_COLS + c]) {
            score -= 300;
        }
        // encourages leaving room
        if (tile == 'h') score += 50; // hallways are good for travel

        // small randomness to avoid getting stuck
        score += rng.nextInt(5);

        return score;
    }
    */



    /**
     * handles what happens when thief lands on tile
     * - steal a painting
     * - cut camera
     * - checks thief's win conditions
     */
    private void handleThiefLanding() {
        // disable camera on tile
        if (cameras[thiefRow][thiefCol]) {
            cameras[thiefRow][thiefCol] = false;
        }
        // check painting steal
        for (int i = 0; i < paintingPositions.length; i++) {

            if (paintingPositions[i] == -1) continue;

            int pr = paintingPositions[i] / NUM_COLS;
            int pc = paintingPositions[i] % NUM_COLS;

            if (thiefRow == pr && thiefCol == pc) {

                int paintingId = i + 1;

                // safety: prevent duplicates
                if (!stolenPaintings.contains(paintingId)) {
                    stolenPaintings.add(paintingId);
                }

                paintingPositions[i] = -1;

                // win check (ONLY HERE)
                if (stolenPaintings.size() >= 3) {
                    gameOver = true;
                    winnerId = 0;
                    currentPhase = GamePhase.ENDGAME;
                }
                return; // prevents double-processing same tile
            }
        }
    }
    /**
     * Returns true if the tile is inside the board AND not a wall.
     * This is the universal "can step here" check for guards and thief.
     */
    private boolean isValidTile(int r, int c) {
        if (r < 0 || r >= NUM_ROWS || c < 0 || c >= NUM_COLS) return false;
        if (gameBoard[r][c] == 't') return false; // wall
        return true;
    }

    /**
     * Returns true if there is straight-line (orthogonal) vision
     * between (r1,c1) and (r2,c2) with no walls in between.
     */
    private boolean hasLineOfSight(int r1, int c1, int r2, int c2) {

        // must be same row or same column
        if (r1 != r2 && c1 != c2) return false;

        // horizontal scan
        if (r1 == r2) {
            int start = Math.min(c1, c2);
            int end   = Math.max(c1, c2);
            for (int c = start + 1; c < end; c++) {
                if (gameBoard[r1][c] == 't') return false; // wall blocks view
            }
            return true;
        }

        // vertical scan
        if (c1 == c2) {
            int start = Math.min(r1, r2);
            int end   = Math.max(r1, r2);
            for (int r = start + 1; r < end; r++) {
                if (gameBoard[r][c1] == 't') return false; // wall blocks view
            }
            return true;
        }

        return false;
    }

    /**
     * Thief-specific movement validator.
     * Does NOT use movementRoll.
     * Does NOT modify movementRoll.
     * Does NOT interfere with guard movement.
     */
    private boolean isValidThiefMove(int r1, int c1, int r2, int c2) {

        if (!isValidTile(r2, c2)) return false;

        char start = gameBoard[r1][c1];
        char end   = gameBoard[r2][c2];

        boolean startRoom = (start != 'h' && start != '+' && start != 't');
        boolean endRoom   = (end   != 'h' && end   != '+' && end   != 't');

        boolean startDoor = (start == '+');
        boolean endDoor   = (end   == '+');

        boolean startHall = (start == 'h');
        boolean endHall   = (end   == 'h');

        if (r1 == r2 && c1 == c2) return false;

        // room → room (same room)
        if (startRoom && endRoom && start == end) return true;

        // room → door
        if (startRoom && endDoor) {
            return isDoorForRoom(r2, c2, start);
        }

        // door → hallway
        if (startDoor && endHall) return true;

        // hallway → door
        if (startHall && endDoor) return true;

        // door → room
        if (startDoor && endRoom) {
            return isDoorForRoom(r1, c1, end);
        }
        // hallway to hallway
        if (startHall && endHall) return true;

        return false;
    }

    /**
     * checks whether a move from one tile to another is legal
     * @param r1
     * @param c1
     * @param r2
     * @param c2
     * @return
     */
    private boolean isLegalMove(int r1, int c1, int r2, int c2) {

        if (!isValidTile(r2, c2)) return false;

        char start = gameBoard[r1][c1];
        char end   = gameBoard[r2][c2];

        // must be adjacent
        if (Math.abs(r1 - r2) + Math.abs(c1 - c2) != 1) return false;

        // hallway ↔ hallway
        if (start == 'h' && end == 'h') return true;

        // room ↔ same room
        if (start != 'h' && end != 'h' && start == end) return true;

        // room → door
        if (start != 'h' && end == '+') return true;

        // door → hallway
        if (start == '+' && end == 'h') return true;

        // hallway → door
        if (start == 'h' && end == '+') return true;

        // door → room
        if (start == '+' && end != 'h' && end != '+') return true;

        return false;
    }

    /**
     * returns true if row and col is a valid
     * door tile for the given room type
     */
    private boolean isDoorForRoom(int r, int c, char room) {
        switch (room) {
            case 'r': return (r == 1 && (c == 4 || c == 7));
            case 'p': return (r == 4 && c == 2);
            case 'b': return (r == 3 && c == 9);
            case 'y': return (r == 7 && c == 2);
            case 'g': return (r == 7 && c == 9);
            case 'w': return (r == 3 && (c == 5 || c == 6)) || (r == 7 && (c == 5 || c == 6));
            case 'o': return (r == 9 && c == 4);
            case 'v': return (r == 9 && c == 7);
            default: return false;
        }
    }

    /**
     * helper method for humanPlayer
     * it recorded a camera's ID + position in the game
     * = board and game logic know it exists
     * @param id
     * @param row
     * @param col
     */
    public boolean placeCamera(int id, int row, int col) {

        if (!isValidTile(row, col)) return false;
        if (gameBoard[row][col] == 't' || gameBoard[row][col] == '+') return false;
        if (cameras[row][col]) return false;

        // enforce limit
        if (cameraCount >= 6) return false;

        cameras[row][col] = true;
        cameraCount++;

        return true;
    }

    /**
     * helper method for checking painting logic
     * @param r
     * @param c
     * @return
     */
    private boolean isPaintingAt(int r, int c) {
        int pos = r * NUM_COLS + c;

        for (int p : paintingPositions) {
            if (p == pos) return true;
        }
        return false;
    }

    private int scoreSmartMove(int r, int c, int[] target, boolean fleeing) {
        int score = 0;

        if (target != null) {
            int distToTarget = manhattan(r, c, target[0], target[1]);
            score -= distToTarget * (fleeing ? 40 : 35);
        }

        if (isPaintingAt(r, c)) {
            score += 1000;
        }
        // bonus for being in hallway when heading to a painting in another room
        if (gameBoard[r][c] == 'h' && !fleeing) score += 60;

        for (int i = 0; i < guardRow.length; i++) {
            int dist = manhattan(r, c, guardRow[i], guardCol[i]);
            if (dist == 0) return -100000;
            if (dist == 1) score -= 800;
            else if (dist == 2) score -= 300;
            else if (dist == 3) score -= 100;
            else score += dist * 10;
        }

        if (cameras[r][c]) {
            if (fleeing) score += 200;
            else score -= 400;
        }

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (isValidTile(nr, nc) && cameras[nr][nc]) {
                score -= 100;
            }
        }

        if (gameBoard[r][c] == 'h' && fleeing) score += 80;

        if (!fleeing && target == null) {
            int minGuardDist = Integer.MAX_VALUE;
            for (int i = 0; i < guardRow.length; i++) {
                minGuardDist = Math.min(minGuardDist,
                        manhattan(r, c, guardRow[i], guardCol[i]));
            }
            score += minGuardDist * 15;
        }

        score += rng.nextInt(5);
        return score;
    }

    private boolean isGuardNearby(int manhattanDist) {
        for (int i = 0; i < guardRow.length; i++) {
            if (manhattan(thiefRow, thiefCol, guardRow[i], guardCol[i]) <= manhattanDist) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRemainingPaintings() {
        for (int pos : paintingPositions) {
            if (pos != -1) return true;
        }
        return false;
    }

    private int[] bfsNearestPainting() {
        boolean[][] visited = new boolean[NUM_ROWS][NUM_COLS];
        ArrayList<int[]> queue = new ArrayList<>();
        queue.add(new int[]{thiefRow, thiefCol});
        visited[thiefRow][thiefCol] = true;

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};

        while (!queue.isEmpty()) {
            int[] cur = queue.remove(0);
            int r = cur[0], c = cur[1];

            if (isPaintingAt(r, c)) return new int[]{r, c};

            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (!isValidTile(nr, nc)) continue;
                if (visited[nr][nc]) continue;
                if (!isValidThiefMove(r, c, nr, nc)) continue;
                visited[nr][nc] = true;
                queue.add(new int[]{nr, nc});
            }
        }
        return null;
    }

    private int[] bfsNearestExit() {
        boolean[][] visited = new boolean[NUM_ROWS][NUM_COLS];
        ArrayList<int[]> queue = new ArrayList<>();
        queue.add(new int[]{thiefRow, thiefCol});
        visited[thiefRow][thiefCol] = true;

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};

        while (!queue.isEmpty()) {
            int[] cur = queue.remove(0);
            int r = cur[0], c = cur[1];

            if (isExitTile(r, c)) return new int[]{r, c};

            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (!isValidTile(nr, nc)) continue;
                if (visited[nr][nc]) continue;
                if (!isValidThiefMove(r, c, nr, nc)) continue;
                visited[nr][nc] = true;
                queue.add(new int[]{nr, nc});
            }
        }
        return null;
    }

    private boolean isExitTile(int r, int c) {
        if (gameBoard[r][c] != 'h') return false;
        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};
        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr >= 0 && nr < NUM_ROWS && nc >= 0 && nc < NUM_COLS) {
                if (gameBoard[nr][nc] == 't') return true;
            }
        }
        return false;
    }

    private boolean isReachableByThief(int sr, int sc, int tr, int tc, int maxSteps) {
        boolean[][] visited = new boolean[NUM_ROWS][NUM_COLS];
        ArrayList<int[]> queue = new ArrayList<>();
        queue.add(new int[]{sr, sc, 0});
        visited[sr][sc] = true;

        int[][] dirs = {{-1,0},{1,0},{0,-1},{0,1}};

        while (!queue.isEmpty()) {
            int[] cur = queue.remove(0);
            int r = cur[0], c = cur[1], steps = cur[2];

            if (r == tr && c == tc) return true;
            if (steps >= maxSteps) continue;

            for (int[] d : dirs) {
                int nr = r + d[0];
                int nc = c + d[1];
                if (!isValidTile(nr, nc)) continue;
                if (visited[nr][nc]) continue;
                if (!isValidThiefMove(r, c, nr, nc)) continue;
                visited[nr][nc] = true;
                queue.add(new int[]{nr, nc, steps + 1});
            }
        }
        return false;
    }
    // =====================================================================
    // GETTERS
    // =====================================================================

    public char[][] getGameBoard() { return gameBoard; }
    public String getLastQuestionAnswer() { return lastQuestionAnswer; }
    public int[] getPaintingPositions() { return paintingPositions.clone(); }
    public int getThiefRow() { return thiefRow; }
    public int getThiefCol() { return thiefCol; }
    public int[] getGuardRow() { return guardRow.clone(); }
    public boolean isMovementDieUsed() { return movementDieUsed; }
    public boolean isQuestionDieUsed()  { return questionDieUsed; }
    public int[] getGuardCol() { return guardCol.clone(); }
    public int getGuardRow(int guardIndex) { return guardRow[guardIndex]; }
    public int getGuardCol(int guardIndex) { return guardCol[guardIndex]; }
    public boolean[][] getCameras()
    {
        boolean[][] copy = new boolean[NUM_ROWS][NUM_COLS];
        for(int r = 0; r < NUM_ROWS; r++)
        {
            copy[r] = cameras[r].clone();
        }
        return copy;
    }
    public boolean[] getAlarmTriggered() { return alarmsTriggered.clone(); }
    public int getCameraCount() { return cameraCount; }
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
    public int getThiefRow(int playerId) {
        if (playerId != 0 && !thiefVisible) return -1;
        return thiefRow;
    }
    public int getThiefCol(int playerId) {
        if (playerId != 0 && !thiefVisible) return -1;
        return thiefCol;
    }

    // =====================================================================
    // SETTERS
    // =====================================================================

    /** @param i the player turn to set (0 = thief, 1 = detective) */
    public void setPlayerTurn(int i) { playerTurn = i; }

    /** @param type the AI type to set */
    public void setAIType(AIType type) { aiType = type; }

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

    /**
     * helper method for humanPlayer
     * identifies if painting has already been placed on board
     * @param id
     * @return paintingPosition
     */
    public boolean isPaintingPlaced(int id) {
        return paintingPositions[id - 1] != -1;
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