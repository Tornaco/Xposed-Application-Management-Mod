package github.tornaco.xposedmoduletest.x.service;

import android.app.Activity;
import android.view.KeyEvent;

import github.tornaco.apigen.CreateMessageIdWithMethods;

import static github.tornaco.xposedmoduletest.x.service.AppGuardServiceHandler.BASE;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(base = BASE, fallbackMessageDecode = "MSG_TRANSACTION_EXPIRE")
interface AppGuardServiceHandler {

    int BASE = 0x1;
    int MSG_TRANSACTION_EXPIRE_BASE = 2017;

    void setEnabled(boolean enabled);

    void setUninstallInterruptEnabled(boolean enabled);

    void setVerifySettings(github.tornaco.xposedmoduletest.x.bean.VerifySettings settings);

    void setBlurSettings(github.tornaco.xposedmoduletest.x.bean.BlurSettings settings);

    void setResult(int transactionID, int res);

    void verify(VerifyArgs verifyArgs);

    void watch(github.tornaco.xposedmoduletest.IWatcher w);

    void unWatch(github.tornaco.xposedmoduletest.IWatcher w);

    void mockCrash();

    void onKeyEvent(KeyEvent keyEvent);

    void injectHomeEvent();

    void setDebug(boolean debug);

    void onActivityResume(Activity activity);

    void onActivityPackageResume(String pkg);
}
