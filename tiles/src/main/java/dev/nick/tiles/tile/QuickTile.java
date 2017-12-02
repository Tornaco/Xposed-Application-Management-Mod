package dev.nick.tiles.tile;

import android.content.Context;
import android.support.annotation.NonNull;

public abstract class QuickTile extends Tile {

    private Context mContext;
    private TileListener mListener;

    public QuickTile(@NonNull Context context, TileListener listener) {
        mContext = context;
        mListener = listener;
    }

    public QuickTile(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    TileListener getListener() {
        return mListener;
    }

    public void setEnabled(boolean enabled) {
        if (getTileView() != null)
            getTileView().setEnabled(enabled);
    }
}
