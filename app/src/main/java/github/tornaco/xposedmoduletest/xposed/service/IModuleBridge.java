package github.tornaco.xposedmoduletest.xposed.service;

import android.app.IApplicationThread;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public interface IModuleBridge {

    void attachContext(Context context);

    void publish();

    IBinder onRetrieveBinderService(String name);

    IBinder asBinder();

    void systemReady();

    /**
     * System is ready, we should retrieve settings now before other service start.
     */
    void retrieveSettings();

    void publishFeature(String f);

    void shutdown();

    void onPackageMoveToFront(Intent who);

    String serial();

    boolean onKeyEvent(KeyEvent keyEvent, String source);

    boolean checkBroadcastIntent(IApplicationThread caller,
                                 Intent intent
//            , String resolvedType, IIntentReceiver resultTo,
//                                 int resultCode, String resultData, Bundle map,
//                                 String requiredPermission, int appOp, boolean serialized, boolean sticky, int userId
    );
}
