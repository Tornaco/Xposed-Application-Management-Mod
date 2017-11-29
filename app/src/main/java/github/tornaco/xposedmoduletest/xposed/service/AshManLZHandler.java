package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Intent;

import github.tornaco.apigen.CreateMessageIdWithMethods;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "UNKNOWN")
interface AshManLZHandler {
    void onActivityDestroy(Intent intent);

    void onPackageMoveToFront(String who);
}
