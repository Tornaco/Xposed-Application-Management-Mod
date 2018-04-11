package github.tornaco.xposedmoduletest.xposed.service.opt.gcm;

import android.content.Context;

import github.tornaco.xposedmoduletest.xposed.repo.SettingsProvider;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Tornaco on 2018/4/10 17:31.
 * God bless no bug!
 */
@Getter
@Setter
abstract class BasePushNotificationHandler implements PushNotificationHandler {
    private Context context;

    private int badge;

    BasePushNotificationHandler(Context context) {
        this.context = context;
        readSettings(getTag());
    }

    private boolean enabled, showContentEnabled;

    @Override
    public void onSettingsChanged(String tag) {
        readSettings(tag);

        XposedLog.verbose("onSettingsChanged: %s enabled: %s, showContentEnabled:%s",
                getClass(),
                isEnabled(),
                isShowContentEnabled());
    }

    private void readSettings(String tag) {
        setEnabled(SettingsProvider.get().getBoolean(getTag(), false));
        // FIXME any means all, should be specific.
        setShowContentEnabled(SettingsProvider.get().getBoolean("any" + "_show_content", false));
    }

    @Override
    public void onTopPackageChanged(String who) {
        if (getTargetPackageName().equals(who)) {
            XposedLog.verbose("onTopPackageChanged: %s", getClass());
            // Reset badge.
            setBadge(0);
        }
    }
}
