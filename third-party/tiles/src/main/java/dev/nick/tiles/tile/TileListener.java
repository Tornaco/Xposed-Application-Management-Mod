package dev.nick.tiles.tile;

import android.support.annotation.NonNull;

public interface TileListener {
    void onTileClick(@NonNull QuickTile tile);
    void onTileLongClick(@NonNull QuickTile tile);
}
