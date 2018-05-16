package github.tornaco.xposedmoduletest.ui.tiles.app.per;

import android.content.Context;
import android.support.annotation.NonNull;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */

public class AppWakeLockSetting extends AppSettingsSwitchTile {

    public AppWakeLockSetting(@NonNull Context context, AppSettings appSettings) {
        super(context, appSettings);
        this.titleRes = R.string.app_ops_wakelock;
        this.iconRes = R.drawable.ic_memory_black_24dp;
    }

    @Override
    boolean getSwitchState() {
        return getAppSettings().isWakeLock();
    }

    @Override
    void applySwitchState(boolean checked) {
        super.applySwitchState(checked);
        getAppSettings().setWakeLock(checked);
        XAPMManager.get().setPermissionControlBlockModeForPkg(XAppOpsManager.OP_WAKE_LOCK,
                getAppSettings().getPkgName(),
                checked ? XAppOpsManager.MODE_IGNORED : XAppOpsManager.MODE_ALLOWED);
    }
}
