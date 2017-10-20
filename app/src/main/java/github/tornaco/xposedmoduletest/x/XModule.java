package github.tornaco.xposedmoduletest.x;

import android.os.IBinder;
import android.os.RemoteException;

import org.newstand.logger.Logger;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAppService;
import github.tornaco.xposedmoduletest.IXModuleToken;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

class XModule extends IXModuleToken.Stub implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static final String TAG = "XAppGuard-";

    static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    static {
        PREBUILT_WHITE_LIST.add("com.android.systemui");
        PREBUILT_WHITE_LIST.add("com.android.packageinstaller");
        PREBUILT_WHITE_LIST.add("android");
        PREBUILT_WHITE_LIST.add(BuildConfig.APPLICATION_ID);
    }

    XStatus xStatus = XStatus.UNKNOWN;

    XSharedPreferences xSharedPreferences;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }


    void initDefaultXPreference() {
        xSharedPreferences = new XSharedPreferences(BuildConfig.APPLICATION_ID);
        xSharedPreferences.makeWorldReadable();
        XposedBridge.log(TAG + "xSharedPreferences:" + xSharedPreferences);
        boolean enabled = xSharedPreferences.getBoolean(XKey.ENABLED, false);
        XposedBridge.log(TAG + "enabled:" + enabled);
    }

    @Override
    public void dump() throws RemoteException {
        try {
            Logger.i("DUMP STARTED");
            Logger.i("PREBUILT_WHITE_LIST:");
            Collections.consumeRemaining(PREBUILT_WHITE_LIST, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    Logger.i(s);
                }
            });
            Logger.i("DUMP END");
        } catch (Exception ignored) {
        }
    }

    @Override
    public int status() throws RemoteException {
        return xStatus.ordinal();
    }

    @Override
    public String codename() throws RemoteException {
        return null;
    }

    class AppServiceClient implements IBinder.DeathRecipient {
        boolean ok;
        IAppService service;

        AppServiceClient(IAppService service) {
            ok = service != null;
            if (!ok) return;
            this.service = service;
            try {
                this.service.registerXModuleToken(XModule.this);
                this.service.asBinder().linkToDeath(this, 0);
            } catch (Exception ignored) {

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
