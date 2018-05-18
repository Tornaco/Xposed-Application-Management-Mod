package github.tornaco.xposedmoduletest.ui.tiles.app.per;

import android.content.Context;
import android.support.annotation.NonNull;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */

public class AppLockSetting extends AppSettingsSwitchTile {

    public AppLockSetting(@NonNull Context context, AppSettings appSettings) {
        super(context, appSettings);
        this.titleRes = R.string.title_app_guard;
        this.iconRes = R.drawable.ic_lock_black_24dp;
    }

    @Override
    boolean getSwitchState() {
        return getAppSettings().isApplock();
    }

    @Override
    void applySwitchState(boolean checked) {
        super.applySwitchState(checked);
        getAppSettings().setApplock(checked);
        XAppLockManager.get()
                .addOrRemoveLockApps(new String[]{getAppSettings().getPkgName()},
                        checked);
    }
}
