package github.tornaco.xposedmoduletest.x;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.newstand.logger.Logger;

import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAppService;
import github.tornaco.xposedmoduletest.IXModuleToken;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
class XModule extends IXModuleToken.Stub implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    static final boolean DEBUG_V = true;

    static final String TAG = "XAppGuard-";

    static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    static {
        PREBUILT_WHITE_LIST.add("com.android.systemui");
        PREBUILT_WHITE_LIST.add("com.android.packageinstaller");
        PREBUILT_WHITE_LIST.add("android");
        PREBUILT_WHITE_LIST.add("com.cyanogenmod.trebuchet");
        // It is good for user if our mod crash.
        PREBUILT_WHITE_LIST.add("de.robv.android.xposed.installer");
        PREBUILT_WHITE_LIST.add(BuildConfig.APPLICATION_ID);
    }

    XStatus xStatus = XStatus.UNKNOWN;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if ("android".equals(lpparam.packageName)) {
            hookFinishBooting(lpparam);
            //hookPerms(lpparam);
            hookFPService(lpparam);
        }
    }

    // http://androidxref.com/7.0.0_r1/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    // http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/fingerprint/FingerprintService.java
    private void hookFPService(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllMethods(
                    XposedHelpers.findClass("com.android.server.fingerprint.FingerprintService", lpparam.classLoader),
                    "canUseFingerprint", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Object pkg = param.args[0];
                            if (BuildConfig.APPLICATION_ID.equals(pkg)) {
                                param.setResult(true);
                                XposedBridge.log(TAG + "ALLOWING APPGUARD TO USE FP ANYWAY");
                            }
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log(TAG + "Fail hookFPService" + e);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }

    private void hookPerms(XC_LoadPackage.LoadPackageParam lpparam) {
        Class ams = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", lpparam.classLoader);
        XposedBridge.hookAllMethods(ams, "checkPermission", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                int uid = (int) param.args[2];
                if (uid <= 1000) return;
                if (Manifest.permission.USE_FINGERPRINT.equals(param.args[0])) {
                    XposedBridge.log(TAG + "Hooked USE_FINGERPRINT permissions");
                    param.setResult(PackageManager.PERMISSION_GRANTED);
                }
            }
        });

    }

    private void hookFinishBooting(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllMethods(Class.forName("com.android.server.am.ActivityManagerService",
                    false,
                    lpparam.classLoader)
                    , "finishBooting", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            onBootComplete();
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log(TAG + "hookFinishBooting" + Log.getStackTraceString(e));
        }
    }

    void onBootComplete() {
        XposedBridge.log(TAG + "onBootComplete");
    }

    boolean isLauncherIntent(Intent intent) {
        return intent != null
                && intent.getCategories() != null
                && intent.getCategories().contains("android.intent.category.HOME");
    }

    @Override
    public void dump() throws RemoteException {
        try {
            Logger.i("DUMP STARTED");
            Logger.i("PREBUILT_WHITE_LIST:");
            Collections.consumeRemaining(PREBUILT_WHITE_LIST,
                    new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            Logger.i(s);
                        }
                    });
            Logger.i("DUMP END");
        } catch (Exception ignored) {
        }
    }

    protected boolean isAppServiceInstalled() {
        return true;
    }

    @Override
    public int status() throws RemoteException {
        return xStatus.ordinal();
    }

    @Override
    public String codename() throws RemoteException {
        return getClass().getName();
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
