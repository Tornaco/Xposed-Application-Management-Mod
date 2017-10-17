package github.tornaco.xposedmoduletest;

import android.app.Application;

import org.newstand.logger.Logger;
import org.newstand.logger.Settings;

import github.tornaco.xposedmoduletest.license.ADM;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public class XApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Logger.config(Settings.builder().tag("XAppGuard").build());
        ADM.reloadAsync(this);
    }
}
