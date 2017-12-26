package github.tornaco.xposedmoduletest.xposed;

import android.annotation.SuppressLint;
import android.app.Application;

import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import github.tornaco.apigen.BuildHostInfo;
import github.tornaco.apigen.BuildVar;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.provider.XSettings;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
@BuildHostInfo
@BuildVar
public class XApp extends Application {

    @SuppressLint("StaticFieldLeak")
    private static XApp xApp;

    public static XApp getApp() {
        return xApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        xApp = this;

        Logger.config(Settings.builder().tag("X-APM-C")
                .logLevel(XSettings.isDevMode(this)
                        ? Logger.LogLevel.DEBUG : Logger.LogLevel.WARN)
                .build());
    }
}
