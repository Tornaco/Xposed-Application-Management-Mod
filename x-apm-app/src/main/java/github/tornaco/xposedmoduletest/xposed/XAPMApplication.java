package github.tornaco.xposedmoduletest.xposed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.widget.Toast;

import com.vanniktech.emoji.EmojiManager;

import org.newstand.logger.AndroidLogAdapter;
import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import github.tornaco.android.common.BlackHole;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.cache.InstalledAppsLoadingCache;
import github.tornaco.xposedmoduletest.cache.RunningServicesLoadingCache;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.provider.XSettings;
import github.tornaco.xposedmoduletest.ui.activity.NavigatorActivityBottomNav;
import github.tornaco.xposedmoduletest.ui.widget.SimpleEmojiProvider;
import github.tornaco.xposedmoduletest.util.ActvityLifeCycleAdapter;
import github.tornaco.xposedmoduletest.util.GMSUtil;
import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */
public class XAPMApplication extends MultiDexApplication {

    public static final int EVENT_RUNNING_SERVICE_CACHE_UPDATE = 0x1;
    public static final int EVENT_INSTALLED_APPS_CACHE_UPDATE = 0x2;
    public static final int EVENT_GCM_REGISTRATION_COMPLETE = 0x3;
    public static final int EVENT_RECENT_TILE_CHANGED = 0x4;
    public static final int EVENT_APP_DEBUG_MODE_CHANGED = 0x5;

    @SuppressLint("StaticFieldLeak")
    private static XAPMApplication xApp;

    public static XAPMApplication getApp() {
        return xApp;
    }

    private static boolean sGMSSupported = false;

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        xApp = this;
        // Give user a chance to force use GMS features.
        setGMSSupported(AppSettings.isForceHasGMS(this) || GMSUtil.checkPlayServices(this));
        initLogger();
        EmojiManager.install(new SimpleEmojiProvider());
        cacheRunningServices();
        cacheInstalledApps();
        registerLifeCycleCallback();

        // For dev.
        if (BuildConfig.DEBUG) {
            forDev();
        }

        Logger.w("reportActivityLaunching");
    }

    public static boolean isGMSSupported() {
        return sGMSSupported;
    }

    public static void setGMSSupported(boolean supported) {
        sGMSSupported = supported;
    }

    private void registerLifeCycleCallback() {
        registerActivityLifecycleCallbacks(
                new ActvityLifeCycleAdapter() {
                    @Override
                    public void onActivityResumed(Activity activity) {
                        super.onActivityResumed(activity);

                        // This is the run activity.
                        if (activity instanceof NavigatorActivityBottomNav) {
                            cacheRunningServices();
                            cacheInstalledApps();
                            AppSettings.cacheRecentTilesAsync(getApplicationContext());

                            // Only test on debug build.
                            if (BuildConfig.DEBUG && sGMSSupported) {
                                // Start IntentService to register this application with GCM.
//                                Intent intent = new Intent(activity.getApplicationContext(), XRegistrationIntentService.class);
//                                startService(intent);
                            }

                        }
                    }
                });
    }

    private void cacheInstalledApps() {
        XExecutor.execute(() -> BlackHole.eat(InstalledAppsLoadingCache.getInstance().getInstalledAppsCache()));
    }

    private void cacheRunningServices() {
        XExecutor.execute(() -> BlackHole.eat(RunningServicesLoadingCache.getInstance().getRunningServiceCache()));
    }

    private void initLogger() {

        // No need for play version, google has done this.
        if (!isPlayVersion()) {
//            CrashReport.initCrashReport(getApplicationContext(), "db5e3b88a3", BuildConfig.DEBUG);
//            CrashReport.setIsDevelopmentDevice(getApplicationContext(), BuildConfig.DEBUG);
//            // Set cache size.
//            BuglyLog.setCache(12 * 1024);
        }

        Logger.config(Settings.builder().tag("X-APM-C")
                .logLevel(XSettings.isDevMode(this)
                        ? Logger.LogLevel.VERBOSE : Logger.LogLevel.WARN)
                .logAdapter(new AndroidLogAdapter() {
                    @Override
                    public void e(String tag, String message) {
                        super.e(tag, message);
                        if (!isPlayVersion()) {
                            // Report to bugly.
                            // CrashReport.postCatchedException(new Throwable(message));
                        }
                    }
                })
                .build());

    }

    public static boolean isPlayVersion() {
        return XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.PLAY);
    }

    private void forDev() {
        Toast.makeText(this, "Hello developer!", Toast.LENGTH_LONG).show();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                .edit().putString("DEBUG_APP_START", String.valueOf(System.currentTimeMillis()))
                .apply();
    }
}
