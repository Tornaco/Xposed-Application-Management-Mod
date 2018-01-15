package github.tornaco.xposedmoduletest.ui.tiles.app.per;

import android.content.Context;
import android.support.annotation.NonNull;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */

public class AppBootSetting extends AppSettingsSwitchTile {

    public AppBootSetting(@NonNull Context context, AppSettings appSettings) {
        super(context, appSettings);
        this.titleRes = R.string.title_app_boot;
        this.iconRes = R.drawable.ic_directions_car_black_24dp;
    }

    @Override
    boolean getSwitchState() {
        return getAppSettings().isBoot();
    }

    @Override
    void applySwitchState(boolean checked) {
        super.applySwitchState(checked);
        XAshmanManager.get()
                .addOrRemoveBootBlockApps(new String[]{getAppSettings().getPkgName()},
                        checked ? XAshmanManager.Op.ADD : XAshmanManager.Op.REMOVE);
    }
}
