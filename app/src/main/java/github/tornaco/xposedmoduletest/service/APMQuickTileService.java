package github.tornaco.xposedmoduletest.service;

import android.annotation.TargetApi;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2018/1/28.
 * Email: Tornaco@163.com
 */

@TargetApi(Build.VERSION_CODES.N)
public class APMQuickTileService extends TileService {

    @Override
    public void onClick() {
        super.onClick();

        // Disable/Enable activity info view.
        boolean enabled;
        if (XAshmanManager.get().isServiceAvailable()) {
            enabled = XAshmanManager.get().showFocusedActivityInfoEnabled();
            boolean newEnabled = !enabled;
            XAshmanManager.get().setShowFocusedActivityInfoEnabled(newEnabled);
            getQsTile().setState(newEnabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        } else {
            getQsTile().setState(Tile.STATE_INACTIVE);
        }

    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        Logger.d("onStartListening");
        if (XAshmanManager.get().isServiceAvailable()) {
            getQsTile().setState(XAshmanManager.get().isServiceAvailable() && XAshmanManager.get().showFocusedActivityInfoEnabled() ?
                    Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        } else {
            getQsTile().setState(Tile.STATE_INACTIVE);
        }
    }
}
