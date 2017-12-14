package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.util.Log;

import java.util.Collections;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class PMSSubModule4 extends IntentFirewallAndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookGetInstalledPkgs(lpparam);
        hookGetInstalledApps(lpparam);
    }

    private void hookGetInstalledPkgs(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookGetInstalledPkgs...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "getInstalledPackages", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            int uid = Binder.getCallingUid();
                            if (PkgUtil.isSystemOrPhoneOrShell(uid)) return;
                            // Check op.
                            XAshmanManager xAshmanManager = XAshmanManager.get();
                            if (xAshmanManager.isServiceAvailable()) {
                                int mode = xAshmanManager.getPermissionControlBlockModeForUid(
                                        AppOpsManagerCompat.OP_READ_INSTALLED_APPS, uid);
                                if (mode == AppOpsManagerCompat.MODE_IGNORED) {
                                    XposedLog.verbose("getInstalledPackages, MODE_IGNORED returning empty for :" + uid);
                                    try {
                                        ParceledListSlice<PackageInfo> empty = new ParceledListSlice<>(Collections.<PackageInfo>emptyList());
                                        param.setResult(empty);
                                    } catch (Exception e) {
                                        param.setResult(null);
                                        XposedLog.wtf("Fail get empty ParceledListSlice:" + e);
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookGetInstalledPkgs OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookGetInstalledPkgs:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookGetInstalledApps(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookGetInstalledApps...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageManagerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "getInstalledApplications", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            int uid = Binder.getCallingUid();
                            // Check op.
                            XAshmanManager xAshmanManager = XAshmanManager.get();
                            if (xAshmanManager.isServiceAvailable()) {
                                int mode = xAshmanManager.getPermissionControlBlockModeForUid(
                                        AppOpsManagerCompat.OP_READ_INSTALLED_APPS, uid);
                                if (mode == AppOpsManagerCompat.MODE_IGNORED) {
                                    XposedLog.verbose("getInstalledApplications, MODE_IGNORED returning empty for :" + uid);
                                    try {
                                        // M has no method named empty.
                                        ParceledListSlice<ApplicationInfo> empty = new ParceledListSlice<>(Collections.<ApplicationInfo>emptyList());
                                        param.setResult(empty);
                                    } catch (Exception e) {
                                        param.setResult(null);
                                        XposedLog.wtf("Fail get empty ParceledListSlice:" + e);
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookGetInstalledApps OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookGetInstalledApps:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
