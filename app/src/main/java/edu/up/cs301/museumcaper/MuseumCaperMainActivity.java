package edu.up.cs301.museumcaper;

import java.util.ArrayList;

import edu.up.cs301.GameFramework.GameMainActivity;
import edu.up.cs301.GameFramework.infoMessage.GameState;
import edu.up.cs301.GameFramework.players.GamePlayer;
import edu.up.cs301.GameFramework.LocalGame;
import edu.up.cs301.GameFramework.gameConfiguration.*;

/**
 * this is the primary activity for Counter game
 *
 * @author Farid S.
 * @author Jayden H.
 * @author Allison E.
 * @version March 2026
 */
public class MuseumCaperMainActivity extends GameMainActivity {


// the port number that this game will use when playing over the network
	private static final int PORT_NUMBER = 2234;

	/**
	 * Create the default configuration for this game:
	 * - one human player vs. one computer player
	 * - minimum of 1 player, maximum of 2
	 * - one kind of computer player and one kind of human player available
	 *
	 * @return
	 * 		the new configuration object, representing the default configuration
	 */
	@Override
	public GameConfig createDefaultConfig() {

		// Define the allowed player types
		ArrayList<GamePlayerType> playerTypes = new ArrayList<GamePlayerType>();

		// player 0 = thief (computer AI)
		playerTypes.add(new GamePlayerType("Thief (AI)") {
			public GamePlayer createPlayer(String name) {
				return new MuseumCaperHumanPlayer(name,-1);
			}});

        // player 1 = detective (human)
		playerTypes.add(new GamePlayerType("Detective (Human)") {
			public GamePlayer createPlayer(String name) {
				return new MuseumCaperComputerPlayer1(name,1);
			}});

        // player 2 = detective (human)
		//playerTypes.add(new GamePlayerType("Detective 2 (Human)") {
		//	public GamePlayer createPlayer(String name) {
		//		return new MuseumCaperComputerPlayer2(name,0);
		//	}});


		GameConfig defaultConfig = new GameConfig(playerTypes, 2, 2, "Museum Caper",
				PORT_NUMBER);

		// Add the default players to the configuration
		defaultConfig.addPlayer("Thief", 0); // player 1: thief AI
        defaultConfig.addPlayer("Detective", 1); // player 2: human detective

		// Set the default remote-player setup:
		// - player name: "Remote Player"
		// - IP code: (empty string)
		// - default player type: human player
		defaultConfig.setRemoteData("Remote Player", "", 1);

		// return the configuration
		return defaultConfig;
	}//createDefaultConfig

	/**
	 * create a local game
	 *
	 * @return
	 * 		the local game, a counter game
	 */
	@Override
	public LocalGame createLocalGame(GameState state) {
		if (state == null) state = new MuseumCaperState(2);
		return new MuseumCaperLocalGame(state);
	}

}
