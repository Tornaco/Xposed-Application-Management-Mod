package github.tornaco.xposedmoduletest.service;

import github.tornaco.apigen.CreateMessageIdWithMethods;

/**
 * Created by guohao4 on 2017/11/22.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "UNKNOWN")
public interface WatchDog {
    // Stub methods, will not be used for impl.
    void onStartBlocked(String pkg);
}
