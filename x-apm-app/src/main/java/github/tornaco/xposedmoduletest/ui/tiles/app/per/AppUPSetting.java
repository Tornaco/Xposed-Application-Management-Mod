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

public class AppUPSetting extends AppSettingsSwitchTile {

    public AppUPSetting(@NonNull Context context, AppSettings appSettings) {
        super(context, appSettings);
        this.titleRes = R.string.title_app_uninstall_pro;
        this.iconRes = R.drawable.ic_do_not_disturb_on_black_24dp;
    }

    @Override
    boolean getSwitchState() {
        return getAppSettings().isUninstall();
    }

    @Override
    void applySwitchState(boolean checked) {
        super.applySwitchState(checked);
        getAppSettings().setUninstall(checked);
        XAppLockManager.get()
                .addOrRemoveUPApps(new String[]{getAppSettings().getPkgName()},
                        checked);
    }
}
