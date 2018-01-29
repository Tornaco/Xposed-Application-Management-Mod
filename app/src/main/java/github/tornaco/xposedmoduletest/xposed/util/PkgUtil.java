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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.UserHandle;
import android.print.PrintManager;
import android.provider.Telephony;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class PkgUtil {

    public static boolean isApplicationStateDisabled(int state) {
        return state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED;
    }

    public static boolean isPkgInstalled(Context context, String pkg) {
        String path = pathOf(context, pkg);
        if (path == null) return false;
//        if (!new File(path).exists()) {
//            // FIXME Should we skip now?
//        }
        PackageManager pm = context.getPackageManager();

        try {
            ApplicationInfo info = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                info = pm.getApplicationInfo(pkg, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                info = pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES);
            }
            return info != null;
        } catch (PackageManager.NameNotFoundException var4) {
            return false;
        }
    }

    public static int loadVersionCodeByPkgName(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                info = pm.getApplicationInfo(pkg, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                info = pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES);
            }
            return info == null ? -1 : info.versionCode;
        } catch (PackageManager.NameNotFoundException var4) {
            return -1;
        }
    }

    public static String loadVersionNameByPkgName(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                info = pm.getPackageInfo(pkg, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                info = pm.getPackageInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES);
            }
            return info == null ? null : info.versionName;
        } catch (PackageManager.NameNotFoundException var4) {
            return null;
        }
    }

    public static CharSequence loadNameByPkgName(Context context, String pkg) {
        if (pkg == null) return "NULL";
        if ("android".equals(pkg)) {
            return context.getString(R.string.app_name_android);
        }
        // Here we check if this is dummy one.
        boolean isDummy = XAshmanManager.APPOPS_WORKAROUND_DUMMY_PACKAGE_NAME.equals(pkg);
        if (isDummy) {
            return context.getString(R.string.title_perm_control_template);
        }

        PackageManager pm = context.getPackageManager();

        try {
            ApplicationInfo info = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                info = pm.getApplicationInfo(pkg, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                info = pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES);
            }
            return info == null ? pkg : info.loadLabel(pm);
        } catch (PackageManager.NameNotFoundException var4) {
            return pkg;
        }
    }

    private static final SparseArray<String> sUidMap = new SparseArray<>();

    public static synchronized void cachePkgUid(String pkg, int uid) {
        sUidMap.put(uid, pkg);
    }

    // FIXME Add a cache.
    public static String pkgForUid(Context context, int uid) {
        // Check if in map.
        String cached = sUidMap.get(uid);
        if (cached != null) return cached;

        // This is fucking dangerous!!!!!!!!! to call get installed apps cross system.
        // This cause the overflow error!!!!!!!!!!!!!!!!!!
//
//        PackageManager pm = context.getPackageManager();
//        List<android.content.pm.PackageInfo> packages;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
//            packages = pm.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
//        } else {
//            packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
//        }
//        if (packages == null) return null;
//        for (android.content.pm.PackageInfo packageInfo : packages) {
//            if (packageInfo.applicationInfo == null) continue;
//            if (packageInfo.applicationInfo.uid == uid) {
//                String pkg = packageInfo.packageName;
//                if (pkg != null) {
//                    sUidMap.put(uid, pkg);
//                    return pkg;
//                }
//            }
//        }
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
        return isSystemOrPhoneOrShell(uid)
                || isSystemApp(context, pkgForUid(context, uid));
    }

    // Check if uid is system, shell or phone.
    public static boolean isSystemOrPhoneOrShell(int uid) {
        return uid <= 2000
                || (uid > UserHandle.PER_USER_RANGE && (uid % UserHandle.PER_USER_RANGE <= 2000));
    }

    public static boolean isSystemCall(int uid) {
        return uid == 1000
                || (uid > UserHandle.PER_USER_RANGE && (uid % UserHandle.PER_USER_RANGE == 1000));
    }

    public static boolean isSystemApp(Context context, String pkg) {
        if ("android".equals(pkg)) return true;
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                applicationInfo = pm.getApplicationInfo(pkg, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                applicationInfo = pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES);
            }
            return applicationInfo != null && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static String pathOf(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                applicationInfo = pm.getApplicationInfo(pkg, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                applicationInfo = pm.getApplicationInfo(pkg, PackageManager.GET_UNINSTALLED_PACKAGES);
            }
            return applicationInfo.publicSourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static boolean isHomeIntent(Intent intent) {
        return intent != null && intent.hasCategory(Intent.CATEGORY_HOME);
    }

    public static boolean isMainIntent(Intent intent) {
        return intent != null
                && Intent.ACTION_MAIN.equals(intent.getAction())
                && intent.hasCategory(Intent.CATEGORY_LAUNCHER);
    }

    public static String packageNameOf(Intent intent) {
        if (intent == null) return null;
        if (intent.getComponent() == null) return null;
        return intent.getComponent().getPackageName();
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

    public static Set<String> getRunningProcessPackages(Context context) {
        @SuppressWarnings("ConstantConditions") List<ActivityManager.RunningAppProcessInfo> processes =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningAppProcesses();
        HashSet<String> h = new HashSet<>();
        if (processes == null || processes.size() == 0) return h;
        for (ActivityManager.RunningAppProcessInfo info : processes) {
            if (info != null && info.pkgList != null && info.pkgList.length > 0) {
                Collections.addAll(h, info.pkgList);
            }
        }
        return h;
    }

    private static final Set<String> sRunningApps = new HashSet<>();

    public static int getRunningAppsCount(Context context) {
        List<ActivityManager.RunningAppProcessInfo> processes =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningAppProcesses();
        return processes == null ? 0 : processes.size();
    }

    public static void onAppLaunched(String who, String reason) {
        XposedLog.verbose("onAppLaunched: " + who + ", reason: " + reason);
        sRunningApps.add(who);
    }

    public static void onAppBringDown(String who, String why) {
        XposedLog.verbose("onAppBringDown: " + who + ", reason: " + why);
        sRunningApps.remove(who);
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isAppRunning(Context context, String pkg, boolean systemApp) {

        // Use our running apps cal only for 3-rd app.
        if (!systemApp) {
            boolean running = sRunningApps.contains(pkg);
            if (!running) return false;
            // Do not rely on the true res for 3-rd apps, also check by system.
        }


        // This is system app.
        List<ActivityManager.RunningAppProcessInfo> processes =
                ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningAppProcesses();
        int count = processes == null ? 0 : processes.size();
        for (int i = 0; i < count; i++) {
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
            kill(context, pkg);
        } catch (Exception ignored) {
        }
    }

    private static final Object sLock = new Object();
    private static Handler sPkgBringDownHandler;

    private static void initBringDownHandler() {
        HandlerThread hr = new HandlerThread("BringDownHandler");
        hr.start();
        sPkgBringDownHandler = new Handler(hr.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    String pkg = (String) msg.obj;
                    bringDownComplete(pkg);
                } catch (Exception e) {
                    XposedLog.wtf("BringDownHandler" + e.getLocalizedMessage());
                }
            }
        };
        XposedLog.verbose("initBringDownHandler: " + sPkgBringDownHandler);
    }

    private static final Set<String> BRING_DOWN_PACKAGES = new HashSet<>();

    public static boolean justBringDown(String pkg) {
        return BRING_DOWN_PACKAGES.contains(pkg);
    }

    private static void onBringDown(String who) {
        BRING_DOWN_PACKAGES.add(who);
        XposedLog.verbose("onBringDown: " + who);
        onAppBringDown(who, "onBringDown");
        synchronized (sLock) {
            if (sPkgBringDownHandler == null) {
                initBringDownHandler();
            }
        }
        sPkgBringDownHandler.sendMessageDelayed(sPkgBringDownHandler.obtainMessage(0, who), 3000);
    }

    private static void bringDownComplete(String who) {
        BRING_DOWN_PACKAGES.remove(who);
        XposedLog.verbose("bringDownComplete: " + who);
    }

    public static void kill(Context context, String pkg) {
        if (pkg == null) return;
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (am != null) {
                am.forceStopPackage(pkg);
                onBringDown(pkg);
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
                XposedLog.wtf("getPermissionControllerPackageName err: " + e);
            }
        }
        if (sServicesSystemSharedLibPackageName == null) {
            try {
                sServicesSystemSharedLibPackageName = pm.getServicesSystemSharedLibraryPackageName();
            } catch (Throwable e) {
                XposedLog.wtf("getServicesSystemSharedLibraryPackageName err: " + e);
            }
        }
        if (sSharedSystemSharedLibPackageName == null) {
            try {
                sSharedSystemSharedLibPackageName = pm.getSharedSystemSharedLibraryPackageName();
            } catch (Throwable e) {
                XposedLog.wtf("getSharedSystemSharedLibraryPackageName err: " + e);
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

    public static String[] getAllDeclaredPermissions(Context context, String packageName) {
        PackageInfo packageInfo = getPkgInfo(context, packageName);
        String[] permissions = new String[0];
        if (packageInfo != null) {
            permissions = packageInfo.requestedPermissions;
        }
        return permissions;
    }


    private static PackageInfo getPkgInfo(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            return packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static String[] extractUnGrantedPerms(Context context, String packageName, String[] declaredPerms) {
        if (declaredPerms == null || declaredPerms.length == 0) return null;
        PackageManager packageManager = context.getPackageManager();
        List<String> requestList = new ArrayList<>(declaredPerms.length);
        for (String info : declaredPerms) {
            int code = packageManager.checkPermission(info, packageName);
            if (code == PackageManager.PERMISSION_GRANTED) continue;
            requestList.add(info);
        }
        String[] out = new String[requestList.size()];
        for (int i = 0; i < requestList.size(); i++) {
            out[i] = requestList.get(i);
        }
        return out;
    }
}
