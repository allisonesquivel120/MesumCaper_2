package edu.up.cs301.museumcaper;

import java.util.ArrayList;
import edu.up.cs301.GameFramework.GameMainActivity;
import edu.up.cs301.GameFramework.infoMessage.GameState;
import edu.up.cs301.GameFramework.players.GamePlayer;
import edu.up.cs301.GameFramework.LocalGame;
import edu.up.cs301.GameFramework.gameConfiguration.*;

/**
 * The main activity for Museum Caper.
 * Configures the players and creates the local game instance.
 *
 * Player setup:
 * - Player 0: Thief (AI) — runs automatically via MuseumCaperComputerPlayer1
 * - Player 1: Detective (Human) — controlled by the human via MuseumCaperHumanPlayer
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 * @version March 2026
 */
public class MuseumCaperMainActivity extends GameMainActivity {

    // port number used when playing over the network
    private static final int PORT_NUMBER = 2234;

    /**
     * Creates the default player configuration for the game.
     * Sets up one AI thief and one human detective.
     *
     * @return the default GameConfig object
     */
    @Override
    public GameConfig createDefaultConfig() {
        ArrayList<GamePlayerType> playerTypes = new ArrayList<>();

        // player 0 = thief (AI) — movement handled automatically by MuseumCaperComputerPlayer1
        playerTypes.add(new GamePlayerType("Thief (AI)") {
            public GamePlayer createPlayer(String name) {
                return new MuseumCaperComputerPlayer1(name, 0);
            }});

        // player 1 = detective (human) — uses the GUI to roll dice and move
        playerTypes.add(new GamePlayerType("Detective (Human)") {
            public GamePlayer createPlayer(String name) {
                return new MuseumCaperHumanPlayer(name, 1);
            }});

        GameConfig defaultConfig = new GameConfig(playerTypes, 2, 2, "Museum Caper", PORT_NUMBER);
        defaultConfig.addPlayer("Thief", 0);      // AI thief
        defaultConfig.addPlayer("Detective", 1);  // human detective
        defaultConfig.setRemoteData("Remote Player", "", 1);
        return defaultConfig;
    }

    /**
     * Creates a new local game instance.
     * If no existing state is provided, initializes a fresh 2-player game.
     *
     * @param state an existing game state to restore, or null for a new game
     * @return a new MuseumCaperLocalGame instance
     */
    @Override
    public LocalGame createLocalGame(GameState state) {
        if (state == null) state = new MuseumCaperState(2);
        return new MuseumCaperLocalGame(state);
    }
}