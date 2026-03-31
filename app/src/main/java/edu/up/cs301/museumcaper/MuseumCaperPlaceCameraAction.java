package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;
/**
 * To place a camera on the board.
 *
 * @author Allison E.
 * @author Jayden H.
 * @author Farid S.
 * @version Feb 2026
 */
public class MuseumCaperPlaceCameraAction extends GameAction {
    private final int cameraId; // which camera (1 to 6)
    private final int row;
    private final int col;

    public MuseumCaperPlaceCameraAction(GamePlayer player, int cameraId, int row, int col) {
        super(player);
        this.cameraId = cameraId;
        this.row = row;
        this.col = col;
    }

    public int getCameraId() { return cameraId; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}