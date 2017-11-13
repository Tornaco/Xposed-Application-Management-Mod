package github.tornaco.xposedmoduletest.xposed.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.Arrays;
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

    @SuppressWarnings("ConstantConditions")
    public static boolean isAppRunning(Context context, String pkg) {
        List<ActivityManager.RunningAppProcessInfo> processes =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningAppProcesses();
        int count = processes == null ? 0 : processes.size();
        for (int i = 0; i < count; i++) {
            XLog.logV("runningPackageName====================");
            XLog.logV("runningPackageName: " + processes.get(i).processName);
            XLog.logV("runningPackageName-pkgs: " + Arrays.toString(processes.get(i).pkgList));
            XLog.logV("runningPackageName====================");
            for (String runningPackageName : processes.get(i).pkgList) {
                if (runningPackageName != null && runningPackageName.equals(pkg)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isAppRunningForeground(Context context, String pkg) {
        List<ActivityManager.RunningAppProcessInfo> processes =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningAppProcesses();
        int count = processes == null ? 0 : processes.size();
        for (int i = 0; i < count; i++) {
            ActivityManager.RunningAppProcessInfo proc = processes.get(i);
            if (proc.importance
                    == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && arraysContains(proc.pkgList, pkg))
                return true;
        }
        return false;
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isAppRunningForeground(Context context, int uid) {
        List<ActivityManager.RunningAppProcessInfo> processes =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningAppProcesses();
        int count = processes == null ? 0 : processes.size();
        for (int i = 0; i < count; i++) {
            ActivityManager.RunningAppProcessInfo proc = processes.get(i);
            if (proc.uid == uid && proc.importance
                    == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND)
                return true;
        }
        return false;
    }

    private static boolean arraysContains(String[] arr, String target) {
        if (arr == null) return false;
        for (String s : arr) {
            if (s.equals(target)) return true;
        }
        return false;
    }
}
