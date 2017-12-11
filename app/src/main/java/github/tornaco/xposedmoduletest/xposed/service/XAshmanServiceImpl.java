package github.tornaco.xposedmoduletest.xposed.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkPolicyManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.support.annotation.GuardedBy;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.android.internal.os.Zygote;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.android.common.Holder;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.bean.AutoStartPackage;
import github.tornaco.xposedmoduletest.bean.AutoStartPackageDaoUtil;
import github.tornaco.xposedmoduletest.bean.BootCompletePackage;
import github.tornaco.xposedmoduletest.bean.BootCompletePackageDaoUtil;
import github.tornaco.xposedmoduletest.bean.LockKillPackage;
import github.tornaco.xposedmoduletest.bean.LockKillPackageDaoUtil;
import github.tornaco.xposedmoduletest.bean.RFKillPackage;
import github.tornaco.xposedmoduletest.bean.RFKillPackageDaoUtil;
import github.tornaco.xposedmoduletest.provider.AutoStartPackageProvider;
import github.tornaco.xposedmoduletest.provider.BootPackageProvider;
import github.tornaco.xposedmoduletest.provider.LockKillPackageProvider;
import github.tornaco.xposedmoduletest.provider.RFKillPackageProvider;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.bean.NetworkRestriction;
import github.tornaco.xposedmoduletest.xposed.bean.NetworkRestrictionList;
import github.tornaco.xposedmoduletest.xposed.service.bandwidth.BandwidthCommandCompat;
import github.tornaco.xposedmoduletest.xposed.service.provider.SystemSettings;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static github.tornaco.xposedmoduletest.xposed.app.XAshmanManager.POLICY_REJECT_NONE;
import static github.tornaco.xposedmoduletest.xposed.app.XAshmanManager.POLICY_REJECT_ON_DATA;
import static github.tornaco.xposedmoduletest.xposed.app.XAshmanManager.POLICY_REJECT_ON_WIFI;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

public class XAshmanServiceImpl extends XAshmanServiceAbs {

    private static final boolean DEBUG_BROADCAST = true;
    private static final boolean DEBUG_SERVICE = true;

    private static final Set<String> WHITE_LIST = new HashSet<>();
    private static final Set<Pattern> WHITE_LIST_PATTERNS = new HashSet<>();
    // To prevent the apps with system signature added to white list.
    private static final Set<String> WHITE_LIST_HOOK = new HashSet<>();
    // Installed in system/, not contains system-packages and persist packages.
    private static final Set<String> SYSTEM_APPS = new HashSet<>();

    private UUID mSerialUUID = UUID.randomUUID();

    private static int sClientUID = 0;

    private final ExecutorService mWorkingService = Executors.newCachedThreadPool();
    private final ExecutorService mLoggingService = Executors.newSingleThreadExecutor();

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, String> mPackagesCache = new HashMap<>();

    private final Map<String, BlockRecord2> mBlockRecords = new HashMap<>();

    private Handler h, lazyH;

    private final Holder<String> mAudioFocusedPackage = new Holder<>();

    private AtomicBoolean mWhiteSysAppEnabled = new AtomicBoolean(true);
    private AtomicBoolean mBootBlockEnabled = new AtomicBoolean(false);
    private AtomicBoolean mStartBlockEnabled = new AtomicBoolean(false);
    private AtomicBoolean mLockKillEnabled = new AtomicBoolean(false);

    private AtomicBoolean mAutoAddToBlackListForNewApp = new AtomicBoolean(false);

    private AtomicBoolean mLockKillDoNotKillAudioEnabled = new AtomicBoolean(true);
    private AtomicBoolean mRootActivityFinishKillEnabled = new AtomicBoolean(false);
    private AtomicBoolean mCompSettingBlockEnabled = new AtomicBoolean(false);

    // FIXME Now we force set control mode to BLACK LIST.
    private AtomicInteger mControlMode = new AtomicInteger(XAshmanManager.ControlMode.BLACK_LIST);

    private long mLockKillDelay;

    private final Map<String, BootCompletePackage> mBootControlListPackages = new HashMap<>();
    private final Map<String, AutoStartPackage> mStartControlListPackages = new HashMap<>();
    private final Map<String, LockKillPackage> mLockKillControlListPackages = new HashMap<>();
    private final Map<String, RFKillPackage> mRFKillControlListPackages = new HashMap<>();

    // FIXME Change to remote callbacks.
    private final Set<AshManHandler.WatcherClient> mWatcherClients = new HashSet<>();

    // Safe mode is the last clear place user can stay.
    private boolean mIsSafeMode = false;

    private boolean mIsSystemReady = false;

    private BroadcastReceiver mScreenReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        onScreenOff();
                    }

                    if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                        onUserPresent();
                    }
                }
            };

    private void onUserPresent() {
        h.sendEmptyMessage(AshManHandlerMessages.MSG_ONSCREENON);
    }

    private void onScreenOff() {
        h.sendEmptyMessage(AshManHandlerMessages.MSG_ONSCREENOFF);
    }

    private BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action == null || intent.getData() == null) {
                // They send us bad action~
                return;
            }

            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (packageName == null) return;
                    parsePackageAsync(packageName);


                    try {
                        XAshmanManager x = XAshmanManager.get();
                        if (x.isServiceAvailable() && x.isAutoAddBlackEnabled()) {
                            if (!isInSystemAppList(packageName) && !isInWhiteList(packageName)) {
                                x.addOrRemoveBootBlockApps(new String[]{packageName}, XAshmanManager.Op.ADD);
                                x.addOrRemoveRFKApps(new String[]{packageName}, XAshmanManager.Op.ADD);
                                x.addOrRemoveLKApps(new String[]{packageName}, XAshmanManager.Op.ADD);
                                x.addOrRemoveStartBlockApps(new String[]{packageName}, XAshmanManager.Op.ADD);

                                XposedLog.verbose("Add to black list for new app.");
                                showNotification(context, String.valueOf(PkgUtil.loadNameByPkgName(context, packageName)));
                            }
                        }
                    } catch (Throwable e) {
                        XposedLog.wtf(Log.getStackTraceString(e));
                    }

                    break;
                case Intent.ACTION_PACKAGE_REPLACED:
                    packageName = intent.getData().getSchemeSpecificPart();
                    if (packageName == null) return;
                    parsePackageAsync(packageName);

                    break;
            }
        }
    };

    private void parsePackageAsync(final String... pkg) {
        mWorkingService.execute(new Runnable() {
            @Override
            public void run() {
                cachePackages(pkg);
            }
        });
    }

    private static final AtomicInteger NOTIFICATION_ID = new AtomicInteger(0);

    private void showNotification(Context context, String name) {
        XposedLog.verbose("Add to black list showNotification: " + name);
        NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID.getAndIncrement(),
                        new Notification.Builder(context)
                                .setContentTitle("新增阻止应用")
                                .setContentText("已经阻止 " + name + " 的自启动，关联启动等。")
                                .setSmallIcon(android.R.drawable.ic_dialog_info)
                                .build());
    }

    private void cachePackages(final String... pkg) {

        final PackageManager pm = getContext().getPackageManager();

        Collections.consumeRemaining(pkg, new Consumer<String>() {
            @Override
            public void accept(String s) {
                ApplicationInfo applicationInfo;
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        applicationInfo = pm.getApplicationInfo(s, PackageManager.MATCH_UNINSTALLED_PACKAGES);
                    } else {
                        applicationInfo = pm.getApplicationInfo(s, PackageManager.GET_UNINSTALLED_PACKAGES);
                    }
                    int uid = applicationInfo.uid;
                    String pkg = applicationInfo.packageName;
                    if (TextUtils.isEmpty(pkg)) return;

                    if (XposedLog.isVerboseLoggable())
                        XposedLog.verbose("Cached pkg:" + pkg + "-" + uid);
                    mPackagesCache.put(uid, pkg);

                    if (isIME(pkg)) {
                        addToWhiteList(pkg);
                    }
                    if (PkgUtil.isHomeApp(getContext(), pkg)) {
                        addToWhiteList(pkg);
                    }
                    if (PkgUtil.isDefaultSmsApp(getContext(), pkg)) {
                        addToWhiteList(pkg);
                    }
                } catch (Exception ignored) {

                }
            }
        });
    }

    private void cachePackages() {
        final PackageManager pm = this.getContext().getPackageManager();

        // Retrieve our package first.
        try {
            ApplicationInfo applicationInfo;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID,
                        PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID,
                        PackageManager.GET_UNINSTALLED_PACKAGES);
            }
            sClientUID = applicationInfo.uid;
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("Our client app uid: " + sClientUID);
        } catch (PackageManager.NameNotFoundException e) {
            XposedLog.debug("Can not get client UID for our client:" + e);
        }

        try {
            // Filter all apps.
            List<ApplicationInfo> applicationInfos =
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ?
                            pm.getInstalledApplications(android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES)
                            : pm.getInstalledApplications(android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES);

            Collections.consumeRemaining(applicationInfos,
                    new Consumer<ApplicationInfo>() {
                        @Override
                        public void accept(ApplicationInfo applicationInfo) {
                            String pkg = applicationInfo.packageName;
                            int uid = applicationInfo.uid;
                            if (TextUtils.isEmpty(pkg)) return;

                            // Add to package cache.
                            mPackagesCache.put(uid, pkg);

                            // Add system apps to system list.
                            boolean isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                            if (isSystemApp) {

                                // Check if persist.
                                boolean isPersist = (applicationInfo.flags & ApplicationInfo.FLAG_PERSISTENT) != 0;
                                if (isPersist) {
                                    addToWhiteList(pkg);
                                    return;
                                }

                                android.content.pm.PackageInfo packageInfo = null;
                                // Check if android system uid or media, phone.
                                try {
                                    packageInfo = pm.getPackageInfo(pkg, 0);
                                    String sharedUserId = packageInfo.sharedUserId;
                                    if ("android.uid.system".equals(sharedUserId)
                                            || "android.uid.phone".equals(sharedUserId)
                                            || "android.media".equals(sharedUserId)) {
                                        XposedLog.debug("Add to white list package: " + pkg + ", sharedUid: " + sharedUserId);
                                        addToWhiteList(pkg);
                                        return;
                                    }

                                } catch (PackageManager.NameNotFoundException e) {
                                    XposedLog.wtf("NameNotFoundException: " + e + ", for: " + pkg);
                                }

                                // Check if is green app.
                                if (packageInfo != null && ComponentUtil.isGreenPackage(packageInfo)) {
                                    XposedLog.debug("Maybe green package???: " + pkg);
                                    // addToWhiteList(pkg);
                                    // return;
                                }

                                addToSystemApps(pkg);
                            }

                            if (PkgUtil.isHomeApp(getContext(), pkg)) {
                                addToWhiteList(pkg);
                            }

                            if (PkgUtil.isDefaultSmsApp(getContext(), pkg)) {
                                addToWhiteList(pkg);
                            }

                            if (isIME(pkg)) {
                                addToWhiteList(pkg);
                            }
                        }
                    });
        } catch (Exception ignored) {
            XposedLog.debug("Can not getSingleton UID for our client:" + ignored);
        }
    }

    synchronized private ValueExtra<Boolean, String> loadBootPackageSettings() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(BootPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                BootCompletePackage bootCompletePackage = BootCompletePackageDaoUtil.readEntity(cursor, 0);
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("Boot pkg reader readEntity of: " + bootCompletePackage);
                String key = bootCompletePackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mBootControlListPackages.put(key, bootCompletePackage);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail query boot pkgs:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        } finally {
            Closer.closeQuietly(cursor);
        }
        return new ValueExtra<>(true, "Read count: " + mBootControlListPackages.size());
    }

    synchronized private ValueExtra<Boolean, String> loadStartPackageSettings() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(AutoStartPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                AutoStartPackage autoStartPackage = AutoStartPackageDaoUtil.readEntity(cursor, 0);
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("Start white list pkg reader readEntity of: " + autoStartPackage);
                String key = autoStartPackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mStartControlListPackages.put(key, autoStartPackage);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail query start pkgs:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        } finally {
            Closer.closeQuietly(cursor);
        }

        return new ValueExtra<>(true, "Read count: " + mStartControlListPackages.size());
    }

    synchronized private ValueExtra<Boolean, String> loadLockKillPackageSettings() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(LockKillPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                LockKillPackage lockKillPackage = LockKillPackageDaoUtil.readEntity(cursor, 0);
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("Lock kill white list pkg reader readEntity of: " + lockKillPackage);
                String key = lockKillPackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mLockKillControlListPackages.put(key, lockKillPackage);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail query lk pkgs:\n" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(cursor);
        }

        return new ValueExtra<>(true, "Read count: " + mLockKillControlListPackages.size());
    }

    synchronized private ValueExtra<Boolean, String> loadRFKillPackageSettings() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(RFKillPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                RFKillPackage rfKillPackage = RFKillPackageDaoUtil.readEntity(cursor, 0);
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("RF kill white list pkg reader readEntity of: " + rfKillPackage);
                String key = rfKillPackage.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mRFKillControlListPackages.put(key, rfKillPackage);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail query rf pkgs:\n" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(cursor);
        }

        return new ValueExtra<>(true, "Read count: " + mRFKillControlListPackages.size());
    }

    private ValueExtra<Boolean, String> registerBootPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(BootPackageProvider.CONTENT_URI,
                    false, new ContentObserver(h) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadBootPackageSettings();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XposedLog.wtf("Fail registerContentObserver@BootPackageProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }

    private ValueExtra<Boolean, String> registerStartPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(AutoStartPackageProvider.CONTENT_URI,
                    false, new ContentObserver(h) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadStartPackageSettings();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XposedLog.wtf("Fail registerContentObserver@AutoStartPackageProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }


    private ValueExtra<Boolean, String> registerLKPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(LockKillPackageProvider.CONTENT_URI,
                    false, new ContentObserver(h) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadLockKillPackageSettings();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XposedLog.wtf("Fail registerContentObserver@LockKillPackageProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }

    private ValueExtra<Boolean, String> registerRFKPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(RFKillPackageProvider.CONTENT_URI,
                    false, new ContentObserver(h) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadRFKillPackageSettings();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XposedLog.wtf("Fail registerContentObserver@RFKillPackageProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }


    private void whiteIMEPackages() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodInfos = imm != null ? imm.getInputMethodList() : null;
        if (methodInfos == null) return;
        for (InputMethodInfo inputMethodInfo : methodInfos) {
            String pkg = inputMethodInfo.getPackageName();
            addToWhiteList(pkg);
            if (XposedLog.isVerboseLoggable()) XposedLog.verbose("whiteIMEPackages: " + pkg);
        }
    }

    private boolean isIME(String pkg) {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        List<InputMethodInfo> methodInfos = imm != null ? imm.getInputMethodList() : null;
        if (methodInfos == null) return false;
        for (InputMethodInfo inputMethodInfo : methodInfos) {
            String pkgIME = inputMethodInfo.getPackageName();
            if (pkg.equals(pkgIME)) return true;
        }
        return false;
    }

    private static boolean isInWhiteList(String pkg) {
        if (pkg == null) return false;
        boolean inWhite = WHITE_LIST.contains(pkg);
        if (inWhite) return true;
        if (WHITE_LIST_PATTERNS.size() == 0) return false;

        for (Pattern p : WHITE_LIST_PATTERNS) {
            if (p.matcher(pkg).find()) {
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("Match white list for pattern: " + p.toString() + ", pkg: " + pkg);
                addToWhiteList(pkg);
                return true;
            }
        }
        return false;
    }

    private synchronized static void addWhiteListPattern(Pattern pattern) {
        if (!WHITE_LIST_PATTERNS.contains(pattern)) {
            WHITE_LIST_PATTERNS.add(pattern);
        }
    }

    private synchronized static void addToWhiteList(String pkg) {
        if (WHITE_LIST_HOOK.contains(pkg)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("Not add to white list because it is hooked: " + pkg);
            return;
        }
        if (!WHITE_LIST.contains(pkg)) {
            WHITE_LIST.add(pkg);
        }
    }

    private synchronized static void addToWhiteListHook(String pkg) {
        if (!WHITE_LIST_HOOK.contains(pkg)) {
            WHITE_LIST_HOOK.add(pkg);
        }
    }

    private static boolean isInSystemAppList(String pkg) {
        return SYSTEM_APPS.contains(pkg);
    }

    private synchronized static void addToSystemApps(String pkg) {
        if (!SYSTEM_APPS.contains(pkg)) {
            SYSTEM_APPS.add(pkg);
        }
    }

    private void checkSafeMode() {
        mIsSafeMode = getContext().getPackageManager().isSafeMode();
    }

    private boolean isSystemReady() {
        return mIsSystemReady;
    }

    private void getConfigFromSettings() {
        try {
            boolean whiteSysApp = (boolean) SystemSettings.ASH_WHITE_SYS_APP_ENABLED_B.readFromSystemSettings(getContext());
            mWhiteSysAppEnabled.set(whiteSysApp);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("whiteSysAapp: " + String.valueOf(whiteSysApp));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

//        try {
//            int controlMode = (int) SystemSettings.ASH_CONTROL_MODE_I.readFromSystemSettings(getContext());
//            mControlMode.set(controlMode);
//            if (XposedLog.isVerboseLoggable())
//                XposedLog.verbose("controlMode: " + String.valueOf(controlMode));
//        } catch (Throwable e) {
//            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
//        }

        try {
            boolean bootBlockEnabled = (boolean) SystemSettings.BOOT_BLOCK_ENABLED_B.readFromSystemSettings(getContext());
            mBootBlockEnabled.set(bootBlockEnabled);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("bootBlockEnabled: " + String.valueOf(bootBlockEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean startBlockEnabled = (boolean) SystemSettings.START_BLOCK_ENABLED_B.readFromSystemSettings(getContext());
            mStartBlockEnabled.set(startBlockEnabled);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("startBlockEnabled:" + String.valueOf(startBlockEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean lockKillEnabled = (boolean) SystemSettings.LOCK_KILL_ENABLED_B.readFromSystemSettings(getContext());
            mLockKillEnabled.set(lockKillEnabled);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("lockKillEnabled: " + String.valueOf(lockKillEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean autoAddBlack = (boolean) SystemSettings.AUTO_BLACK_FOR_NEW_INSTALLED_APP_B.readFromSystemSettings(getContext());
            mAutoAddToBlackListForNewApp.set(autoAddBlack);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("autoAddBlack: " + String.valueOf(autoAddBlack));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean lockKillDoNotKillAudioEnabled = (boolean) SystemSettings.LOCK_KILL_DONT_KILL_AUDIO_ENABLED_B
                    .readFromSystemSettings(getContext());
            mLockKillDoNotKillAudioEnabled.set(lockKillDoNotKillAudioEnabled);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("lockKillDoNotKillAudioEnabled: " + String.valueOf(lockKillDoNotKillAudioEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }


        try {
            boolean rootKillEnabled = (boolean) SystemSettings.ROOT_ACTIVITY_KILL_ENABLED_B
                    .readFromSystemSettings(getContext());
            mRootActivityFinishKillEnabled.set(rootKillEnabled);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("rootKillEnabled: " + String.valueOf(rootKillEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean compSettingBlockEnabled = (boolean) SystemSettings.COMP_SETTING_BLOCK_ENABLED_B
                    .readFromSystemSettings(getContext());
            mCompSettingBlockEnabled.set(compSettingBlockEnabled);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("compSettingBlockEnabled: " + String.valueOf(compSettingBlockEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            mLockKillDelay = (long) SystemSettings.LOCK_KILL_DELAY_L.readFromSystemSettings(getContext());
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mLockKillDelay: " + String.valueOf(mLockKillDelay));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
    }

    @Override
    @InternalCall
    public boolean checkService(ComponentName serviceComp, int callerUid) {
        if (serviceComp == null) return true;
        String appPkg = serviceComp.getPackageName();
        CheckResult res = checkServiceDetailed(appPkg, callerUid);
        // Saving res record.
        if (!res.res) logServiceEventToMemory(
                ServiceEvent.builder()
                        .service("Service")
                        .why(res.why)
                        .allowed(res.res)
                        .appName(null)
                        .pkg(appPkg)
                        .when(System.currentTimeMillis())
                        .build());
        if (DEBUG_SERVICE)
            if (XposedLog.isVerboseLoggable())
                XposedLog.verboseOn("XAshmanService checkService returning: " + res + "for: " +
                                PkgUtil.loadNameByPkgName(getContext(), appPkg)
                                + ", comp: " + serviceComp
                                + ", caller: " + PkgUtil.pkgForUid(getContext(), callerUid),
                        mLoggingService);
        return res.res;
    }

    private CheckResult checkServiceDetailed(String servicePkgName, int callerUid) {
        if (!isSystemReady()) return CheckResult.SYSTEM_NOT_READY;
        // Disabled case.
        if (!isStartBlockEnabled()) return CheckResult.SERVICE_CHECK_DISABLED;

        if (TextUtils.isEmpty(servicePkgName)) return CheckResult.BAD_ARGS;

        String callerPkgName =
                mPackagesCache.get(callerUid);
        if (callerPkgName == null) {
            callerPkgName = PkgUtil.pkgForUid(getContext(), callerUid);
        }

        if (isInWhiteList(servicePkgName)) {
            return CheckResult.WHITE_LISTED;
        }

        if (isWhiteSysAppEnabled() && isInSystemAppList(servicePkgName)) {
            return CheckResult.SYSTEM_APP;
        }

        // Service from/to same app is allowed.
        if (servicePkgName.equals(callerPkgName)) {
            return CheckResult.SAME_CALLER;
        }

        if (PkgUtil.isHomeApp(getContext(), servicePkgName)) {
            return CheckResult.HOME_APP;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), servicePkgName)) {
            return CheckResult.SMS_APP;
        }

        if (PkgUtil.isAppRunning(getContext(), servicePkgName)) {
            return CheckResult.APP_RUNNING;
        }

        // If this app is not in good condition, and user choose to block:
        boolean blockedByUser = isPackageStartBlockByUser(servicePkgName);
        // User block!!!
        if (blockedByUser) {
            return CheckResult.USER_DENIED;
        }

        // By default, we allow.
        return CheckResult.ALLOWED_GENERAL;
    }

    @Override
    @InternalCall
    public boolean checkBroadcast(String action, int receiverUid, int callerUid) {
        CheckResult res = checkBroadcastDetailed(action, receiverUid, callerUid);
        // Saving res record.
        if (!res.res) logBroadcastEventToMemory(
                BroadcastEvent.builder()
                        .action(action)
                        .allowed(res.res)
                        .appName(null)
                        .receiver(receiverUid)
                        .caller(callerUid)
                        .when(System.currentTimeMillis())
                        .why(res.why)
                        .build());

        if (DEBUG_BROADCAST)
            if (XposedLog.isVerboseLoggable())
                XposedLog.verboseOn("XAshmanService checkBroadcast returning: "
                                + res + " for: "
                                + PkgUtil.loadNameByPkgName(getContext(), mPackagesCache.get(receiverUid))
                                + " receiverUid: " + receiverUid
                                + " callerUid: " + callerUid
                                + " action: " + action
                                + ", caller: " + PkgUtil.pkgForUid(getContext(), callerUid),
                        mLoggingService);
        return res.res;
    }

    @Override
    @InternalCall
    public boolean checkComponentSetting(ComponentName componentName, int newState,
                                         int flags, int callingUid) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("checkComponentSetting: " + componentName
                    + ", calling uid: " + callingUid
                    + ", state: " + newState);

        if (componentName == null) return true;

        String pkgName = componentName.getPackageName();

        //noinspection ConstantConditions
        if (pkgName == null) return true;

        if (BuildConfig.APPLICATION_ID.equals(pkgName)
                && callingUid != sClientUID
                && callingUid != android.os.Process.myUid()
                && callingUid > 1000) {
            // Prevent our component modifued by someone else!!!
            XposedLog.wtf("Wht the fuck? Someone want's to disable our core components!!! Let's see who" +
                    " it is: " + (callingUid == Process.SHELL_UID ? "SHELL" : callingUid) + ", shit it!!!");
            throw new IllegalStateException("Do not change any component of AppGuard!!!");
        }

        if (newState != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                && newState != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("It is not enable state, allow component setting.");
            return true;
        }

        if (isInWhiteList(pkgName)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("It is from while list, allow component setting.");
            return true;
        }

        if (isWhiteSysAppEnabled() && isInSystemAppList(pkgName)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("It is from system app list, allow component setting.");
            return true;
        }

        if (pkgName.contains("com.google.android")) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("It is maybe from google apps list, allow component setting.");
            return true;
        }

        if (pkgName.contains("com.qualcomm.qti")
                || pkgName.contains("com.qti.smq")) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("It is maybe from qcom apps list, allow component setting.");
            return true;
        }

        if (callingUid == sClientUID || callingUid <= 1000
                || callingUid == android.os.Process.myUid()) {
            // Do not block system settings.
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("It is us or the system, allow component setting.");
            return true;
        }

        if (!isCompSettingBlockEnabledEnabled()) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("Block is not enabled, allow component setting.");
            return true;
        }

        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("Block component setting.");
        return false;
    }

    @Override
    @InternalCall
    public void onActivityDestroy(Intent intent, String reason) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("onActivityDestroy: " + intent + ", reason: " + reason);
        if (!isRFKillEnabled()) return;
        lazyH.obtainMessage(AshManLZHandlerMessages.MSG_ONACTIVITYDESTROY, intent).sendToTarget();
    }

    private boolean isPackageRFKillEnabled(String pkg) {
        if (!isRFKillEnabled()) return false;
        // If this app is not in good condition, but user
        // does not block, we also allow it to start.
        boolean rfkByUser = isPackageRFKByUser(pkg);
        if (!rfkByUser) {
            return false;
        }

        if (isInWhiteList(pkg)) {
            return false;
        }

        if (isWhiteSysAppEnabled() && isInSystemAppList(pkg)) {
            return false;
        }

        if (PkgUtil.isHomeApp(getContext(), pkg)) {
            return false;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), pkg)) {
            return false;
        }
        return true;
    }

    @Override
    @BinderCall
    public List<BlockRecord2> getBlockRecords() throws RemoteException {
        enforceCallingPermissions();
        synchronized (mBlockRecords) {
            return Lists.newArrayList(mBlockRecords.values());
        }
    }

    @Override
    @BinderCall
    public void clearBlockRecords() throws RemoteException {
        enforceCallingPermissions();
        h.removeMessages(AshManHandlerMessages.MSG_CLEARBLOCKRECORDS);
        h.obtainMessage(AshManHandlerMessages.MSG_CLEARBLOCKRECORDS).sendToTarget();
    }

    @Override
    @BinderCall
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETCOMPONENTENABLEDSETTING, newState, flags, componentName).sendToTarget();
    }

    @Override
    @BinderCall
    public int getComponentEnabledSetting(ComponentName componentName) throws RemoteException {
        enforceCallingPermissions();
        long id = Binder.clearCallingIdentity();
        try {
            PackageManager pm = getContext().getPackageManager();
            return pm.getComponentEnabledSetting(componentName);
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public int getApplicationEnabledSetting(final String packageName) throws RemoteException {
        enforceCallingPermissions();
        long id = Binder.clearCallingIdentity();
        try {
            PackageManager pm = getContext().getPackageManager();
            return pm.getApplicationEnabledSetting(packageName);
        } finally {
            Binder.restoreCallingIdentity(id);
        }

    }

    @Override
    public void setApplicationEnabledSetting(String packageName, int newState, int flags) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETAPPLICATIONENABLEDSETTING, newState, flags, packageName).sendToTarget();
    }

    @Override
    public void watch(IAshmanWatcher w) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(w, "IAshmanWatcher is null");
        AshManHandler.WatcherClient watcherClient = new AshManHandler.WatcherClient(w);
        h.obtainMessage(AshManHandlerMessages.MSG_WATCH, watcherClient).sendToTarget();
    }

    @Override
    @BinderCall
    public void unWatch(IAshmanWatcher w) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(w, "IAshmanWatcher is null");
        AshManHandler.WatcherClient watcherClient = new AshManHandler.WatcherClient(w);
        h.obtainMessage(AshManHandlerMessages.MSG_UNWATCH, watcherClient).sendToTarget();
    }

    @Override
    @BinderCall
    public void setNetworkPolicyUidPolicy(int uid, int policy) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETNETWORKPOLICYUIDPOLICY, uid, policy).sendToTarget();
    }

    @Override
    public void restart() throws RemoteException {
        enforceCallingPermissions();
        lazyH.post(new Runnable() {
            @Override
            public void run() {
                Zygote.execShell("reboot"); //FIXME Change to soft reboot?
            }
        });
    }

    @Override
    @BinderCall
    public void setCompSettingBlockEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETCOMPSETTINGBLOCKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isCompSettingBlockEnabledEnabled() {
        enforceCallingPermissions();
        return mCompSettingBlockEnabled.get();
    }

    private static String[] convertObjectArrayToStringArray(Object[] objArr) {
        if (objArr == null || objArr.length == 0) {
            return new String[0];
        }
        String[] out = new String[objArr.length];
        for (int i = 0; i < objArr.length; i++) {
            Object o = objArr[i];
            if (o == null) continue;
            String pkg = String.valueOf(o);
            if (o instanceof AutoStartPackage) {
                pkg = ((AutoStartPackage) o).getPkgName();
            } else if (o instanceof BootCompletePackage) {
                pkg = ((BootCompletePackage) o).getPkgName();
            } else if (o instanceof LockKillPackage) {
                pkg = ((LockKillPackage) o).getPkgName();
            } else if (o instanceof RFKillPackage) {
                pkg = ((RFKillPackage) o).getPkgName();
            }
            out[i] = pkg;
        }
        return out;
    }

    @Override
    public String[] getWhiteListApps(int filterOptions) throws RemoteException {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getWhiteListApps: " + filterOptions);
        enforceCallingPermissions();
        Object[] data = WHITE_LIST.toArray(); // FIXME, no sync protect?
        return convertObjectArrayToStringArray(data);
    }

    @Override
    public String[] getBootBlockApps(boolean block) throws RemoteException {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getBootBlockApps: " + block);
        enforceCallingPermissions();
        if (!block) {
            Collection<String> packages = mPackagesCache.values();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (isPackageBootBlockByUser(s)) return;
                    if (isInWhiteList(s)) return;
                    if (isWhiteSysAppEnabled() && isInSystemAppList(s)) return;
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Collection<BootCompletePackage> packages = mBootControlListPackages.values();
            if (packages.size() == 0) {
                return new String[0];
            }
            if (isWhiteSysAppEnabled()) {
                final List<String> noSys = Lists.newArrayList();
                Collections.consumeRemaining(packages.toArray(),
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {
                                BootCompletePackage p = (BootCompletePackage) o;
                                if (!isInSystemAppList(p.getPkgName())) {
                                    noSys.add(p.getPkgName());
                                }
                            }
                        });
                return convertObjectArrayToStringArray(noSys.toArray());
            }
            return convertObjectArrayToStringArray(packages.toArray());
        }
    }

    @Override
    public void addOrRemoveBootBlockApps(String[] packages, int op) throws RemoteException {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveBootBlockApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        switch (op) {
            case XAshmanManager.Op.ADD:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                BootCompletePackage b = new BootCompletePackage();
                                b.setPkgName(s);
                                b.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(getContext(), s)));
                                mBootControlListPackages.put(s, b);
                                BootPackageProvider.insert(getContext(), b);
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail add boot pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XposedLog.wtf("Fail add boot packages...");
                    throw new IllegalStateException(e);
                }
                break;
            case XAshmanManager.Op.REMOVE:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                BootCompletePackage b = new BootCompletePackage();
                                b.setPkgName(s);
                                mBootControlListPackages.remove(s);
                                BootPackageProvider.delete(getContext(), b);
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail delete boot pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XposedLog.wtf("Fail remove boot packages...");
                    throw new IllegalStateException(e);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String[] getStartBlockApps(boolean block) throws RemoteException {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getStartBlockApps: " + block);
        enforceCallingPermissions();
        if (!block) {
            Collection<String> packages = mPackagesCache.values();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (isPackageStartBlockByUser(s)) return;
                    if (isInWhiteList(s)) return;
                    if (isWhiteSysAppEnabled() && isInSystemAppList(s)) return;
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Collection<AutoStartPackage> packages = mStartControlListPackages.values();
            if (packages.size() == 0) {
                return new String[0];
            }
            if (isWhiteSysAppEnabled()) {
                final List<String> noSys = Lists.newArrayList();
                Collections.consumeRemaining(packages.toArray(),
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {
                                AutoStartPackage p = (AutoStartPackage) o;
                                if (!isInSystemAppList(p.getPkgName())) {
                                    noSys.add(p.getPkgName());
                                }
                            }
                        });
                return convertObjectArrayToStringArray(noSys.toArray());
            }
            return convertObjectArrayToStringArray(packages.toArray());
        }
    }

    @Override
    public void addOrRemoveStartBlockApps(String[] packages, int op) throws RemoteException {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveStartBlockApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        switch (op) {
            case XAshmanManager.Op.ADD:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                AutoStartPackage b = new AutoStartPackage();
                                b.setPkgName(s);
                                b.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(getContext(), s)));
                                mStartControlListPackages.put(s, b);
                                AutoStartPackageProvider.insert(getContext(), b);
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail add start pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XposedLog.wtf("Fail add start packages...");
                    throw new IllegalStateException(e);
                }
                break;
            case XAshmanManager.Op.REMOVE:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                AutoStartPackage b = new AutoStartPackage();
                                b.setPkgName(s);
                                mStartControlListPackages.remove(s);
                                AutoStartPackageProvider.delete(getContext(), b);
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail delete start pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XposedLog.wtf("Fail remove start packages...");
                    throw new IllegalStateException(e);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String[] getLKApps(boolean kill) throws RemoteException {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getLKApps: " + kill);
        enforceCallingPermissions();
        if (!kill) {
            Collection<String> packages = mPackagesCache.values();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (isPackageLKByUser(s)) return;
                    if (isInWhiteList(s)) return;
                    if (isWhiteSysAppEnabled() && isInSystemAppList(s)) return;
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Collection<LockKillPackage> packages = mLockKillControlListPackages.values();
            if (packages.size() == 0) {
                return new String[0];
            }
            if (isWhiteSysAppEnabled()) {
                final List<String> noSys = Lists.newArrayList();
                Collections.consumeRemaining(packages.toArray(),
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {
                                LockKillPackage p = (LockKillPackage) o;
                                if (!isInSystemAppList(p.getPkgName())) {
                                    noSys.add(p.getPkgName());
                                }
                            }
                        });
                return convertObjectArrayToStringArray(noSys.toArray());
            }
            return convertObjectArrayToStringArray(packages.toArray());
        }
    }

    @Override
    public void addOrRemoveLKApps(String[] packages, int op) throws RemoteException {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveLKApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        switch (op) {
            case XAshmanManager.Op.ADD:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                LockKillPackage b = new LockKillPackage();
                                b.setPkgName(s);
                                b.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(getContext(), s)));
                                mLockKillControlListPackages.put(s, b);
                                LockKillPackageProvider.insert(getContext(), b);
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail add lk pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XposedLog.wtf("Fail add lk packages...");
                    throw new IllegalStateException(e);
                }
                break;
            case XAshmanManager.Op.REMOVE:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                LockKillPackage b = new LockKillPackage();
                                b.setPkgName(s);
                                mLockKillControlListPackages.remove(s);
                                LockKillPackageProvider.delete(getContext(), b);
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail delete lk pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XposedLog.wtf("Fail remove lk packages...");
                    throw new IllegalStateException(e);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public String[] getRFKApps(boolean kill) throws RemoteException {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getRFKApps: " + kill);
        enforceCallingPermissions();
        if (!kill) {
            Collection<String> packages = mPackagesCache.values();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (isPackageRFKByUser(s)) return;
                    if (isInWhiteList(s)) return;
                    if (isWhiteSysAppEnabled() && isInSystemAppList(s)) return;
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Collection<RFKillPackage> packages = mRFKillControlListPackages.values();
            if (packages.size() == 0) {
                return new String[0];
            }
            if (isWhiteSysAppEnabled()) {
                final List<String> noSys = Lists.newArrayList();
                Collections.consumeRemaining(packages.toArray(),
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {
                                RFKillPackage p = (RFKillPackage) o;
                                if (!isInSystemAppList(p.getPkgName())) {
                                    noSys.add(p.getPkgName());
                                }
                            }
                        });
                return convertObjectArrayToStringArray(noSys.toArray());
            }
            return convertObjectArrayToStringArray(packages.toArray());
        }
    }

    @Override
    public void addOrRemoveRFKApps(String[] packages, int op) throws RemoteException {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveRFKApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        switch (op) {
            case XAshmanManager.Op.ADD:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                RFKillPackage b = new RFKillPackage();
                                b.setPkgName(s);
                                b.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(getContext(), s)));
                                mRFKillControlListPackages.put(s, b);
                                RFKillPackageProvider.insert(getContext(), b);
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail add rf pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XposedLog.wtf("Fail add rf packages...");
                    throw new IllegalStateException(e);
                }
                break;
            case XAshmanManager.Op.REMOVE:
                try {
                    Collections.consumeRemaining(packages, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            try {
                                RFKillPackage b = new RFKillPackage();
                                b.setPkgName(s);
                                mRFKillControlListPackages.remove(s);
                                RFKillPackageProvider.delete(getContext(), b);
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail delete rf pkg: " + s);
                            }
                        }
                    });
                } catch (Throwable e) {
                    XposedLog.wtf("Fail remove rf packages...");
                    throw new IllegalStateException(e);
                }
                break;
            default:
                break;
        }
    }

    @Override
    @BinderCall
    @Deprecated
    public void unInstallPackage(final String pkg, final IPackageUninstallCallback callback)
            throws RemoteException {
        enforceCallingPermissions();
    }

    @Override
    public boolean isLockKillDoNotKillAudioEnabled() {
        enforceCallingPermissions();
        return mLockKillDoNotKillAudioEnabled.get();
    }

    @Override
    public void setLockKillDoNotKillAudioEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLDONOTKILLAUDIOENABLED, enabled)
                .sendToTarget();
    }

    @Override
    public int getControlMode() {
        enforceCallingPermissions();
        return mControlMode.get();
    }

    private boolean isWhiteListControlMode() {
        return mControlMode.get() == XAshmanManager.ControlMode.WHITE_LIST;
    }

    @Override
    public void setControlMode(int mode) throws RemoteException {
        if (mode != XAshmanManager.ControlMode.BLACK_LIST && mode != XAshmanManager.ControlMode.WHITE_LIST) {
            throw new IllegalArgumentException("Bad mode:" + mode);
        }
        enforceCallingPermissions();

        h.obtainMessage(AshManHandlerMessages.MSG_SETCONTROLMODE, mode).sendToTarget();
    }

    @Override
    public String getBuildSerial() throws RemoteException {
        return BuildFingerprintBuildHostInfo.BUILD_FINGER_PRINT;
    }

    @Override
    @BinderCall(restrict = "any")
    public boolean isAutoAddBlackEnabled() throws RemoteException {
        return mAutoAddToBlackListForNewApp.get();
    }

    @Override
    public void setAutoAddBlackEnable(boolean enable) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETAUTOADDBLACKENABLE, enable)
                .sendToTarget();
    }

    @Override
    public void forceReloadPackages() throws RemoteException {
        enforceCallingPermissions();
        h.removeMessages(AshManHandlerMessages.MSG_FORCERELOADPACKAGES);
        h.sendEmptyMessage(AshManHandlerMessages.MSG_FORCERELOADPACKAGES);
    }

    private CheckResult checkBroadcastDetailed(String action, int receiverUid, int callerUid) {

        // Check if this is a boot complete action.
        if (isBootCompleteBroadcastAction(action)) {
            return checkBootCompleteBroadcast(receiverUid, callerUid);
        }

        if (!isSystemReady()) return CheckResult.SYSTEM_NOT_READY;

        // Disabled case.
        if (!isStartBlockEnabled()) return CheckResult.BROADCAST_CHECK_DISABLED;

        String receiverPkgName =
                mPackagesCache.get(receiverUid);
        if (receiverPkgName == null) {
            receiverPkgName = PkgUtil.pkgForUid(getContext(), receiverUid);
        }
        if (TextUtils.isEmpty(receiverPkgName)) return CheckResult.BAD_ARGS;

        // Broadcast from/to same app is allowed.
        if (callerUid == receiverUid) {
            return CheckResult.SAME_CALLER;
        }

        return checkBroadcastDetailed(receiverPkgName);
    }

    private CheckResult checkBroadcastDetailed(String receiverPkgName) {

        if (isInWhiteList(receiverPkgName)) {
            return CheckResult.WHITE_LISTED;
        }

        if (isWhiteSysAppEnabled() && isInSystemAppList(receiverPkgName)) {
            return CheckResult.SYSTEM_APP;
        }

        if (PkgUtil.isHomeApp(getContext(), receiverPkgName)) {
            return CheckResult.HOME_APP;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), receiverPkgName)) {
            return CheckResult.SMS_APP;
        }

        if (PkgUtil.isAppRunning(getContext(), receiverPkgName)) {
            return CheckResult.APP_RUNNING;
        }

        // It is in user black list.
        boolean blockByUser = isPackageStartBlockByUser(receiverPkgName);
        if (blockByUser) {
            return CheckResult.USER_DENIED;
        }

        if (!mStartPkgLoaded.get()) {
            return CheckResult.DENIED_USER_LIST_NOT_READY;
        }

        return CheckResult.ALLOWED_GENERAL;
    }

    private boolean isPackageBootBlockByUser(String pkg) {
        BootCompletePackage bootCompletePackage = mBootControlListPackages.get(pkg);
        return bootCompletePackage != null;
    }

    private boolean isPackageStartBlockByUser(String pkg) {
        AutoStartPackage autoStartPackage = mStartControlListPackages.get(pkg);
        return autoStartPackage != null;
    }

    private boolean isPackageLKByUser(String pkg) {
        LockKillPackage lockKillPackage = mLockKillControlListPackages.get(pkg);
        return lockKillPackage != null;
    }

    private boolean isPackageRFKByUser(String pkg) {
        RFKillPackage rfKillPackage = mRFKillControlListPackages.get(pkg);
        return rfKillPackage != null;
    }

    private CheckResult checkBootCompleteBroadcast(int receiverUid, int callerUid) {

        // Disabled case.
        if (!isBlockBlockEnabled()) return CheckResult.BOOT_CHECK_DISABLED;

        String receiverPkgName =
                mPackagesCache.get(receiverUid);
        if (receiverPkgName == null) {
            receiverPkgName = PkgUtil.pkgForUid(getContext(), receiverUid);
        }

        if (TextUtils.isEmpty(receiverPkgName)) return CheckResult.BAD_ARGS;

        if (isInWhiteList(receiverPkgName)) {
            return CheckResult.WHITE_LISTED;
        }

        if (isWhiteSysAppEnabled() && isInSystemAppList(receiverPkgName)) {
            return CheckResult.SYSTEM_APP;
        }

        if (PkgUtil.isHomeApp(getContext(), receiverPkgName)) {
            return CheckResult.HOME_APP;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), receiverPkgName)) {
            return CheckResult.SMS_APP;
        }

        boolean blockByUser = isPackageBootBlockByUser(receiverPkgName);

        if (blockByUser) {
            return CheckResult.USER_DENIED;
        }

        if (!mBootPkgLoaded.get()) {
            return CheckResult.DENIED_USER_LIST_NOT_READY;
        }

        if (XposedLog.isVerboseLoggable() && receiverPkgName.equals("com.catchingnow.tinyclipboardmanager")) {
            XposedLog.wtf("DUMP LIST FOR JZD START");

            Collections.consumeRemaining(mBootControlListPackages.values(), new Consumer<BootCompletePackage>() {
                @Override
                public void accept(BootCompletePackage bootCompletePackage) {
                    XposedLog.wtf("DUMP LIST FOR JZD: " + bootCompletePackage.getPkgName());
                }
            });
            XposedLog.wtf("DUMP LIST FOR JZD END");
        }

        return CheckResult.ALLOWED_GENERAL;
    }

    private static boolean isBootCompleteBroadcastAction(String action) {
        return Intent.ACTION_BOOT_COMPLETED.equals(action);
    }

    private void logServiceEventToMemory(final ServiceEvent serviceEvent) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                BlockRecord2 old = getBlockRecord(serviceEvent.pkg);
                long oldTimes = old == null ? 0 : old.getHowManyTimes();
                BlockRecord2 blockRecord2 = BlockRecord2.builder()
                        .pkgName(serviceEvent.pkg)
                        .appName(String.valueOf(
                                PkgUtil.loadNameByPkgName(getContext(),
                                        serviceEvent.pkg)))
                        .howManyTimes(oldTimes + 1)
                        .timeWhen(System.currentTimeMillis())
                        .build();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("SVC BlockRecord2: " + blockRecord2);
                addBlockRecord(blockRecord2);
            }
        };

        mLoggingService.execute(r);

        h.obtainMessage(AshManHandlerMessages.MSG_NOTIFYSTARTBLOCK, serviceEvent.getPkg()).sendToTarget();
    }

    private void logBroadcastEventToMemory(final BroadcastEvent broadcastEvent) {
        Runnable r = new Runnable() {
            @Override
            public void run() {

                String receiverPkgName =
                        mPackagesCache.get(broadcastEvent.receiver);
                if (receiverPkgName == null) {
                    receiverPkgName = PkgUtil.pkgForUid(getContext(), broadcastEvent.receiver);
                    if (receiverPkgName == null) return;
                }

                h.obtainMessage(AshManHandlerMessages.MSG_NOTIFYSTARTBLOCK, receiverPkgName).sendToTarget();

                BlockRecord2 old = getBlockRecord(receiverPkgName);
                long oldTimes = old == null ? 0 : old.getHowManyTimes();
                BlockRecord2 blockRecord2 = BlockRecord2.builder()
                        .pkgName(receiverPkgName)
                        .appName(String.valueOf(
                                PkgUtil.loadNameByPkgName(getContext(),
                                        receiverPkgName)))
                        .howManyTimes(oldTimes + 1)
                        .timeWhen(System.currentTimeMillis())
                        .build();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("BRD BlockRecord2: " + blockRecord2);
                addBlockRecord(blockRecord2);
            }
        };
        mLoggingService.execute(r);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        getContext().registerReceiver(mScreenReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        getContext().registerReceiver(mPackageReceiver, intentFilter);
    }

    private void inflateWhiteList() {
        String[] whiteListArr = readStringArrayFromAppGuard("default_ash_white_list_packages");
        XposedLog.debug("Res default_ash_white_list_packages: " + Arrays.toString(whiteListArr));
        Collections.consumeRemaining(whiteListArr, new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (TextUtils.isEmpty(s)) return;
                // Only accept pattern with *
                boolean isPattern = s.contains("*");
                if (isPattern) {
                    try {
                        addWhiteListPattern(Pattern.compile(s));
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("Adding pattern: " + s);
                    } catch (Throwable e) {
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("Invalid pattern: " + s);
                        addToWhiteList(s);
                    }
                } else {
                    addToWhiteList(s);
                }
            }
        });
    }

    private void inflateWhiteListHook() {
        String[] whiteListArr = readStringArrayFromAppGuard("ash_white_list_packages_hooks");
        XposedLog.debug("Res ash_white_list_packages_hooks: " + Arrays.toString(whiteListArr));
        Collections.consumeRemaining(whiteListArr, new Consumer<String>() {
            @Override
            public void accept(String s) {
                addToWhiteListHook(s);
            }
        });
    }

    private String[] readStringArrayFromAppGuard(String resName) {
        Context context = getContext();
        if (context == null) {
            XposedLog.wtf("Context is null!!!");
            return new String[0];
        }
        try {
            Context appContext =
                    context.createPackageContext(BuildConfig.APPLICATION_ID, CONTEXT_IGNORE_SECURITY);
            Resources res = appContext.getResources();
            int id = res.getIdentifier(resName, "array", BuildConfig.APPLICATION_ID);
            XposedLog.debug("readStringArrayFromAppGuard get id: " + id + ", for res: " + resName);
            if (id != 0) {
                return res.getStringArray(id);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail createPackageContext: " + Log.getStackTraceString(e));
        }
        return new String[0];
    }

    @Override
    public void publish() {
        ServiceManager.addService(XAshmanManager.ASH_MAN_SERVICE_NAME, asBinder());
        construct();
    }

    private AtomicBoolean mBootPkgLoaded = new AtomicBoolean(false), mStartPkgLoaded = new AtomicBoolean(false);

    @Override
    public void systemReady() {
        XposedLog.wtf("systemReady@" + getClass().getSimpleName());
        inflateWhiteList();
        inflateWhiteListHook();
        // Update system ready, since we can call providers now.
        mIsSystemReady = true;
        checkSafeMode();
        registerReceiver();

        // Try load providers.
        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadBootPackageSettings();
                String extra = res.getExtra();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("loadBootPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {

                mBootPkgLoaded.set(true);

                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerBootPackageObserver();
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("registerBootPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadStartPackageSettings();
                String extra = res.getExtra();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("loadStartPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {

                mStartPkgLoaded.set(true);

                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerStartPackageObserver();
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("registerStartPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadLockKillPackageSettings();
                String extra = res.getExtra();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("loadLockKillPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerLKPackageObserver();
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("registerLKPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadRFKillPackageSettings();
                String extra = res.getExtra();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("loadRFKillPackageSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerRFKPackageObserver();
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("registerRFKPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        try {
            Intent fakeIntent = new Intent("github.tornaco.xposed.app_guard.fake");
            fakeIntent.setPackage(BuildConfig.APPLICATION_ID);
            getContext().startService(fakeIntent);
            XposedLog.wtf("Fake service start called!!!!!!!!!! Oops...");
        } catch (Throwable e) {
            XposedLog.wtf("Fail start fake service: " + Log.getStackTraceString(e));
        }
    }


    // NMS API START.
    private NativeDaemonConnector mNativeDaemonConnector;

    private String mDataInterfaceName, mWifiInterfaceName;

    private BroadcastReceiver mPendingDataRestrictReceiver;

    private SparseBooleanArray mPendingRestrictOnData = new SparseBooleanArray();

    @GuardedBy("mQuotaLock")
    private final SparseBooleanArray mWifiBlacklist = new SparseBooleanArray();
    @GuardedBy("mQuotaLock")
    private final SparseBooleanArray mDataBlacklist = new SparseBooleanArray();

    private final Object mQuotaLock = new Object();

    private void initDataRestrictionBlackList() {
        try {
            File dataDir = Environment.getDataDirectory();
            File systemDir = new File(dataDir, "system/tor/data_restrict/");
            File blacklistFile = new File(systemDir, "blacklist2");

            if (!blacklistFile.exists()) {
                XposedLog.wtf("initDataRestrictionBlackList, blacklistFile not foun.@"
                        + blacklistFile.getPath());
                return;
            }

            NetworkRestrictionList networkRestrictionList
                    = NetworkRestrictionList.fromJson(FileUtil.readString(blacklistFile.getPath()));
            XposedLog.debug("initDataRestrictionBlackList, networkRestrictionList: " + networkRestrictionList);
            if (networkRestrictionList == null) return;

            List<NetworkRestriction> restrictionList = networkRestrictionList.getRestrictionList();
            if (Collections.isNullOrEmpty(restrictionList)) return;

            synchronized (mQuotaLock) {
                Collections.consumeRemaining(restrictionList, new Consumer<NetworkRestriction>() {
                    @Override
                    public void accept(NetworkRestriction networkRestriction) {
                        mDataBlacklist.put(networkRestriction.getUid(),
                                (networkRestriction.getRestrictPolicy() & POLICY_REJECT_ON_DATA) != 0);
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("Put uid networkRestriction: " + networkRestriction);
                    }
                });
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail initDataRestrictionBlackList: " + e);
        }
    }

    private void initWifiRestrictionBlackList() {
        try {
            File dataDir = Environment.getDataDirectory();
            File systemDir = new File(dataDir, "system/tor/wifi_restrict/");
            File blacklistFile = new File(systemDir, "blacklist2");

            if (!blacklistFile.exists()) {
                XposedLog.wtf("initWifiRestrictionBlackList, blacklistFile not found@"
                        + blacklistFile.getPath());
                return;
            }

            NetworkRestrictionList networkRestrictionList
                    = NetworkRestrictionList.fromJson(FileUtil.readString(blacklistFile.getPath()));
            XposedLog.debug("initWifiRestrictionBlackList, networkRestrictionList: " + networkRestrictionList);
            if (networkRestrictionList == null) return;

            List<NetworkRestriction> restrictionList = networkRestrictionList.getRestrictionList();
            if (Collections.isNullOrEmpty(restrictionList)) return;

            synchronized (mQuotaLock) {
                Collections.consumeRemaining(restrictionList, new Consumer<NetworkRestriction>() {
                    @Override
                    public void accept(NetworkRestriction networkRestriction) {
                        mWifiBlacklist.put(networkRestriction.getUid(),
                                (networkRestriction.getRestrictPolicy() & POLICY_REJECT_ON_WIFI) != 0);
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("Put uid networkRestriction: " + networkRestriction);
                    }
                });
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail initWifiRestrictionBlackList: " + e);
        }
    }

    private void applyRestrictionBlackList() {
        synchronized (mQuotaLock) {

            int N = mDataBlacklist.size();
            for (int i = 0; i < N; i++) {
                int key = mDataBlacklist.keyAt(i);
                boolean value = mDataBlacklist.valueAt(i);

                restrictAppOnDataForce(key, value);
            }

            N = mWifiBlacklist.size();
            for (int i = 0; i < N; i++) {
                int key = mWifiBlacklist.keyAt(i);
                boolean value = mWifiBlacklist.valueAt(i);

                restrictAppOnWifiForce(key, value);
            }
        }
    }

    @InternalCall
    private void writeDataRestrictionBlackList() {
        synchronized (mQuotaLock) {

            try {
                NetworkRestrictionList list = new NetworkRestrictionList();
                List<NetworkRestriction> restrictionList = new ArrayList<>();

                int N = mDataBlacklist.size();
                for (int i = 0; i < N; i++) {
                    int key = mDataBlacklist.keyAt(i);
                    boolean value = mDataBlacklist.valueAt(i);
                    NetworkRestriction n = new NetworkRestriction();
                    n.setUid(key);
                    n.setRestrictPolicy(value ? POLICY_REJECT_ON_DATA : POLICY_REJECT_NONE);

                    restrictionList.add(n);
                }

                list.setRestrictionList(restrictionList);
                String json = list.toJson();

                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("writeDataRestrictionBlackList, js: " + json);

                File dataDir = Environment.getDataDirectory();
                File systemDir = new File(dataDir, "system/tor/data_restrict/");
                File blacklistFile = new File(systemDir, "blacklist2");

                FileUtil.writeString(json, blacklistFile.getPath());
            } catch (Throwable e) {
                XposedLog.wtf("Fail writeDataRestrictionBlackList: " + Log.getStackTraceString(e));
            }
        }
    }

    @InternalCall
    private void writeWifiRestrictionBlackList() {
        synchronized (mQuotaLock) {

            try {
                NetworkRestrictionList list = new NetworkRestrictionList();
                List<NetworkRestriction> restrictionList = new ArrayList<>();

                int N = mWifiBlacklist.size();
                for (int i = 0; i < N; i++) {
                    int key = mWifiBlacklist.keyAt(i);
                    boolean value = mWifiBlacklist.valueAt(i);
                    NetworkRestriction n = new NetworkRestriction();
                    n.setUid(key);
                    n.setRestrictPolicy(value ? POLICY_REJECT_ON_WIFI : POLICY_REJECT_NONE);

                    restrictionList.add(n);
                }

                list.setRestrictionList(restrictionList);
                String json = list.toJson();

                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("writeWifiRestrictionBlackList, js: " + json);

                File dataDir = Environment.getDataDirectory();
                File systemDir = new File(dataDir, "system/tor/wifi_restrict/");
                File blacklistFile = new File(systemDir, "blacklist2");

                FileUtil.writeString(json, blacklistFile.getPath());
            } catch (Throwable e) {
                XposedLog.wtf("Fail writeDataRestrictionBlackList: " + Log.getStackTraceString(e));
            }
        }
    }

    @Override
    @InternalCall
    @Deprecated
    public void onNetWorkManagementServiceReady(NativeDaemonConnector connector) {
        XposedLog.debug("NMS onNetWorkManagementServiceReady: " + connector);
        this.mNativeDaemonConnector = connector;
        this.mWifiInterfaceName = SystemProperties.get("wifi.interface");
        XposedLog.debug("NMS mWifiInterfaceName: " + mWifiInterfaceName);

        initDataInterface();

        initDataRestrictionBlackList();
        initWifiRestrictionBlackList();

        applyRestrictionBlackList();

        // Note: processPendingDataRestrictRequests() will unregister
        // mPendingDataRestrictReceiver once it has been able to determine
        // the cellular network interface name.
        mPendingDataRestrictReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processPendingDataRestrictRequests();
            }
        };
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getContext().registerReceiver(mPendingDataRestrictReceiver, filter);
    }

    @Override
    @InternalCall
    public void onRequestAudioFocus(int type, int res, int callingUid, String callingPkg) {
        String pkgName = mPackagesCache.get(callingUid);
        if (pkgName == null) return;
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onRequestAudioFocus: " + pkgName + " ,uid: " + callingUid
                    + ", type: " + type + ", res: " + res);
        }

        if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            return;
        }

        // Only protect music, movie, speech.
        if (type != AudioAttributes.CONTENT_TYPE_MOVIE
                && type != AudioAttributes.CONTENT_TYPE_MUSIC
                && type != AudioAttributes.CONTENT_TYPE_SPEECH) {
            return;
        }

        h.obtainMessage(AshManHandlerMessages.MSG_ONAUDIOFOCUSEDPACKAGECHANGED, pkgName).sendToTarget();
    }

    @Override
    @InternalCall
    public void onAbandonAudioFocus(int res, int callingUid, String callingPkg) {
        if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            return;
        }
        String pkgName = mPackagesCache.get(callingUid);
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onAbandonAudioFocus: " + callingPkg + "--" + callingUid);
        }
        if (pkgName == null) return;
        h.obtainMessage(AshManHandlerMessages.MSG_ONAUDIOFOCUSEDPACKAGEABANDONED, pkgName).sendToTarget();
    }

    private void processPendingDataRestrictRequests() {
        initDataInterface();
        if (TextUtils.isEmpty(mDataInterfaceName)) {
            return;
        }
        if (mPendingDataRestrictReceiver != null) {
            getContext().unregisterReceiver(mPendingDataRestrictReceiver);
            mPendingDataRestrictReceiver = null;
        }
        int count = mPendingRestrictOnData.size();
        for (int i = 0; i < count; i++) {
            restrictAppOnData(mPendingRestrictOnData.keyAt(i),
                    mPendingRestrictOnData.valueAt(i));
        }
        mPendingRestrictOnData.clear();
    }

    private void initDataInterface() {
        XposedLog.debug("NMS mDataInterfaceName: " + mDataInterfaceName);
        if (!TextUtils.isEmpty(mDataInterfaceName)) {
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        LinkProperties linkProperties = cm.getLinkProperties(ConnectivityManager.TYPE_MOBILE);
        if (linkProperties != null) {
            mDataInterfaceName = linkProperties.getInterfaceName();
        }
        XposedLog.debug("NMS mDataInterfaceName: " + mDataInterfaceName);
    }


    @Override
    @BinderCall
    public void restrictAppOnData(int uid, boolean restrict) {
        XposedLog.debug("NMS restrictAppOnData: " + uid + ", restrict: " + restrict);
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_RESTRICTAPPONDATA, uid, -1, restrict)
                .sendToTarget();
    }

    private void restrictAppOnDataForce(int uid, boolean restrict) {
        XposedLog.debug("NMS restrictAppOnDataForce: " + uid + ", restrict: " + restrict);
        h.obtainMessage(AshManHandlerMessages.MSG_RESTRICTAPPONDATA, uid, 1, restrict)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public void restrictAppOnWifi(int uid, boolean restrict) {
        XposedLog.debug("NMS restrictAppOnWifi: " + uid + ", restrict: " + restrict);
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_RESTRICTAPPONWIFI, uid, -1, restrict)
                .sendToTarget();
    }

    private void restrictAppOnWifiForce(int uid, boolean restrict) {
        XposedLog.debug("NMS restrictAppOnWifiForce: " + uid + ", restrict: " + restrict);
        h.obtainMessage(AshManHandlerMessages.MSG_RESTRICTAPPONWIFI, uid, 1, restrict)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isRestrictOnData(int uid) throws RemoteException {
        enforceCallingPermissions();
        return mDataBlacklist.get(uid);
    }

    @Override
    @BinderCall
    public boolean isRestrictOnWifi(int uid) throws RemoteException {
        enforceCallingPermissions();
        return mWifiBlacklist.get(uid);
    }


    // NMS API END.

    @Override
    public void retrieveSettings() {
        XposedLog.wtf("retrieveSettings@" + getClass().getSimpleName());
        getConfigFromSettings();
        cachePackages();
        whiteIMEPackages();
    }

    private void construct() {
        h = onCreateServiceHandler();
        lazyH = onCreateLazyHandler();
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("construct, h: " + h
                + ", lazyH: " + lazyH
                + " -" + serial());
    }

    protected Handler onCreateServiceHandler() {
        return new HandlerImpl();
    }

    protected Handler onCreateLazyHandler() {
        HandlerThread hr = new HandlerThread("ASHMAN-LAZY-H");
        hr.start();
        return new LazyHandler(hr.getLooper());
    }

    @Override
    public void publishFeature(String f) {

    }

    @Override
    public void shutdown() {
    }


    // For debug.
    private Toast mDebugToast;
    private ComponentName mFocusedCompName;

    private Runnable toastRunnable = new Runnable() {
        @Override
        public void run() {
            ComponentName c = mFocusedCompName;
            if (c != null) {
                try {
                    if (mDebugToast != null) {
                        mDebugToast.cancel();
                    }
                    mDebugToast = Toast.makeText(getContext(),
                            "应用管理开发者模式：\n" +
                                    c.flattenToString(), Toast.LENGTH_LONG);
                    mDebugToast.show();
                } catch (Throwable ignored) {
                }
            }
        }
    };

    @Override
    @InternalCall
    public void onPackageMoveToFront(final Intent who) {
        onPackageMoveToFront(PkgUtil.packageNameOf(who));

        if (XposedLog.isVerboseLoggable()) {
            lazyH.removeCallbacks(toastRunnable);
            if (who != null) {
                mFocusedCompName = who.getComponent();
                lazyH.post(toastRunnable);
            }
        }
    }

    private void onPackageMoveToFront(String who) {
        if (who == null) return;
        lazyH.removeMessages(AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONT);
        lazyH.obtainMessage(AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONT, who).sendToTarget();
    }

    @Override
    public String serial() {
        return mSerialUUID.toString();
    }

    @Override
    @BinderCall
    public void clearProcess(IProcessClearListener listener) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_CLEARPROCESS, listener)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public void setLockKillDelay(long delay) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLDELAY, delay).sendToTarget();
    }

    @Override
    @BinderCall
    public long getLockKillDelay() throws RemoteException {
        enforceCallingPermissions();
        return mLockKillDelay;
    }

    @Override
    public void setWhiteSysAppEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETWHITESYSAPPENABLED, enabled)
                .sendToTarget();
    }

    @Override
    public boolean isWhiteSysAppEnabled() {
        enforceCallingPermissions();
        return mWhiteSysAppEnabled.get();
    }

    @Override
    @BinderCall
    public void setBootBlockEnabled(boolean enabled) {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETBOOTBLOCKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isBlockBlockEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mBootBlockEnabled.get();
    }

    @Override
    @BinderCall
    public void setStartBlockEnabled(boolean enabled) {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETSTARTBLOCKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isStartBlockEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mStartBlockEnabled.get();
    }

    @Override
    @BinderCall
    public void setLockKillEnabled(boolean enabled) {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isLockKillEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mLockKillEnabled.get();
    }

    @Override
    @BinderCall
    public void setRFKillEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        h.obtainMessage(AshManHandlerMessages.MSG_SETRFKILLENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isRFKillEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mRootActivityFinishKillEnabled.get();
    }

    @Override
    @BinderCall
    protected void dump(FileDescriptor fd, final PrintWriter fout, String[] args) {
        super.dump(fd, fout, args);
        // For secure and CTS.
        if (getContext().checkCallingOrSelfPermission(Manifest.permission.DUMP) != PackageManager.PERMISSION_GRANTED) {
            fout.println("Permission denial: can not dump Ashman service from pid= " + Binder.getCallingPid()
                    + ", uid= " + Binder.getCallingUid());
            return;
        }

        synchronized (this) {
            // Dump switch.
            fout.println("White system app enabled: " + mWhiteSysAppEnabled.get());
            fout.println("Start block enabled: " + mStartBlockEnabled.get());
            fout.println("Boot block enabled: " + mBootBlockEnabled.get());
            fout.println("LK enabled: " + mLockKillEnabled.get());
            fout.println("RF kill enabled: " + mRootActivityFinishKillEnabled.get());
            fout.println("CompSettingBlockEnabled enabled: " + mCompSettingBlockEnabled.get());
            fout.println("LK delay: " + mLockKillDelay);
            fout.println("Control mode: " + mControlMode.get());

            fout.println();
            fout.println("======================");
            fout.println();

            // Dump while list.
            fout.println("White list: ");
            Object[] whileListObjects = WHITE_LIST.toArray();
            Collections.consumeRemaining(whileListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            fout.println();
            fout.println("======================");
            fout.println();

            // Dump System list.
            fout.println("System list: ");
            Object[] systemListObjects = SYSTEM_APPS.toArray();
            Collections.consumeRemaining(systemListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            fout.println();
            fout.println("======================");
            fout.println();

            // Dump boot list.
            fout.println("Boot list: ");
            Object[] bootListObjects = mBootControlListPackages.values().toArray();
            Collections.consumeRemaining(bootListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            fout.println();
            fout.println("======================");
            fout.println();

            // Dump start list.
            fout.println("Start list: ");
            Object[] startListObjects = mStartControlListPackages.values().toArray();
            Collections.consumeRemaining(startListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            fout.println();
            fout.println("======================");
            fout.println();

            // Dump lk list.
            fout.println("LK list: ");
            Object[] lkListObjects = mLockKillControlListPackages.values().toArray();
            Collections.consumeRemaining(lkListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            fout.println();
            fout.println("======================");
            fout.println();

            // Dump rf list.
            fout.println("RF list: ");
            Object[] rfListObjects = mRFKillControlListPackages.values().toArray();
            Collections.consumeRemaining(rfListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });

            fout.println();
            fout.println("======================");
            fout.println();

            // Dump watcher.
            fout.println("Watcher list: ");
            Object[] watcherListObjects = mWatcherClients.toArray();
            Collections.consumeRemaining(watcherListObjects, new Consumer<Object>() {
                @Override
                public void accept(Object o) {
                    fout.println(o);
                }
            });
        }
    }

    @Override
    public void dump(FileDescriptor fd, String[] args) {
        super.dump(fd, args);
        enforceCallingPermissions();
    }

    protected void enforceCallingPermissions() {
        int callingUID = Binder.getCallingUid();
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("enforceCallingPermissions@uid:" + callingUID);
        if (callingUID == android.os.Process.myUid() || (sClientUID > 0 && sClientUID == callingUID)) {
            return;
        }
        throw new SecurityException("Package of uid:" + callingUID
                + ", does not require permission to interact with XIntentFirewallService");
    }

    private void addBlockRecord(BlockRecord2 blockRecord2) {
        synchronized (mBlockRecords) {
            mBlockRecords.put(blockRecord2.getPkgName(), blockRecord2);
        }
    }

    private BlockRecord2 getBlockRecord(String pkg) {
        synchronized (mBlockRecords) {
            return mBlockRecords.get(pkg);
        }
    }

    @SuppressLint("HandlerLeak")
    private class HandlerImpl extends Handler implements AshManHandler {

        private final Runnable clearProcessRunnable = new Runnable() {
            @Override
            public void run() {
                clearProcess(null);
            }
        };

        @Override
        public void handleMessage(Message msg) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("HandlerImpl handleMessage: " + AshManHandlerMessages.decodeMessage(msg.what));
            super.handleMessage(msg);
            switch (msg.what) {
                case AshManHandlerMessages.MSG_CLEARPROCESS:
                    IProcessClearListener listener = msg.obj == null ? null : (IProcessClearListener) msg.obj;
                    HandlerImpl.this.clearProcess(listener);
                    break;
                case AshManHandlerMessages.MSG_SETWHITESYSAPPENABLED:
                    HandlerImpl.this.setWhiteSysAppEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETBOOTBLOCKENABLED:
                    HandlerImpl.this.setBootBlockEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETSTARTBLOCKENABLED:
                    HandlerImpl.this.setStartBlockEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETLOCKKILLENABLED:
                    HandlerImpl.this.setLockKillEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETRFKILLENABLED:
                    HandlerImpl.this.setRFKillEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETCOMPSETTINGBLOCKENABLED:
                    HandlerImpl.this.setCompSettingBlockEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_ONSCREENOFF:
                    HandlerImpl.this.onScreenOff();
                    break;
                case AshManHandlerMessages.MSG_ONSCREENON:
                    HandlerImpl.this.onScreenOn();
                    break;
                case AshManHandlerMessages.MSG_SETLOCKKILLDELAY:
                    HandlerImpl.this.setLockKillDelay((Long) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_CLEARBLOCKRECORDS:
                    HandlerImpl.this.clearBlockRecords();
                    break;
                case AshManHandlerMessages.MSG_SETCOMPONENTENABLEDSETTING:
                    HandlerImpl.this.setComponentEnabledSetting((ComponentName) msg.obj, msg.arg1, msg.arg2);
                    break;
                case AshManHandlerMessages.MSG_SETAPPLICATIONENABLEDSETTING:
                    HandlerImpl.this.setApplicationEnabledSetting((String) msg.obj, msg.arg1, msg.arg2);
                    break;
                case AshManHandlerMessages.MSG_WATCH:
                    HandlerImpl.this.watch((WatcherClient) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_UNWATCH:
                    HandlerImpl.this.unWatch((WatcherClient) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_NOTIFYSTARTBLOCK:
                    HandlerImpl.this.notifyStartBlock((String) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETNETWORKPOLICYUIDPOLICY:
                    HandlerImpl.this.setNetworkPolicyUidPolicy(msg.arg1, msg.arg2);
                    break;
                case AshManHandlerMessages.MSG_RESTRICTAPPONDATA:
                    HandlerImpl.this.restrictAppOnData(msg.arg1, (Boolean) msg.obj, msg.arg2 == 1);
                    break;
                case AshManHandlerMessages.MSG_RESTRICTAPPONWIFI:
                    HandlerImpl.this.restrictAppOnWifi(msg.arg1, (Boolean) msg.obj, msg.arg2 == 1);
                    break;
                case AshManHandlerMessages.MSG_ONAUDIOFOCUSEDPACKAGECHANGED:
                    HandlerImpl.this.onAudioFocusedPackageChanged((String) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_ONAUDIOFOCUSEDPACKAGEABANDONED:
                    HandlerImpl.this.onAudioFocusedPackageAbandoned((String) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETLOCKKILLDONOTKILLAUDIOENABLED:
                    HandlerImpl.this.setLockKillDoNotKillAudioEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETCONTROLMODE:
                    HandlerImpl.this.setControlMode((Integer) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETAUTOADDBLACKENABLE:
                    HandlerImpl.this.setAutoAddBlackEnable((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_FORCERELOADPACKAGES:
                    HandlerImpl.this.forceReloadPackages();
                    break;
            }
        }

        @Override
        public void setWhiteSysAppEnabled(boolean enabled) {
            if (mWhiteSysAppEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.ASH_WHITE_SYS_APP_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setBootBlockEnabled(boolean enabled) {
            if (mBootBlockEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.BOOT_BLOCK_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setStartBlockEnabled(boolean enabled) {
            if (mStartBlockEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.START_BLOCK_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setLockKillEnabled(boolean enabled) {
            if (mLockKillEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.LOCK_KILL_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setAutoAddBlackEnable(boolean enabled) {
            if (mAutoAddToBlackListForNewApp.compareAndSet(!enabled, enabled)) {
                SystemSettings.AUTO_BLACK_FOR_NEW_INSTALLED_APP_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void forceReloadPackages() {
            mWorkingService.execute(new Runnable() {
                @Override
                public void run() {
                    cachePackages();
                }
            });
        }

        @Override
        public void setLockKillDoNotKillAudioEnabled(boolean enabled) {
            if (mLockKillDoNotKillAudioEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.LOCK_KILL_DONT_KILL_AUDIO_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setRFKillEnabled(boolean enabled) {
            if (mRootActivityFinishKillEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.ROOT_ACTIVITY_KILL_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setCompSettingBlockEnabled(boolean enabled) {
            if (mCompSettingBlockEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.COMP_SETTING_BLOCK_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void clearProcess(final IProcessClearListener listener) {
            boolean doNotCleatWhenInter = true;
            if (listener != null) try {
                doNotCleatWhenInter = listener.doNotClearWhenIntervative();
            } catch (RemoteException ignored) {

            }

            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("clearProcess!!! doNotCleatWhenInter: " + doNotCleatWhenInter);

            if (listener != null) try {
                listener.onPrepareClearing();
            } catch (RemoteException ignored) {

            }
            final boolean finalDoNotCleatWhenInter = doNotCleatWhenInter;
            FutureTask<String[]> futureTask = new FutureTask<>(new SignalCallable<String[]>() {

                @Override
                public String[] call() throws Exception {

                    PowerManager power = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                    ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
                    if (am == null) return null;
                    List<ActivityManager.RunningAppProcessInfo> processes =
                            am.getRunningAppProcesses();
                    int count = processes == null ? 0 : processes.size();

                    if (listener != null) try {
                        listener.onStartClearing(count);
                    } catch (RemoteException ignored) {

                    }

                    String[] cleared = new String[count];

                    for (int i = 0; i < count; i++) {

                        // Check if canceled.
                        if (power != null && (finalDoNotCleatWhenInter && power.isInteractive())) {
                            XposedLog.wtf("isInteractive, skip clearing");
                            return cleared;
                        }

                        ActivityManager.RunningAppProcessInfo runningAppProcessInfo = processes.get(i);
                        String runningPackageName = mPackagesCache.get(runningAppProcessInfo.uid);
                        if (runningPackageName == null) {
                            if (listener != null) try {
                                listener.onIgnoredPkg(null, "null");
                            } catch (RemoteException ignored) {

                            }
                            continue;
                        }

                        if (!isPackageLKByUser(runningPackageName)) {
                            if (XposedLog.isVerboseLoggable()) {
                                XposedLog.verbose("Won't kill app that not in user list: " + runningPackageName);
                            }
                            if (listener != null) try {
                                listener.onIgnoredPkg(null, "User let it go");
                            } catch (RemoteException ignored) {

                            }
                            continue;
                        }

                        if (isLockKillDoNotKillAudioEnabled()
                                && runningPackageName.equals(mAudioFocusedPackage.getData())) {
                            if (XposedLog.isVerboseLoggable()) {
                                XposedLog.verbose("Won't kill app with audio focus: " + runningPackageName);
                            }
                            if (listener != null) try {
                                listener.onIgnoredPkg(null, "Audio focused");
                            } catch (RemoteException ignored) {

                            }
                            continue;
                        }

                        // Check if we can control.
                        boolean whiteApp = isInWhiteList(runningPackageName)
                                || (isWhiteSysAppEnabled() && isInSystemAppList(runningPackageName));
                        if (whiteApp) {
                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "white-list");
                            } catch (RemoteException ignored) {

                            }
                            //if (XposedLog.isVerboseLoggable()) XposedLog.verbose("App is in white-listed, wont kill: " + runningPackageName);
                            continue;
                        }
                        if (PkgUtil.isAppRunningForeground(getContext(), runningPackageName)) {

                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "foreground-app");
                            } catch (RemoteException ignored) {

                            }

                            if (XposedLog.isVerboseLoggable())
                                XposedLog.verbose("App is in foreground, wont kill: " + runningPackageName);
                            continue;
                        }
                        if (PkgUtil.isHomeApp(getContext(), runningPackageName)) {
                            addToWhiteList(runningPackageName);
                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "home-app");
                            } catch (RemoteException ignored) {

                            }

                            if (XposedLog.isVerboseLoggable())
                                XposedLog.verbose("App is in isHomeApp, wont kill: " + runningPackageName);
                            continue;
                        }
                        if (PkgUtil.isDefaultSmsApp(getContext(), runningPackageName)) {
                            addToWhiteList(runningPackageName);
                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "sms-app");
                            } catch (RemoteException ignored) {

                            }

                            if (XposedLog.isVerboseLoggable())
                                XposedLog.verbose("App is in isDefaultSmsApp, wont kill: " + runningPackageName);
                            continue;
                        }

                        if (listener != null) try {
                            listener.onClearingPkg(runningPackageName);
                        } catch (RemoteException ignored) {

                        }

                        // Clearing using kill command.
                        if (power != null && (finalDoNotCleatWhenInter && power.isInteractive())) {
                            XposedLog.wtf("isInteractive, skip clearing");
                            return cleared;
                        }
                        PkgUtil.kill(getContext(), runningAppProcessInfo);

                        cleared[i] = runningPackageName;
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("Force stopped: " + runningPackageName);

                        if (listener != null) try {
                            listener.onClearedPkg(runningPackageName);
                        } catch (RemoteException ignored) {

                        }
                    }

                    if (listener != null) try {
                        listener.onAllCleared(cleared);
                    } catch (RemoteException ignored) {

                    }

                    return cleared;
                }
            });
            mWorkingService.execute(futureTask);
        }

        @Override
        public void clearBlockRecords() {
            Runnable clear = new Runnable() {
                @Override
                public void run() {
                    synchronized (mBlockRecords) {
                        mBlockRecords.clear();
                    }
                }
            };
            mWorkingService.execute(clear);
        }

        @Override
        public void setLockKillDelay(long delay) {
            mLockKillDelay = delay;
            SystemSettings.LOCK_KILL_DELAY_L.writeToSystemSettings(getContext(), delay);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("setLockKillDelay to: " + mLockKillDelay);
        }

        @Override
        public void onScreenOff() {
            if (isLockKillEnabled()) {
                removeCallbacks(clearProcessRunnable);
                postDelayed(clearProcessRunnable, mLockKillDelay);
            }
        }

        @Override
        public void onScreenOn() {
            cancelProcessClearing("SCREEN ON");
        }


        private Runnable writeDataRestrictionRunnable = new Runnable() {
            @Override
            public void run() {
                writeDataRestrictionBlackList();
            }
        };

        private Runnable writeWifiRestrictionRunnable = new Runnable() {
            @Override
            public void run() {
                writeWifiRestrictionBlackList();
            }
        };

        @Override
        public void restrictAppOnData(int uid, boolean restrict, boolean force) {
            initDataInterface();

            if (TextUtils.isEmpty(mDataInterfaceName)) {
                // We don't have an interface name since data is not active
                // yet, so queue up the request for when it comes up alive
                mPendingRestrictOnData.put(uid, restrict);
                return;
            }

            if (!force) synchronized (mQuotaLock) {
                boolean oldValue = mDataBlacklist.get(uid, false);
                if (oldValue == restrict) {
                    return;
                }
            }

            try {
                boolean success = BandwidthCommandCompat.restrictAppOnData(mNativeDaemonConnector,
                        uid, restrict, mDataInterfaceName);
                XposedLog.debug("NativeDaemonConnector execute success: " + success);

                synchronized (mQuotaLock) {
                    if (success) {
                        mDataBlacklist.put(uid, restrict);
                        mWorkingService.execute(writeDataRestrictionRunnable);
                    } else {
                        mDataBlacklist.delete(uid);
                    }
                }
            } catch (Exception e) {
                XposedLog.wtf("Fail restrictAppOnData: " + Log.getStackTraceString(e));
            }
        }

        @Override
        public void restrictAppOnWifi(int uid, boolean restrict, boolean force) {

            if (!force) synchronized (mQuotaLock) {
                boolean oldValue = mWifiBlacklist.get(uid, false);
                if (oldValue == restrict) {
                    return;
                }
            }

            try {
                boolean success = BandwidthCommandCompat.restrictAppOnWifi(mNativeDaemonConnector, uid,
                        restrict, mWifiInterfaceName);
                XposedLog.debug("NativeDaemonConnector execute success: " + success);

                synchronized (mQuotaLock) {
                    if (success) {
                        mWifiBlacklist.put(uid, restrict);
                        mWorkingService.execute(writeWifiRestrictionRunnable);
                    } else {
                        mWifiBlacklist.delete(uid);
                    }
                }

            } catch (Exception e) {
                XposedLog.wtf("Fail restrictAppOnWifi: " + Log.getStackTraceString(e));
            }
        }

        private void cancelProcessClearing(String why) {
            if (XposedLog.isVerboseLoggable()) XposedLog.verbose("cancelProcessClearing: " + why);
            removeCallbacks(clearProcessRunnable);
        }

        @Override
        public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
            PackageManager pm = getContext().getPackageManager();
            pm.setComponentEnabledSetting(componentName, newState, flags);
        }

        @Override
        public int getComponentEnabledSetting(ComponentName componentName) {
            PackageManager pm = getContext().getPackageManager();
            return pm.getComponentEnabledSetting(componentName);
        }

        @Override
        public void setApplicationEnabledSetting(String packageName, int newState, int flags) {
            PackageManager pm = getContext().getPackageManager();
            pm.setApplicationEnabledSetting(packageName, newState, flags);
        }

        @Override
        public int getApplicationEnabledSetting(String packageName) {
            PackageManager pm = getContext().getPackageManager();
            return pm.getApplicationEnabledSetting(packageName);
        }

        @Override
        public void watch(WatcherClient w) {
            if (!mWatcherClients.contains(w)) {
                mWatcherClients.add(w);
            }
        }

        @Override
        public void unWatch(WatcherClient w) {
            mWatcherClients.remove(w);
        }

        @Override
        public void notifyStartBlock(final String pkg) {
            if (pkg == null) return;
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Object[] objects = mWatcherClients.toArray();
                    Collections.consumeRemaining(objects, new Consumer<Object>() {
                        @Override
                        public void accept(Object o) {
                            WatcherClient w = (WatcherClient) o;
                            if (!w.isAlive()) {
                                return; // FIXME Try remove from set.
                            }
                            try {
                                w.getWatcher().onStartBlocked(pkg);
                            } catch (RemoteException ignored) {
                            }
                        }
                    });
                }
            };
            mWorkingService.execute(r);
        }

        @Override
        public void setNetworkPolicyUidPolicy(int uid, int policy) {
            NetworkPolicyManager.from(getContext()).setUidPolicy(uid, policy);
        }

        @Override
        public void onAudioFocusedPackageChanged(String who) {
            mAudioFocusedPackage.setData(who);
        }

        @Override
        public void onAudioFocusedPackageAbandoned(String who) {
            String current = mAudioFocusedPackage.getData();
            if (!TextUtils.isEmpty(current) && current.equals(who)) {
                mAudioFocusedPackage.setData(null);
            }
        }

        @Override
        public void setControlMode(int mode) {
//            mControlMode.set(mode);
//            SystemSettings.ASH_CONTROL_MODE_I.writeToSystemSettings(getContext(), mode);
        }
    }

    private class LazyHandler extends Handler implements AshManLZHandler {

        private final Holder<String> mTopPackage = new Holder<>();

        public LazyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void onActivityDestroy(Intent intent) {
            boolean isMainIntent = PkgUtil.isMainIntent(intent);

            final String packageName = PkgUtil.packageNameOf(intent);
            if (packageName == null) return;


            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onActivityDestroy, packageName: " + packageName
                        + ", isMainIntent: " + isMainIntent + ", topPkg: " + getTopPackage());

            if (!isPackageRFKillEnabled(packageName)) {
                if (XposedLog.isVerboseLoggable()) XposedLog.verbose("PackageRFKill not enabled");
                return;
            }

            boolean maybeRootActivityFinish = !packageName.equals(getTopPackage());

            if (maybeRootActivityFinish) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("Killing maybeRootActivityFinish: " + packageName);

                        if (packageName.equals(getTopPackage())) {
                            if (XposedLog.isVerboseLoggable())
                                XposedLog.verbose("Top package is now him, let it go~");
                            return;
                        }

                        PkgUtil.kill(getContext(), packageName);
                    }
                }, 666);
            }
        }

        @Override
        public void onPackageMoveToFront(String who) {
            mTopPackage.setData(who);
        }

        @Override
        public void onCompSetting(String pkg, boolean enable) {

        }

        private String getTopPackage() {
            return mTopPackage.getData();
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (XposedLog.isVerboseLoggable()) XposedLog.verbose("LazyHandler handle message: "
                    + AshManLZHandlerMessages.decodeMessage(msg.what));
            switch (msg.what) {
                case AshManLZHandlerMessages.MSG_ONACTIVITYDESTROY:
                    LazyHandler.this.onActivityDestroy((Intent) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONT:
                    LazyHandler.this.onPackageMoveToFront((String) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_ONCOMPSETTING:
                    LazyHandler.this.onCompSetting((String) msg.obj, msg.arg1 == 1);
                    break;
            }
        }
    }

    @Builder
    @Getter
    @ToString
    private static class ServiceEvent {
        private String pkg;
        private String service;
        private String why;
        private String appName;
        private long when;
        private boolean allowed;
    }

    @Builder
    @Getter
    @ToString
    private static class BootEvent {
        private String pkg;
        private String appName;
        private long when;
        private String why;
        private boolean allowed;
    }

    @Builder
    @Getter
    @ToString
    private static class BroadcastEvent {
        private int receiver;
        private int caller;
        private String appName;
        private String action;
        private String why;
        private long when;
        private boolean allowed;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    private static class CheckResult {
        // Allowed cases.
        public static final CheckResult SERVICE_CHECK_DISABLED = new CheckResult(true, "SERVICE_CHECK_DISABLED", false);
        public static final CheckResult BOOT_CHECK_DISABLED = new CheckResult(true, "BOOT_CHECK_DISABLED", false);
        public static final CheckResult BROADCAST_CHECK_DISABLED = new CheckResult(true, "BROADCAST_CHECK_DISABLED", false);
        public static final CheckResult SYSTEM_NOT_READY = new CheckResult(true, "SYSTEM_NOT_READY", true);

        public static final CheckResult WHITE_LISTED = new CheckResult(true, "WHITE_LISTED", false);
        public static final CheckResult SYSTEM_APP = new CheckResult(true, "SYSTEM_APP", false);

        public static final CheckResult HOME_APP = new CheckResult(true, "HOME_APP", true);
        public static final CheckResult LAUNCHER_APP = new CheckResult(true, "LAUNCHER_APP", true);
        public static final CheckResult SMS_APP = new CheckResult(true, "SMS_APP", true);

        public static final CheckResult APP_RUNNING = new CheckResult(true, "APP_RUNNING", true);
        public static final CheckResult SAME_CALLER = new CheckResult(true, "SAME_CALLER", true);

        public static final CheckResult BAD_ARGS = new CheckResult(true, "BAD_ARGS", false);
        public static final CheckResult USER_ALLOWED = new CheckResult(true, "USER_ALLOWED", true);
        public static final CheckResult USER_DENIED = new CheckResult(false, "USER_DENIED", true);

        // Denied cases.
        public static final CheckResult DENIED_GENERAL = new CheckResult(false, "DENIED_GENERAL", true);
        public static final CheckResult DENIED_USER_LIST_NOT_READY = new CheckResult(false, "DENIED_USER_LIST_NOT_READY", true);
        public static final CheckResult ALLOWED_GENERAL = new CheckResult(true, "ALLOWED_GENERAL", true);

        private boolean res;
        private String why;
        private boolean logRecommended;
    }

    @Getter
    @Setter
    private abstract class SignalCallable<V> implements Callable<V> {
        boolean canceled = false;
    }
}
