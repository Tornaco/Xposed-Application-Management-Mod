package github.tornaco.xposedmoduletest.x.service;

import github.tornaco.apigen.CreateMessageIdWithMethods;

import static github.tornaco.xposedmoduletest.x.service.ServiceHandler.BASE;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */
@CreateMessageIdWithMethods(base = BASE, fallbackMessageDecode = "MSG_TRANSACTION_EXPIRE")
interface ServiceHandler {

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

    void userLeaving(String rea);
}
