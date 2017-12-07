package github.tornaco.xposedmoduletest.xposed.service;

import android.content.ComponentName;

import github.tornaco.apigen.CreateMessageIdWithMethods;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(fallbackMessageDecode = "MSG_TRANSACTION_EXPIRE")
interface AppGuardServiceHandler {

    int MSG_TRANSACTION_EXPIRE_BASE = 2017;

    void setEnabled(boolean enabled);

    void setUninstallInterruptEnabled(boolean enabled);

    void setVerifySettings(github.tornaco.xposedmoduletest.xposed.bean.VerifySettings settings);

    void setBlurSettings(github.tornaco.xposedmoduletest.xposed.bean.BlurSettings settings);

    void setResult(int transactionID, int res);

    void verify(VerifyArgs verifyArgs);

    void watch(github.tornaco.xposedmoduletest.IAppGuardWatcher w);

    void unWatch(github.tornaco.xposedmoduletest.IAppGuardWatcher w);

    void mockCrash();

    void setDebug(boolean debug);

    void onActivityPackageResume(String pkg);

    void onUserPresent();

    void setInterruptFPEventVBEnabled(int event, boolean enabled);

    void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add);
}
