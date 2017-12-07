package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;
import android.content.Intent;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public interface IModuleBridge {

    void attachContext(Context context);

    void publish();

    void systemReady();

    /**
     * System is ready, we should retrieve settings now before other service start.
     */
    void retrieveSettings();

    void publishFeature(String f);

    void shutdown();

    void onPackageMoveToFront(Intent who);

    String serial();
}
