package edu.up.cs301.museumcaper;

import android.view.View;

public interface OnPieceDroppedListener {
    void onPieceDropped(View piece, int row, int col);
}