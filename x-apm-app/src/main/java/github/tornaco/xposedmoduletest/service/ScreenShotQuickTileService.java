package github.tornaco.xposedmoduletest.service;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.StatusBarManager;
import android.os.Build;
import android.os.Handler;
import android.service.quicksettings.TileService;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2018/1/28.
 * Email: Tornaco@163.com
 */

@TargetApi(Build.VERSION_CODES.N)
public class ScreenShotQuickTileService extends TileService {

    private static final String LOCAL_STATUS_BAR_SERVICE = "statusbar";

    @Override
    public void onClick() {
        super.onClick();
        // Expand status.
        @SuppressLint("WrongConstant") StatusBarManager statusBarManager = (StatusBarManager) getSystemService(LOCAL_STATUS_BAR_SERVICE);
        if (statusBarManager != null) {
            statusBarManager.collapsePanels();
        }
        new Handler().postDelayed(() -> XAPMManager.get().takeLongScreenShot(), 1000);
    }
}
