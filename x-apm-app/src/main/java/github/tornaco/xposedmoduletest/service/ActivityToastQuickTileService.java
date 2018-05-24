package github.tornaco.xposedmoduletest.service;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2018/1/28.
 * Email: Tornaco@163.com
 */

@TargetApi(Build.VERSION_CODES.N)
public class ActivityToastQuickTileService extends TileService {

    @Override
    public void onClick() {
        super.onClick();

        // Disable/Enable activity info view.
        boolean enabled;
        if (XAPMManager.get().isServiceAvailable()) {
            enabled = XAPMManager.get().showFocusedActivityInfoEnabled();
            boolean newEnabled = !enabled;
            XAPMManager.get().setShowFocusedActivityInfoEnabled(newEnabled);
            getQsTile().setState(newEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        } else {
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Logger.d("onStartListening");
        if (XAPMManager.get().isServiceAvailable()) {
            getQsTile().setState(XAPMManager.get().isServiceAvailable()
                    && XAPMManager.get().showFocusedActivityInfoEnabled() ?
                    Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        } else {
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }
}
