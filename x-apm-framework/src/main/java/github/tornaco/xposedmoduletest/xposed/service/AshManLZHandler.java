package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.view.KeyEvent;

import github.tornaco.apigen.CreateMessageIdWithMethods;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "UNKNOWN")
interface AshManLZHandler {
    void onActivityDestroy(Intent intent);

    void onPackageMoveToFront(String who);

    void onPackageMoveToFrontDelayUpdate(String who);

    void onCompSetting(String pkg, boolean enable);

    void onBroadcastAction(Intent action);

    void notifyTopPackageChanged(final String from, final String to);

    void onKeyEvent(KeyEvent keyEvent);

    void maybeBackLongPressed(String targetPackage);

    void maybeBackPressed(String targetPackage);

    void onStartProcessLocked(ApplicationInfo applicationInfo);

    void onRemoveProcessLocked(ApplicationInfo applicationInfo, boolean callerWillRestart, boolean allowRestart, String reason);
}
