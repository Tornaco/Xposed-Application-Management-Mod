package github.tornaco.xposedmoduletest.x;

import android.annotation.SuppressLint;
import android.app.Application;

import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import github.tornaco.xposedmoduletest.license.ADM;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

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
        Logger.config(Settings.builder().tag("XAppGuard")
                .logLevel(Logger.LogLevel.ALL)
                .build());
        ADM.reloadAsync(this);
    }
}
