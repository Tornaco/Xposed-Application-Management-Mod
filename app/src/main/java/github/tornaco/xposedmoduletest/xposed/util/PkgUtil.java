package github.tornaco.xposedmoduletest.xposed.util;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build;
import android.os.UserHandle;
import android.print.PrintManager;
import android.provider.Telephony;

import java.util.List;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class PkgUtil {

    public static boolean isPkgInstalled(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();

        try {
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException var4) {
            return false;
        }
    }

    public static int loadVersionByPkgName(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return info == null ? -1 : info.versionCode;
        } catch (PackageManager.NameNotFoundException var4) {
            return -1;
        }
    }

    public static CharSequence loadNameByPkgName(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();

        try {
            ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
            return info == null ? null : info.loadLabel(pm);
        } catch (PackageManager.NameNotFoundException var4) {
            return null;
        }
    }

    public static String pkgForUid(Context context, int uid) {
        PackageManager pm = context.getPackageManager();
        List<android.content.pm.PackageInfo> packages;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            packages = pm.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
        } else {
            packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        }
        if (packages == null) return null;
        for (android.content.pm.PackageInfo packageInfo : packages) {
            if (packageInfo.applicationInfo.uid == uid) {
                return packageInfo.packageName;
            }
        }
        return null;
    }

    public static int uidForPkg(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return pm.getApplicationInfo(pkg, PackageManager.MATCH_UNINSTALLED_PACKAGES).uid;
            } else {
                return pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES).uid;
            }
        } catch (Exception ignored) {

        }
        return -1;
    }

    public static boolean isSystemApp(Context context, int uid) {
        return uid <= 1000
                || isSystemApp(context, pkgForUid(context, uid));
    }

    public static boolean isSystemApp(Context context, String pkg) {
        if ("android".equals(pkg)) return true;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(pkg, 0);
            return applicationInfo != null && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isLauncherApp(Context context, String packageName) {
        PackageManager pkgManager = context.getPackageManager();
        Intent mainIntent = new Intent("android.intent.action.MAIN", null);
        mainIntent.addCategory("android.intent.category.LAUNCHER");
        mainIntent.setPackage(packageName);
        ResolveInfo ri = pkgManager.resolveActivity(mainIntent, 0);
        return !(ri == null || ri.activityInfo == null);
    }

    public static boolean isHomeApp(Context context, String packageName) {
        PackageManager pkgManager = context.getPackageManager();
        Intent homeIntent = new Intent("android.intent.action.MAIN");
        homeIntent.addCategory("android.intent.category.HOME");
        homeIntent.setPackage(packageName);
        ResolveInfo ri = pkgManager.resolveActivity(homeIntent, 0);
        return !(ri == null || ri.activityInfo == null);
    }

    public static boolean isDefaultSmsApp(Context context, String packageName) {
        String def = Telephony.Sms.getDefaultSmsPackage(context);
        return def != null && def.equals(packageName);
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isAppRunning(Context context, String pkg) {
        List<ActivityManager.RunningAppProcessInfo> processes =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningAppProcesses();
        int count = processes == null ? 0 : processes.size();
        for (int i = 0; i < count; i++) {
//            XPosedLog.verbose("runningPackageName====================");
//            XPosedLog.verbose("runningPackageName: " + processes.get(i).processName);
//            XPosedLog.verbose("runningPackageName-pkgs: " + Arrays.toString(processes.get(i).pkgList));
//            XPosedLog.verbose("runningPackageName====================");
            for (String runningPackageName : processes.get(i).pkgList) {
                if (runningPackageName != null && runningPackageName.equals(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String getFirstTask(Context context) {

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) return null;
        List<ActivityManager.RecentTaskInfo> recentTasks =
                am.getRecentTasksForUser(1, ActivityManager.RECENT_IGNORE_UNAVAILABLE,
                        UserHandle.getUserId(Binder.getCallingUid()));
        if (recentTasks.size() > 0) {
            ActivityManager.RecentTaskInfo recentTaskInfo = recentTasks.get(0);
            if (recentTaskInfo != null) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    ComponentName base = recentTaskInfo.baseActivity;
                    return base == null ? null : base.getPackageName();
                } else {
                    ComponentName real = recentTaskInfo.realActivity;
                    return real == null ? null : real.getPackageName();
                }
            }
        }
        return null;
    }

    public static boolean isAppRunningForeground(Context context, String pkg) {
        return pkg != null && pkg.equals(getFirstTask(context));
    }

    public static boolean isAppRunningForeground(Context context, int uid) {
        return false;
    }

    private static boolean arraysContains(String[] arr, String target) {
        if (arr == null) return false;
        for (String s : arr) {
            if (s.equals(target)) return true;
        }
        return false;
    }

    public static void kill(Context context, ActivityManager.RunningAppProcessInfo runningAppProcessInfo) {
        // Process.sendSignalQuiet(runningAppProcessInfo.pid, Process.SIGNAL_KILL);
        if (runningAppProcessInfo.pkgList == null || runningAppProcessInfo.pkgList.length < 1) {
            return;
        }
        try {
            String pkg = runningAppProcessInfo.pkgList[0];
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.forceStopPackage(pkg);
            }
        } catch (Exception ignored) {
        }
    }


    /**
     * Determine whether a package is a "system package", in which case certain things (like
     * disabling notifications or disabling the package altogether) should be disallowed.
     */

    private static Signature[] sSystemSignature;
    private static String sPermissionControllerPackageName;
    private static String sServicesSystemSharedLibPackageName;
    private static String sSharedSystemSharedLibPackageName;

    public static boolean isSystemPackage(Resources resources, PackageManager pm, PackageInfo pkg) {
        if (sSystemSignature == null) {
            sSystemSignature = new Signature[]{getSystemSignature(pm)};
        }
        if (sPermissionControllerPackageName == null) {
            try {
                sPermissionControllerPackageName = pm.getPermissionControllerPackageName();
            } catch (Throwable e) {
                XPosedLog.wtf("getPermissionControllerPackageName err: " + e);
            }
        }
        if (sServicesSystemSharedLibPackageName == null) {
            try {
                sServicesSystemSharedLibPackageName = pm.getServicesSystemSharedLibraryPackageName();
            } catch (Throwable e) {
                XPosedLog.wtf("getServicesSystemSharedLibraryPackageName err: " + e);
            }
        }
        if (sSharedSystemSharedLibPackageName == null) {
            try {
                sSharedSystemSharedLibPackageName = pm.getSharedSystemSharedLibraryPackageName();
            } catch (Throwable e) {
                XPosedLog.wtf("getSharedSystemSharedLibraryPackageName err: " + e);
            }
        }
        return (sSystemSignature[0] != null
                && sSystemSignature[0].equals(getFirstSignature(pkg)))
                || pkg.packageName.equals(sPermissionControllerPackageName)
                || pkg.packageName.equals(sServicesSystemSharedLibPackageName)
                || pkg.packageName.equals(sSharedSystemSharedLibPackageName)
                || pkg.packageName.equals(PrintManager.PRINT_SPOOLER_PACKAGE_NAME)
                || isDeviceProvisioningPackage(resources, pkg.packageName);
    }

    private static Signature getFirstSignature(PackageInfo pkg) {
        if (pkg != null && pkg.signatures != null && pkg.signatures.length > 0) {
            return pkg.signatures[0];
        }
        return null;
    }

    private static Signature getSystemSignature(PackageManager pm) {
        try {
            @SuppressLint("PackageManagerGetSignatures") final PackageInfo sys
                    = pm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            return getFirstSignature(sys);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return null;
    }

    /**
     * Returns {@code true} if the supplied package is the device provisioning app. Otherwise,
     * returns {@code false}.
     */
    public static boolean isDeviceProvisioningPackage(Resources resources, String packageName) {
        try {
            String deviceProvisioningPackage = resources.getString(
                    com.android.internal.R.string.config_deviceProvisioningPackage);
            return deviceProvisioningPackage.equals(packageName);
        } catch (Throwable e) {
            return false;
        }
    }
}
