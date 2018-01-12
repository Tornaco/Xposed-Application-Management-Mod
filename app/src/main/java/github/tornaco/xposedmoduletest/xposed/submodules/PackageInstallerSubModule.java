package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.PackageDeleteObserver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.VersionedPackage;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */


// Oreo
// public void uninstall(VersionedPackage versionedPackage, String callerPackageName, int flags,
//              IntentSender statusReceiver, int userId) throws RemoteException {
class PackageInstallerSubModule extends AndroidSubModule {
    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookPackageInstaller(lpparam);
    }

    private void hookPackageInstaller(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookPackageInstaller...");
        try {
            Class clz = XposedHelpers.findClass("com.android.server.pm.PackageInstallerService",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "uninstall", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            try {

                                String pkgName;
                                if (OSUtil.isOOrAbove()) {
                                    VersionedPackage vp = (VersionedPackage) param.args[0];
                                    pkgName = vp.getPackageName();
                                } else {
                                    pkgName = (String) param.args[0];
                                }
                                XposedLog.verbose("PackageInstallerService uninstall pkg:" + pkgName);
                                boolean interrupt = interruptPackageRemoval(pkgName);
                                if (interrupt) {
                                    // FIXME Test fail by adb.
                                    final Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                                    String callingPkgName = (String) param.args[1];
                                    XposedLog.verbose("uninstall called by: " + callingPkgName);
                                    int userID = (int) param.args[4];
//                                    DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
//                                    boolean isDeviceOwner = dpm != null && dpm.isDeviceOwnerAppOnCallingUser(callingPkgName);
                                    IntentSender statusReceiver = (IntentSender) param.args[3];
                                    PackageDeleteObserverAdapter observerAdapter =
                                            new PackageDeleteObserverAdapter(context, statusReceiver, pkgName, userID);
                                    observerAdapter.onPackageDeleted(pkgName, PackageManager.DELETE_FAILED_ABORTED, null);

                                    param.setResult(null);
                                    XposedLog.verbose("PackageInstallerService interruptPackageRemoval");
                                    getBridge().notifyPackageRemovalInterrupt(pkgName);
                                }
                            } catch (Exception e) {
                                XposedLog.verbose("Fail uninstall:" + e);
                            }
                        }
                    });
            XposedLog.verbose("hookPackageInstaller OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookPackageInstaller:" + e);
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
                XposedLog.wtf("SendIntentException:" + Log.getStackTraceString(ignored));
            }
        }
    }
}
