package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;
import android.view.KeyEvent;

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

    void onKeyEvent(KeyEvent event);

    String serial();
}
