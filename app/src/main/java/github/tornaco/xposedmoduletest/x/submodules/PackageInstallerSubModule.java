package github.tornaco.xposedmoduletest.x.submodules;

import android.app.PackageDeleteObserver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.x.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.x.util.XLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class PackageInstallerSubModule extends AndroidSubModuleModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookPackageInstaller(lpparam);
    }

    private void hookPackageInstaller(XC_LoadPackage.LoadPackageParam lpparam) {
        XLog.logV("hookPackageInstaller...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageInstallerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "uninstall", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            try {
                                String pkgName = (String) param.args[0];
                                XLog.logV("PackageInstallerService uninstall pkg:" + pkgName);
                                boolean interrupt = interruptPackageRemoval(pkgName);
                                if (interrupt) {

                                    // FIXME Test fail by adb.
                                    final Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                                    String callingPkgName = (String) param.args[1];
                                    int userID = (int) param.args[4];
//                                    DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//                                    boolean isDeviceOwner = dpm != null && dpm.isDeviceOwnerAppOnCallingUser(callingPkgName);
                                    IntentSender statusReceiver = (IntentSender) param.args[3];
                                    PackageDeleteObserverAdapter observerAdapter =
                                            new PackageDeleteObserverAdapter(context, statusReceiver, pkgName, userID);
                                    observerAdapter.onPackageDeleted(pkgName, PackageManager.DELETE_FAILED_ABORTED, null);

                                    param.setResult(null);
                                    XLog.logV("PackageInstallerService interruptPackageRemoval");
                                }
                            } catch (Exception e) {
                                XLog.logV("Fail uninstall:" + e);
                            }
                        }
                    });
            XLog.logV("hookPackageInstaller OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
            getBridge().publishFeature(XAppGuardManager.Feature.HOME);
        } catch (Exception e) {
            XLog.logV("Fail hookPackageInstaller:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }


    private boolean interruptPackageRemoval(String pkgName) {
        return getBridge().interruptPackageRemoval(pkgName);
    }

    static class PackageDeleteObserverAdapter extends PackageDeleteObserver {

        private final Context mContext;
        private final IntentSender mTarget;
        private final String mPackageName;

        PackageDeleteObserverAdapter(Context context, IntentSender target,
                                     String packageName, int userId) {
            mContext = context;
            mTarget = target;
            mPackageName = packageName;
        }

        @Override
        public void onUserActionRequired(Intent intent) {
            final Intent fillIn = new Intent();
            fillIn.putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, mPackageName);
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS,
                    PackageInstaller.STATUS_PENDING_USER_ACTION);
            fillIn.putExtra(Intent.EXTRA_INTENT, intent);
            try {
                mTarget.sendIntent(mContext, 0, fillIn, null, null);
            } catch (IntentSender.SendIntentException ignored) {
            }
        }

        @Override
        public void onPackageDeleted(String basePackageName, int returnCode, String msg) {
            final Intent fillIn = new Intent();
            fillIn.putExtra(PackageInstaller.EXTRA_PACKAGE_NAME, mPackageName);
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS,
                    PackageManager.deleteStatusToPublicStatus(returnCode));
            fillIn.putExtra(PackageInstaller.EXTRA_STATUS_MESSAGE,
                    PackageManager.deleteStatusToString(returnCode, msg));
            fillIn.putExtra(PackageInstaller.EXTRA_LEGACY_STATUS, returnCode);
            try {
                mTarget.sendIntent(mContext, 0, fillIn, null, null);
            } catch (IntentSender.SendIntentException ignored) {
                XLog.logF("SendIntentException:" + Log.getStackTraceString(ignored));
            }
        }
    }
}
