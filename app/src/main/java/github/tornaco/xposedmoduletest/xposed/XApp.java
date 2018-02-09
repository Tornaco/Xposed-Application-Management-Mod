package github.tornaco.xposedmoduletest.xposed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;
import com.vanniktech.emoji.EmojiManager;

import org.newstand.logger.AndroidLogAdapter;
import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import github.tornaco.apigen.BuildHostInfo;
import github.tornaco.apigen.BuildVar;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.widget.SimpleEmojiProvider;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
@BuildHostInfo
@BuildVar
public class XApp extends MultiDexApplication {

    @SuppressLint("StaticFieldLeak")
    private static XApp xApp;

    public static XApp getApp() {
        return xApp;
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        xApp = this;

        initLogger();
    }

    private void initLogger() {

        // No need for play version, google has done this.
        if (!isPlayVersion()) {
            CrashReport.initCrashReport(getApplicationContext(), "db5e3b88a3", BuildConfig.DEBUG);
            CrashReport.setIsDevelopmentDevice(getApplicationContext(), BuildConfig.DEBUG);
            // Set cache size.
            BuglyLog.setCache(12 * 1024);
        }

        Logger.config(Settings.builder().tag("X-APM-C")
                .logLevel(BuildConfig.DEBUG || XSettings.isDevMode(this)
                        ? Logger.LogLevel.VERBOSE : Logger.LogLevel.WARN)
                .logAdapter(new AndroidLogAdapter() {
                    @Override
                    public void e(String tag, String message) {
                        super.e(tag, message);
                        if (!isPlayVersion()) {
                            // Report to bugly.
                            CrashReport.postCatchedException(new Throwable(message));
                        }
                    }
                })
                .build());

        EmojiManager.install(new SimpleEmojiProvider());
    }

    public static boolean isPlayVersion() {
        return XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.PLAY);
    }
}
