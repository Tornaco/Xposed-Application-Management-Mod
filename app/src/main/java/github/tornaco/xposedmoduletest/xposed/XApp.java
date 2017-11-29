package github.tornaco.xposedmoduletest.xposed;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.android.internal.os.BinderInternal;

import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import github.tornaco.apigen.BuildHostInfo;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.provider.XSettings;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
@BuildHostInfo
public class XApp extends Application implements Runnable {

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
        Logger.config(Settings.builder().tag("X-APM-")
                .logLevel(XSettings.isDevMode(this)
                        ? Logger.LogLevel.VERBOSE : Logger.LogLevel.WARN)
                .build());
        BinderInternal.addGcWatcher(this);
    }

    @Override
    public void run() {
    }
}
