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

public class AppPrivacySetting extends AppSettingsSwitchTile {

    public AppPrivacySetting(@NonNull Context context, AppSettings appSettings) {
        super(context, appSettings);
        this.titleRes = R.string.title_privacy;
        this.iconRes = R.drawable.ic_track_changes_black_24dp;
    }

    @Override
    boolean getSwitchState() {
        return getAppSettings().isPrivacy();
    }

    @Override
    void applySwitchState(boolean checked) {
        super.applySwitchState(checked);
        getAppSettings().setPrivacy(checked);
        XAPMManager.get()
                .addOrRemoveFromPrivacyList(getAppSettings().getPkgName(),
                        checked ? XAPMManager.Op.ADD : XAPMManager.Op.REMOVE);
    }
}
