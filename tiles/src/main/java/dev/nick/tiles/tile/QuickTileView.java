package dev.nick.tiles.tile;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class QuickTileView extends TileView {

    private QuickTile mTile;

    public QuickTileView(Context context, QuickTile tile) {
        super(context);
        mTile = tile;
    }

    public QuickTileView(Context context, AttributeSet attrs, QuickTile tile) {
        super(context, attrs);
        mTile = tile;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (mTile.getListener() != null) {
            mTile.getListener().onTileClick(mTile);
        }
    }
}
