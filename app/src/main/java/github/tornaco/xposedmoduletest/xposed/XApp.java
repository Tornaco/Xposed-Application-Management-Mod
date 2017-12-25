package github.tornaco.xposedmoduletest.xposed;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import java.util.HashSet;
import java.util.Set;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.apigen.BuildHostInfo;
import github.tornaco.apigen.BuildVar;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
@BuildHostInfo
@BuildVar
public class XApp extends Application implements Runnable {

    private static final Set<String> WHITE_LIST = new HashSet<>();

    private void inflateWhiteList() {
        String[] prebuilt = getResources().getStringArray(R.array.default_ash_white_list_packages);
        Collections.consumeRemaining(prebuilt, new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (!WHITE_LIST.contains(s)) WHITE_LIST.add(s);
            }
        });
    }

    public static boolean isInGlobalWhiteList(String pkg) {
        return WHITE_LIST.contains(pkg);
    }

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

        Logger.config(Settings.builder().tag("X-APM-C")
                .logLevel(XSettings.isDevMode(this)
                        ? Logger.LogLevel.DEBUG : Logger.LogLevel.WARN)
                .build());
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                inflateWhiteList();
            }
        });
    }

    @Override
    public void run() {
        // empty.
    }
}
