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

public class AppBlurSetting extends AppSettingsSwitchTile {

    public AppBlurSetting(@NonNull Context context, AppSettings appSettings) {
        super(context, appSettings);
        this.titleRes = R.string.title_app_recent_blur;
        this.iconRes = R.drawable.ic_blur_on_black_24dp;
    }

    @Override
    boolean getSwitchState() {
        return getAppSettings().isBlur();
    }

    @Override
    void applySwitchState(boolean checked) {
        super.applySwitchState(checked);
        getAppSettings().setBlur(checked);
        XAppLockManager.get()
                .addOrRemoveBlurApps(new String[]{getAppSettings().getPkgName()},
                        checked);
    }
}
