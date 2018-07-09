package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ParceledListSlice;
import android.os.Binder;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class PMSGetInstalledPackagesSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

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

                            boolean permControlEnabled = XAPMManager.get().isServiceAvailable()
                                    && XAPMManager.get().isPermissionControlEnabled();
                            if (!permControlEnabled) {
                                return;
                            }

                            int uid = Binder.getCallingUid();

                            if (PkgUtil.isSystemOrPhoneOrShell(uid)) return;

                            // Check op.
                            XAPMManager xAshmanManager = XAPMManager.get();
                            if (xAshmanManager.isServiceAvailable()) {
                                int mode = xAshmanManager.getPermissionControlBlockModeForUid(
                                        XAppOpsManager.OP_READ_INSTALLED_APPS, uid, true);
                                if (mode == XAppOpsManager.MODE_IGNORED) {
                                    try {
                                        List<PackageInfo> selfOnlyList = new ArrayList<>();
                                        String callingPkgName = xAshmanManager.getPackageNameForUid(uid);
                                        XposedLog.verbose("getInstalledPackages, MODE_IGNORED returning empty for :" + uid + "-" + callingPkgName);
                                        if (callingPkgName != null) {
                                            PackageInfo packageInfo = xAshmanManager.getPackageInfoForPackage(callingPkgName);
                                            if (packageInfo != null) {
                                                selfOnlyList.add(packageInfo);
                                                XposedLog.verbose("getInstalledPackages, MODE_IGNORED inflating pkg info :" + packageInfo);
                                            }
                                        }
                                        ParceledListSlice<PackageInfo> empty = new ParceledListSlice<>(selfOnlyList);
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

                            boolean permControlEnabled = XAPMManager.get().isServiceAvailable()
                                    && XAPMManager.get().isPermissionControlEnabled();
                            if (!permControlEnabled) {
                                return;
                            }

                            int uid = Binder.getCallingUid();

                            if (PkgUtil.isSystemOrPhoneOrShell(uid)) return;

                            // Check op.
                            XAPMManager xAshmanManager = XAPMManager.get();
                            if (xAshmanManager.isServiceAvailable()) {
                                int mode = xAshmanManager.getPermissionControlBlockModeForUid(
                                        XAppOpsManager.OP_READ_INSTALLED_APPS, uid,
                                        true);
                                if (mode == XAppOpsManager.MODE_IGNORED) {
                                    try {
                                        // M has no method named empty.
                                        List<ApplicationInfo> selfOnlyList = new ArrayList<>();
                                        String callingPkgName = xAshmanManager.getPackageNameForUid(uid);
                                        Log.d(XposedLog.TAG, "getInstalledApplications, MODE_IGNORED returning empty for :" + uid + "-" + callingPkgName);
                                        if (callingPkgName != null) {
                                            ApplicationInfo applicationInfo = xAshmanManager.getApplicationInfoForPackage(callingPkgName);
                                            if (applicationInfo != null) {
                                                selfOnlyList.add(applicationInfo);
                                                Log.d(XposedLog.TAG, "getInstalledApplications, MODE_IGNORED inflating app info :" + applicationInfo);
                                            }
                                        }
                                        ParceledListSlice<ApplicationInfo> empty = new ParceledListSlice<>(selfOnlyList);
                                        param.setResult(empty);
                                    } catch (Exception e) {
                                        param.setResult(null);
                                        Log.d(XposedLog.TAG, "Fail get empty ParceledListSlice:" + e);
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
