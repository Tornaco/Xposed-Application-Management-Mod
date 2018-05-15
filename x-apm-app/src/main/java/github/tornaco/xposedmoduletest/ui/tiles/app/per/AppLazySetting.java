package github.tornaco.xposedmoduletest.ui.tiles.app.per;

import android.content.Context;
import android.support.annotation.NonNull;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */

public class AppLazySetting extends AppSettingsSwitchTile {

    public AppLazySetting(@NonNull Context context, AppSettings appSettings) {
        super(context, appSettings);
        this.titleRes = R.string.title_app_lazy;
        this.summaryRes = R.string.summary_exp;
        this.iconRes = R.drawable.ic_child_care_black_24dp;
    }

    @Override
    boolean getSwitchState() {
        return getAppSettings().isLazy();
    }

    @Override
    void applySwitchState(boolean checked) {
        super.applySwitchState(checked);
        getAppSettings().setLazy(checked);
        XAPMManager.get()
                .addOrRemoveLazyApps(new String[]{getAppSettings().getPkgName()},
                        checked ? XAPMManager.Op.ADD : XAPMManager.Op.REMOVE);
    }
}
