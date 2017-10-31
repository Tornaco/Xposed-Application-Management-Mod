package github.tornaco.xposedmoduletest.x;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;

import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import github.tornaco.apigen.BuildHostInfo;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.license.XActivation;
import github.tornaco.xposedmoduletest.ui.GuardAppNavActivity;
import github.tornaco.xposedmoduletest.ui.GuardAppNavActivityNoLauncher;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
@BuildHostInfo
public class XApp extends Application {

    @SuppressLint("StaticFieldLeak")
    private static XApp xApp;

    public static XApp getApp() {
        return xApp;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        xApp = this;
        Logger.config(Settings.builder().tag("XAppGuard")
                .logLevel(XSettings.isDevMode(this)
                        ? Logger.LogLevel.INFO : Logger.LogLevel.WARN)
                .build());
        XAppGuardManager.init();
        XActivation.reloadAsync(this);
    }

    public void hideAppIcon(boolean enable) {
        PackageManager pm = getPackageManager();
        ComponentName enabled = enable ? new ComponentName(this, GuardAppNavActivityNoLauncher.class) : new ComponentName(this, GuardAppNavActivity.class);
        ComponentName disabled = enable ? new ComponentName(this, GuardAppNavActivity.class) : new ComponentName(this, GuardAppNavActivityNoLauncher.class);
        Logger.d(enabled);
        Logger.d(disabled);
        pm.setComponentEnabledSetting(enabled, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(disabled, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    public static boolean isBuildByMan() {
        return false;
    }
}
