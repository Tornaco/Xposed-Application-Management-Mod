package github.tornaco.xposedmoduletest.xposed.service;

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

    void setBlurEnabled(boolean enabled);

    void setBlurRadius(int r);

    void setVerifySettings(github.tornaco.xposedmoduletest.xposed.bean.VerifySettings settings);

    void setResult(int transactionID, int res);

    void verify(VerifyArgs verifyArgs);

    void mockCrash();

    void setDebug(boolean debug);

    void onActivityPackageResume(String pkg);

    void onUserPresent();

    void setInterruptFPEventVBEnabled(int event, boolean enabled);

    void restoreDefaultSettings();

    void warnIfDebug();

    void onAppTaskRemoved(String pkg);
}
