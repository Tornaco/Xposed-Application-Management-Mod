package github.tornaco.xposedmoduletest.x;

import android.os.IBinder;
import android.os.RemoteException;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAppService;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

class XModule implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static final String TAG = "XAppGuard-";

    static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    static {
        PREBUILT_WHITE_LIST.add("com.android.systemui");
        PREBUILT_WHITE_LIST.add("com.android.packageinstaller");
        PREBUILT_WHITE_LIST.add("android");
        PREBUILT_WHITE_LIST.add(BuildConfig.APPLICATION_ID);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }

    class AppServiceClient implements IBinder.DeathRecipient {
        boolean ok;
        IAppService service;

        AppServiceClient(IAppService service) {
            ok = service != null;
            if (!ok) return;
            this.service = service;
            try {
                this.service.asBinder().linkToDeath(this, 0);
            } catch (RemoteException ignored) {

            }
        }

        void unLinkToDeath() {
            if (ok && service != null) {
                service.asBinder().unlinkToDeath(this, 0);
            }
        }

        @Override
        public void binderDied() {
            XposedBridge.log(TAG + "AppServiceClient binder died!!!");
            ok = false;
            unLinkToDeath();
        }
    }
}
