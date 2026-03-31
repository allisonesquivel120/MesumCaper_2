package edu.up.cs301.museumcaper;

import edu.up.cs301.GameFramework.actionMessage.GameAction;
import edu.up.cs301.GameFramework.players.GamePlayer;

public class MuseumCaperPlacePaintingAction extends GameAction {
    private final int paintingId; // which painting (1 to 9)
    private final int row;
    private final int col;

    public MuseumCaperPlacePaintingAction(GamePlayer player, int paintingId, int row, int col) {
        super(player);
        this.paintingId = paintingId;
        this.row = row;
        this.col = col;
    }

    public int getPaintingId() { return paintingId; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}