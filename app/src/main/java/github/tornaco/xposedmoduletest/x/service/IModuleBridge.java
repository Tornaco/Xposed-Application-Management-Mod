package github.tornaco.xposedmoduletest.x.service;

import android.content.Context;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public interface IModuleBridge {

    void attachContext(Context context);

    void publish();

    void systemReady();

    void publishFeature(String f);

    void shutdown();

    String serial();
}
