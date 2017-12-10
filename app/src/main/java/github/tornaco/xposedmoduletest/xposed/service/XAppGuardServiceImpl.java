package github.tornaco.xposedmoduletest.xposed.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.common.base.Preconditions;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.android.common.Holder;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAppGuardWatcher;
import github.tornaco.xposedmoduletest.bean.ComponentReplacement;
import github.tornaco.xposedmoduletest.bean.ComponentReplacementDaoUtil;
import github.tornaco.xposedmoduletest.bean.CongfigurationSetting;
import github.tornaco.xposedmoduletest.bean.CongfigurationSettingDaoUtil;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.bean.PackageInfoDaoUtil;
import github.tornaco.xposedmoduletest.provider.AppGuardPackageProvider;
import github.tornaco.xposedmoduletest.provider.ComponentsReplacementProvider;
import github.tornaco.xposedmoduletest.provider.ConfigurationSettingProvider;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppVerifyMode;
import github.tornaco.xposedmoduletest.xposed.bean.BlurSettings;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;
import github.tornaco.xposedmoduletest.xposed.service.provider.SystemSettings;
import github.tornaco.xposedmoduletest.xposed.submodules.AppGuardSubModuleManager;
import github.tornaco.xposedmoduletest.xposed.submodules.SubModule;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Synchronized;

import static android.content.Context.KEYGUARD_SERVICE;
import static github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager.Feature.FEATURE_COUNT;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */
class XAppGuardServiceImpl extends XAppGuardServiceAbs {

    private static final long TRANSACTION_EXPIRE_TIME = 60 * 1000;

    private Handler mServiceHandler;

    private AtomicBoolean mEnabled = new AtomicBoolean(false);
    private AtomicBoolean mUninstallProEnabled = new AtomicBoolean(false);
    private AtomicBoolean mDebugEnabled = new AtomicBoolean(false);

    private AtomicBoolean mInterruptFPSuccessVB = new AtomicBoolean(false);
    private AtomicBoolean mInterruptFPERRORVB = new AtomicBoolean(false);

    private BlurSettings mBlurSettings;
    private VerifySettings mVerifySettings;

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, Transaction> mTransactionMap = new HashMap<>();

    private final Map<String, CongfigurationSetting> mConfigSettings = new HashMap<>();

    private final Map<ComponentName, ComponentName> mComponentReplacementsMap = new HashMap<>();

    private final Set<String> mFeatures = new HashSet<>(FEATURE_COUNT);

    private static final Set<String> PREBUILT_WHITE_LIST = new HashSet<>();

    private final Map<String, PackageInfo> mGuardPackages = new HashMap<>();
    private final Set<String> mVerifiedPackages = new HashSet<>();
    private final Set<IAppGuardWatcher> mWatchers = new HashSet<>();

    private static int sClientUID = 0;

    private final Holder<String> mTopActivityPkg = new Holder<>();

    static {
        PREBUILT_WHITE_LIST.add("com.android.systemui");
        // PREBUILT_WHITE_LIST.add("com.android.packageinstaller");
        PREBUILT_WHITE_LIST.add("android");
        PREBUILT_WHITE_LIST.add("com.cyanogenmod.trebuchet");
        // It is good for user if our mod crash.
        // PREBUILT_WHITE_LIST.add("de.robv.android.xposed.installer");
        PREBUILT_WHITE_LIST.add(BuildConfig.APPLICATION_ID);
    }

    private final ExecutorService mWorkingService = Executors.newCachedThreadPool();

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
                case Intent.ACTION_PACKAGE_REPLACED:
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (packageName == null) return;
                    cacheUIDForPackages();
                    break;
            }
        }
    };

    private PackageManager mPackageManager;

    // Safe mode is the last clear place user can stay.
    private boolean mIsSafeMode = false;

    private UUID mSerialUUID = UUID.randomUUID();

    @Override
    public void publish() {
        try {
            XposedLog.debug("published by uid: " + Binder.getCallingUid());
            construct();
            ServiceManager.addService(XAppGuardManager.APP_GUARD_SERVICE, asBinder());
            publishFeature(XAppGuardManager.Feature.BASE);
        } catch (Exception e) {
            XposedLog.debug("*** FATAL*** Fail publish our svc:" + e);
        }
    }

    private void checkSafeMode() {
        mIsSafeMode = getContext().getPackageManager().isSafeMode();
    }

    private void construct() {
        mServiceHandler = onCreateServiceHandler();
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("construct, mServiceHandler: " + mServiceHandler + " -" + serial());
        mPackageManager = getContext().getPackageManager();
    }

    protected Handler onCreateServiceHandler() {
        return new AppGuardServiceHandlerImpl();
    }

    @Override
    public void systemReady() {
        XposedLog.wtf("systemReady@" + getClass().getSimpleName());
        checkSafeMode();
        cacheUIDForPackages();

        // Try read providers.
        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = readPackageProvider();
                String extra = res.getExtra();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("readPackageProvider, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerPackageObserver();
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("registerPackageObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadComponentReplacements();
                String extra = res.getExtra();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("loadComponentReplacements, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerComponentReplacementsObserver();
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("registerComponentReplacementsObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
            @Override
            public boolean once() {
                ValueExtra<Boolean, String> res = loadConfigurationSettings();
                String extra = res.getExtra();
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("loadConfigurationSettings, extra: " + extra);
                return res.getValue();
            }
        }, new Runnable() {
            @Override
            public void run() {
                AsyncTrying.tryTillSuccess(mWorkingService, new AsyncTrying.Once() {
                    @Override
                    public boolean once() {
                        ValueExtra<Boolean, String> res = registerConfigSettingsObserver();
                        if (XposedLog.isVerboseLoggable())
                            XposedLog.verbose("registerConfigSettingsObserver, extra: " + res.getExtra());
                        return res.getValue();
                    }
                });
            }
        });

        registerReceiver();
    }

    @Override
    public void retrieveSettings() {
        XposedLog.wtf("retrieveSettings@" + getClass().getSimpleName());
        getConfigFromSettings();
    }

    private void getConfigFromSettings() {
        try {
            boolean appGuardEnabled = (boolean) SystemSettings.APP_GUARD_ENABLED_B.readFromSystemSettings(getContext());
            mEnabled.set(appGuardEnabled);

            boolean uninstallProEnabled = (boolean) SystemSettings.UNINSTALL_GUARD_ENABLED_B.readFromSystemSettings(getContext());
            mUninstallProEnabled.set(uninstallProEnabled);

            boolean interruptFPS = (boolean) SystemSettings.INTERRUPT_FP_SUCCESS_VB_ENABLED_B.readFromSystemSettings(getContext());
            mInterruptFPSuccessVB.set(interruptFPS);

            boolean interruptFPE = (boolean) SystemSettings.INTERRUPT_FP_ERROR_VB_ENABLED_B.readFromSystemSettings(getContext());
            mInterruptFPERRORVB.set(interruptFPE);

            // FIXME Why?
            boolean debug = BuildConfig.DEBUG;
            mDebugEnabled.set(debug);
            XposedLog.setLogLevel(debug ? XposedLog.LogLevel.ALL : XposedLog.LogLevel.WARN);

            ContentResolver resolver = getContext().getContentResolver();
            if (resolver == null) return;
            mBlurSettings = BlurSettings.from(Settings.System.getString(resolver, BlurSettings.KEY_SETTINGS));
            mVerifySettings = VerifySettings.from(Settings.System.getString(resolver, VerifySettings.KEY_SETTINGS));

            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mBlurSettings: " + String.valueOf(mBlurSettings));
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mVerifySettings: " + String.valueOf(mVerifySettings));
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mUninstallProEnabled: " + String.valueOf(mUninstallProEnabled));
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mInterruptFPSuccessVB: " + String.valueOf(mInterruptFPSuccessVB));
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("mEnabled: " + String.valueOf(mEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail getConfigFromSettings:" + Log.getStackTraceString(e));
        }
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

    synchronized private ValueExtra<Boolean, String> readPackageProvider() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(AppGuardPackageProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            mGuardPackages.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                PackageInfo packageInfo = PackageInfoDaoUtil.readEntity(cursor, 0);
                String key = packageInfo.getPkgName();
                if (TextUtils.isEmpty(key)) continue;
                mGuardPackages.put(key, packageInfo);
            }
        } catch (Throwable e) {
            return new ValueExtra<>(false, String.valueOf(e));
        } finally {
            Closer.closeQuietly(cursor);
        }
        return new ValueExtra<>(true, String.valueOf("Read count: " + mGuardPackages.size()));
    }

    private ValueExtra<Boolean, String> registerPackageObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(AppGuardPackageProvider.CONTENT_URI,
                    false, new ContentObserver(mServiceHandler) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    readPackageProvider();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XposedLog.wtf("Fail registerContentObserver@AppGuardPackageProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }

    synchronized private ValueExtra<Boolean, String> loadComponentReplacements() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ComponentsReplacementProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            mComponentReplacementsMap.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {

                ComponentReplacement componentReplacement = ComponentReplacementDaoUtil.readEntity(cursor, 0);

                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("Lock componentReplacements readEntity of: " + componentReplacement);

                try {
                    ComponentName key = ComponentName.unflattenFromString(componentReplacement.fromFlattenToString());
                    if (key == null) continue;
                    ComponentName value = ComponentName.unflattenFromString(componentReplacement.toFlattenToString());

                    if (XposedLog.isVerboseLoggable()) {
                        XposedLog.verbose("Put replacement: " + value);
                    }
                    mComponentReplacementsMap.put(key, value);
                } catch (Throwable e) {
                    XposedLog.wtf("Fail load comp entry:\n" + Log.getStackTraceString(e));
                }
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadComponentReplacements:\n" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(cursor);
        }

        return new ValueExtra<>(true, "Read count: " + mComponentReplacementsMap.size());
    }

    private ValueExtra<Boolean, String> registerComponentReplacementsObserver() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(ComponentsReplacementProvider.CONTENT_URI,
                    false, new ContentObserver(mServiceHandler) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadComponentReplacements();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XposedLog.wtf("Fail registerContentObserver@ComponentsReplacementProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }

    synchronized private ValueExtra<Boolean, String> loadConfigurationSettings() {
        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(ConfigurationSettingProvider.CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                return new ValueExtra<>(false, "cursor is null");
            }

            mConfigSettings.clear();

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                CongfigurationSetting setting = CongfigurationSettingDaoUtil.readEntity(cursor, 0);

                if (XposedLog.isVerboseLoggable()) {
                    XposedLog.verbose("read entry of CongfigurationSetting: " + setting);
                }

                if (setting.getPackageName() == null) continue;

                // Add to map.
                mConfigSettings.put(setting.getPackageName(), setting);
            }
        } catch (Throwable e) {
            return new ValueExtra<>(false, String.valueOf(e));
        } finally {
            Closer.closeQuietly(cursor);
        }
        return new ValueExtra<>(true, String.valueOf("Read count: " + mConfigSettings.size()));
    }

    private ValueExtra<Boolean, String> registerConfigSettingsObserver() {

        ContentResolver contentResolver = getContext().getContentResolver();
        if (contentResolver == null) {
            // Happen when early start.
            return new ValueExtra<>(false, "contentResolver is null");
        }
        try {
            contentResolver.registerContentObserver(ConfigurationSettingProvider.CONTENT_URI,
                    false, new ContentObserver(mServiceHandler) {
                        @Override
                        public void onChange(boolean selfChange, Uri uri) {
                            super.onChange(selfChange, uri);
                            mWorkingService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    loadConfigurationSettings();
                                }
                            });
                        }
                    });
        } catch (Exception e) {
            XposedLog.wtf("Fail registerContentObserver@ConfigurationSettingProvider:\n" + Log.getStackTraceString(e));
            return new ValueExtra<>(false, String.valueOf(e));
        }
        return new ValueExtra<>(true, "OK");
    }

    private void cacheUIDForPackages() {
        PackageManager pm = this.getContext().getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            sClientUID = applicationInfo.uid;
        } catch (Exception ignored) {
            XposedLog.debug("Can not getSingleton UID for our client:" + ignored);
        }
    }

    @Override
    @Synchronized
    public void publishFeature(String f) {
        if (!mFeatures.contains(f)) {
            mFeatures.add(f);
        }
    }

    @Override
    public void shutdown() {
        XposedLog.debug("shutdown...");
    }

    @Override
    public boolean interruptPackageRemoval(String pkg) {
        boolean enabled = isUninstallInterruptEnabled();
        if (!enabled) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("interruptPackageRemoval false: not enabled.");
            return false;
        }
        boolean confirm = onInterruptConfirm(pkg);
        if (!confirm) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("interruptPackageRemoval false: not confirmed.");
            return false;
        }
        return true;
    }

    @Override
    @InternalCall
    public Intent checkIntent(Intent from) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("checkIntent: " + from);
        ComponentName fromComp = from.getComponent();
        if (fromComp == null) return from;

        if (!mComponentReplacementsMap.containsKey(fromComp)) {
            return from;
        }

        ComponentName toComp = mComponentReplacementsMap.get(fromComp);

        if (!validateComponentName(toComp)) {
            XposedLog.debug("Invalid component replacement: " + toComp);
            return from;
        }

        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("Replacing using: " + toComp);
        if (toComp == null) return null;

        return from.setComponent(toComp);
    }

    @Override
    public long wrapCallingUidForIntent(long from, Intent intent) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("wrapCallingUidForIntent for : " + from);
        }
        return from;
    }

    private static boolean validateComponentName(ComponentName componentName) {
        return true;
    }

    @Override
    @BinderCall
    public void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add)
            throws RemoteException {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("addOrRemoveComponentReplacement, from: "
                    + from + ", to: " + to + ", add? " + add);
        }

        enforceCallingPermissions();
        Preconditions.checkNotNull(from);

        Object[] data = new Object[3];
        data[0] = from;
        data[1] = to;
        data[2] = add;

        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ADDORREMOVECOMPONENTREPLACEMENT
                , data).sendToTarget();

        // Insert into db.
        ComponentReplacement replacement = new ComponentReplacement();
        replacement.setAppPackageName(from.getPackageName());

        replacement.setCompFromClassName(from.getClassName());
        replacement.setCompFromPackageName(from.getPackageName());

        replacement.setCompToClassName(to == null ? null : to.getClassName());
        replacement.setCompToPackageName(to == null ? null : to.getPackageName());

        if (add) {
            ComponentsReplacementProvider.insertOrUpdate(getContext(), replacement);
        } else {
            ComponentsReplacementProvider.delete(getContext(), replacement);
        }
    }

    @Override
    protected void dump(FileDescriptor fd, final PrintWriter fout, String[] args) {
        super.dump(fd, fout, args);
        // For secure and CTS.
        if (getContext().checkCallingOrSelfPermission(Manifest.permission.DUMP) != PackageManager.PERMISSION_GRANTED) {
            fout.println("Permission denial: can not dump AppGuard service from pid= " + Binder.getCallingPid()
                    + ", uid= " + Binder.getCallingUid());
            return;
        }
        synchronized (this) {
            fout.println("mInterruptFPSuccessVB enabled: " + mInterruptFPSuccessVB.get());
            fout.println("mInterruptFPERRORVB enabled: " + mInterruptFPERRORVB.get());

            fout.println();
            fout.println();

            Collections.consumeRemaining(mConfigSettings.values(), new Consumer<CongfigurationSetting>() {
                @Override
                public void accept(CongfigurationSetting congfigurationSetting) {
                    fout.println("config setting: " + congfigurationSetting);
                }
            });
        }
    }

    public PackageManager getPackageManager() {
        if (mPackageManager == null) mPackageManager = getContext().getPackageManager();
        return mPackageManager;
    }

    private boolean onInterruptConfirm(String pkg) {
        if (pkg == null) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@pkg-null");
            return false;
        }
        if (mIsSafeMode) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@safe-mode:" + pkg);
            return false;
        }
        if (!mEnabled.get()) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@disabled:" + pkg);
            return false;
        }
        if (PREBUILT_WHITE_LIST.contains(pkg)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@prebuilt-w-list:" + pkg);
            return false;
        } // White list.

        PackageInfo p = mGuardPackages.get(pkg);
        if (p == null) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@not-in-guard-list:" + pkg);
            return false;
        }
        if (!p.getGuard()) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@not-in-guard-value-list:" + pkg);
            return false;
        }

        return true;
    }

    @Override
    public boolean onEarlyVerifyConfirm(String pkg) {
        if (pkg == null) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@pkg-null");
            return false;
        }
        if (mIsSafeMode) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@safe-mode:" + pkg);
            return false;
        }
        if (!mEnabled.get()) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@disabled:" + pkg);
            return false;
        }
        if (PREBUILT_WHITE_LIST.contains(pkg)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@prebuilt-w-list:" + pkg);
            return false;
        } // White list.

        if (mVerifiedPackages.contains(pkg)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@verified-list:" + pkg);
            return false;
        } // Passed.
        PackageInfo p = mGuardPackages.get(pkg);
        if (p == null) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@not-in-guard-list:" + pkg);
            return false;
        }
        if (!p.getGuard()) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("onEarlyVerifyConfirm, false@not-in-guard-value-list:" + pkg);
            return false;
        }

        return true;
    }

    @Override
    @InternalCall
    public void verify(Bundle options, String pkg, int uid, int pid, VerifyListener listener) {
        verifyInternal(options, pkg, uid, pid, false, listener);
    }

    private void verifyInternal(Bundle options, String pkg, int uid, int pid,
                                boolean injectHomeOnFail, VerifyListener listener) {
        if (mServiceHandler == null) {
            XposedLog.wtf("WTF? AppGuardServiceHandler is null?");
            return;
        }
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("verifyInternal: " + pkg);
        VerifyArgs args = VerifyArgs.builder()
                .bnds(options)
                .pid(pid)
                .uid(uid)
                .injectHomeOnFail(injectHomeOnFail)
                .listener(listener)
                .pkg(pkg)
                .build();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_VERIFY, args).sendToTarget();
    }


    @Override
    @BinderCall(restrict = "hooks")
    public void onPackageMoveToFront(Intent who) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("onPackageMoveToFront: " + who);
        onActivityPackageResume(PkgUtil.packageNameOf(who));
    }

    @Override
    @BinderCall(restrict = "anyone")
    public void onActivityPackageResume(String pkg) {
        if (mServiceHandler == null) return;
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONACTIVITYPACKAGERESUME, pkg).sendToTarget();
    }

    @Override
    public String[] getSubModules() throws RemoteException {
        enforceCallingPermissions();
        long id = Binder.clearCallingIdentity();
        try {
            Object[] modules = AppGuardSubModuleManager.getInstance().getAllSubModules().toArray();
            final String[] tokens = new String[modules.length];
            for (int i = 0; i < modules.length; i++) {
                SubModule subModule = (SubModule) modules[i];
                tokens[i] = String.valueOf(subModule.name());
            }
            return tokens;
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public int getSubModuleStatus(final String token) throws RemoteException {
        enforceCallingPermissions();
        Preconditions.checkNotNull(token);
        long id = Binder.clearCallingIdentity();
        final Holder<Integer> status = new Holder<>();
        status.setData(SubModule.SubModuleStatus.ERROR.ordinal());
        try {
            Collections.consumeRemaining(AppGuardSubModuleManager.getInstance().getAllSubModules(),
                    new Consumer<SubModule>() {
                        @Override
                        public void accept(SubModule subModule) {
                            if (token.equals(subModule.name())) {
                                status.setData(subModule.getStatus().ordinal());
                            }
                        }
                    });
        } finally {
            Binder.restoreCallingIdentity(id);
        }
        return status.getData();
    }

    @Override
    public boolean isInterruptFPEventVBEnabled(int event) throws RemoteException {
        enforceCallingPermissions();
        switch (event) {
            case XAppGuardManager.FPEvent.SUCCESS:
                return interruptFPSuccessVibrate();
            case XAppGuardManager.FPEvent.ERROR:
                return interruptFPErrorVibrate();
        }
        return false;
    }

    private boolean isDeviceLocked() {
        KeyguardManager keyguardManager = (KeyguardManager)
                getContext()
                        .getSystemService(KEYGUARD_SERVICE);
        return keyguardManager != null && keyguardManager.isKeyguardLocked();
    }

    @Override
    @BinderCall
    public void setInterruptFPEventVBEnabled(int event, boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages
                .MSG_SETINTERRUPTFPEVENTVBENABLED, event, event, enabled)
                .sendToTarget();
    }

    @Override
    public String serial() {
        return mSerialUUID.toString();
    }

    @Override
    public boolean isBlurForPkg(String pkg) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("isBlurForPkg? " + mBlurSettings);
        if (mBlurSettings == null || !mBlurSettings.isEnabled()) return false;
        int policy = mBlurSettings.getPolicy();
        switch (policy) {
            case XAppGuardManager.BlurPolicy.BLUR_ALL:
                return true;
            case XAppGuardManager.BlurPolicy.BLUR_POLICY_UNKNOWN:
                return false;
            case XAppGuardManager.BlurPolicy.BLUR_WATCHED:
                return mGuardPackages.containsKey(pkg);
            default:
                return false;
        }
    }

    @Override
    @InternalCall
    public float getBlurScale() {
        if (mBlurSettings == null) {
            return BlurSettings.BITMAP_SCALE;
        }
        if (mBlurSettings.getScale() <= 0) {
            mBlurSettings.setScale(BlurSettings.BITMAP_SCALE);
            return BlurSettings.BITMAP_SCALE;
        }
        return mBlurSettings.getScale();
    }

    @Override
    @InternalCall
    public int getBlurRadius() {
        if (mBlurSettings == null) {
            return BlurSettings.BLUR_RADIUS;
        }
        if (mBlurSettings.getRadius() <= 0
                || mBlurSettings.getRadius() > 25) {
            mBlurSettings.setRadius(BlurSettings.BLUR_RADIUS);
            return BlurSettings.BLUR_RADIUS;
        }
        return mBlurSettings.getRadius();
    }

    @Override
    public boolean interruptFPSuccessVibrate() {
        boolean isKeyguard = isDeviceLocked();
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("Device is locked: " + isKeyguard);
        return !isKeyguard && mInterruptFPSuccessVB.get();
    }

    @Override
    public boolean interruptFPErrorVibrate() {
        boolean isKeyguard = isDeviceLocked();
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("Device is locked: " + isKeyguard);
        return !isKeyguard && mInterruptFPERRORVB.get();
    }

    @Override
    public boolean isActivityStartShouldBeInterrupted(ComponentName componentName) {
        if (getContext() == null) return false;
        int state = getPackageManager().getComponentEnabledSetting(componentName);
        if (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
            return false;
        }
        // Comp is disabled, interrupt!!!
        return true;
    }

    @Override
    public void updateConfigurationForPackage(Configuration configuration, String packageName) {
        XAppGuardManager appGuardManager = XAppGuardManager.get();

        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("updateConfigurationForPackage: " + packageName + "-" + appGuardManager);
        }

        if (!appGuardManager.isServiceAvailable()) return;

        // Retrieve by binder call.
        CongfigurationSetting setting = appGuardManager.getConfigurationSetting(packageName);
        if (setting == null) return;

        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("apply config with: " + setting);
        }

        // Apply fields.
        if (setting.getFontScale() > 0) configuration.fontScale = setting.getFontScale();
        if (setting.getDensityDpi() > 0) configuration.densityDpi = setting.getDensityDpi();
    }

    @Override
    @BinderCall(restrict = "anyone")
    public CongfigurationSetting getConfigurationSetting(String packageName) {
        return mConfigSettings.get(packageName);
    }

    @Override
    @BinderCall
    public boolean isEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mEnabled.get();
    }

    @Override
    @BinderCall
    public void setEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETENABLED);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETENABLED,
                enabled ? 1 : 0, 0)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isUninstallInterruptEnabled() {
        enforceCallingPermissions();
        return isEnabled() && mUninstallProEnabled.get();
    }

    @Override
    @BinderCall
    public void setUninstallInterruptEnabled(boolean enabled) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED);
        mServiceHandler.obtainMessage(
                AppGuardServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED,
                enabled ? 1 : 0, 0)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public void setVerifySettings(VerifySettings settings) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETVERIFYSETTINGS);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETVERIFYSETTINGS, settings).sendToTarget();
    }

    @Override
    @BinderCall
    public VerifySettings getVerifySettings() throws RemoteException {
        enforceCallingPermissions();
        return mVerifySettings == null ? null : mVerifySettings.duplicate();
    }

    @Override
    @BinderCall
    public void setBlurSettings(BlurSettings settings) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.removeMessages(AppGuardServiceHandlerMessages.MSG_SETBLURSETTINGS);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETBLURSETTINGS, settings).sendToTarget();
    }

    @Override
    @BinderCall
    public BlurSettings getBlurSettings() throws RemoteException {
        enforceCallingPermissions();
        return mBlurSettings == null ? null : mBlurSettings.duplicate();
    }

    @Override
    @BinderCall
    public void setResult(int transactionID, int res) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETRESULT, transactionID, res).sendToTarget();
    }

    @Override
    public boolean isTransactionValid(int transactionID) {
        enforceCallingPermissions();
        Transaction transaction = mTransactionMap.get(transactionID);
        return transaction != null;
    }

    @Override
    @BinderCall
    public void watch(IAppGuardWatcher w) throws RemoteException {
        XposedLog.debug("iWatcher.watch-" + w);
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_WATCH, w).sendToTarget();
    }

    @Override
    @BinderCall
    public void unWatch(IAppGuardWatcher w) throws RemoteException {
        XposedLog.debug("iWatcher.unWatch-" + w);
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_UNWATCH, w).sendToTarget();
    }

    @Override
    @BinderCall
    public void mockCrash() throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.sendEmptyMessage(AppGuardServiceHandlerMessages.MSG_MOCKCRASH);
    }

    @Override
    @BinderCall
    public void setVerifierPackage(String pkg) throws RemoteException {
        enforceCallingPermissions();
        // TODO.
    }

    @Override
    @Deprecated
    public void injectHomeEvent() throws RemoteException {
        throw new IllegalStateException("injectHomeEvent is Deprecated api");
    }

    @Override
    public void setDebug(boolean debug) throws RemoteException {
        enforceCallingPermissions();
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_SETDEBUG, debug).sendToTarget();
    }

    @Override
    public boolean isDebug() throws RemoteException {
        enforceCallingPermissions();
        return mDebugEnabled.get();
    }

    protected void enforceCallingPermissions() {
        int callingUID = Binder.getCallingUid();
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("enforceCallingPermissions@uid:" + callingUID);
        if (callingUID == android.os.Process.myUid() || (sClientUID > 0 && sClientUID == callingUID)) {
            return;
        }
        throw new SecurityException("Package of uid:" + callingUID
                + ", does not require permission to interact with XAppGuardServiceImpl");
    }

    private void onScreenOff() {
        if (mVerifySettings != null) {
            boolean verifyOnScreenOff = mVerifySettings.isVerifyOnScreenOff();
            if (verifyOnScreenOff) {
                if (XposedLog.isVerboseLoggable())
                    XposedLog.verbose("SCREEN OFF, Clearing passed pkgs...");
                mVerifiedPackages.clear();
            }
        }
    }

    private void onUserPresent() {
        mServiceHandler.obtainMessage(AppGuardServiceHandlerMessages.MSG_ONUSERPRESENT).sendToTarget();
    }

    private static Intent buildVerifyIntent(boolean injectHome, int transId, String pkg) {
        Intent intent = new Intent(XAppGuardManager.ACTION_APP_GUARD_VERIFY_DISPLAYER);
        intent.setClassName(BuildConfig.APPLICATION_ID,
                "github.tornaco.xposedmoduletest.ui.activity.ag.VerifyDisplayerActivity");
        intent.putExtra(XAppGuardManager.EXTRA_PKG_NAME, pkg);
        intent.putExtra(XAppGuardManager.EXTRA_TRANS_ID, transId);
        intent.putExtra(XAppGuardManager.EXTRA_INJECT_HOME_WHEN_FAIL_ID, injectHome);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private static class TransactionFactory {

        private static final AtomicInteger TRANS_ID_BASE = new AtomicInteger(2017);

        static int transactionID() {
            return TRANS_ID_BASE.getAndIncrement();
        }
    }

    @SuppressLint("HandlerLeak")
    private class AppGuardServiceHandlerImpl extends Handler
            implements AppGuardServiceHandler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int wht = msg.what;
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("handleMessage@" + AppGuardServiceHandlerMessages.decodeMessage(wht));
            switch (wht) {
                case AppGuardServiceHandlerMessages.MSG_SETENABLED:
                    AppGuardServiceHandlerImpl.this.setEnabled(msg.arg1 == 1);
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETBLURSETTINGS:
                    AppGuardServiceHandlerImpl.this.setBlurSettings((BlurSettings) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_MOCKCRASH:
                    AppGuardServiceHandlerImpl.this.mockCrash();
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETRESULT:
                    AppGuardServiceHandlerImpl.this.setResult(msg.arg1, msg.arg2);
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETUNINSTALLINTERRUPTENABLED:
                    AppGuardServiceHandlerImpl.this.setUninstallInterruptEnabled(msg.arg1 == 1);
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETVERIFYSETTINGS:
                    AppGuardServiceHandlerImpl.this.setVerifySettings((VerifySettings) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_UNWATCH:
                    AppGuardServiceHandlerImpl.this.unWatch((IAppGuardWatcher) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_WATCH:
                    AppGuardServiceHandlerImpl.this.watch((IAppGuardWatcher) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_VERIFY:
                    AppGuardServiceHandlerImpl.this.verify((VerifyArgs) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETDEBUG:
                    AppGuardServiceHandlerImpl.this.setDebug((Boolean) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_ONACTIVITYPACKAGERESUME:
                    AppGuardServiceHandlerImpl.this.onActivityPackageResume((String) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_ONUSERPRESENT:
                    AppGuardServiceHandlerImpl.this.onUserPresent();
                    break;
                case AppGuardServiceHandlerMessages.MSG_SETINTERRUPTFPEVENTVBENABLED:
                    AppGuardServiceHandlerImpl.this.setInterruptFPEventVBEnabled(msg.arg1, (Boolean) msg.obj);
                    break;
                case AppGuardServiceHandlerMessages.MSG_ADDORREMOVECOMPONENTREPLACEMENT:
                    Object[] data = (Object[]) msg.obj;
                    AppGuardServiceHandlerImpl.this.addOrRemoveComponentReplacement((ComponentName) data[0],
                            (ComponentName) data[1], (Boolean) data[2]);
                    break;
                default:
                    AppGuardServiceHandlerImpl.this.setResult((Integer) msg.obj,
                            XAppVerifyMode.MODE_IGNORED);
                    break;
            }
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (mEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APP_GUARD_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setUninstallInterruptEnabled(boolean enabled) {
            if (mUninstallProEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.UNINSTALL_GUARD_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setVerifySettings(final VerifySettings settings) {
            mVerifySettings = Preconditions.checkNotNull(settings);
            mWorkingService.execute(new Runnable() {
                @Override
                public void run() {
                    // Saving to db.
                    ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putString(resolver, VerifySettings.KEY_SETTINGS, settings.formatJson());
                }
            });
        }


        @Override
        public void setBlurSettings(final BlurSettings settings) {
            mBlurSettings = Preconditions.checkNotNull(settings);
            mWorkingService.execute(new Runnable() {
                @Override
                public void run() {
                    // Saving to db.
                    ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putString(resolver, BlurSettings.KEY_SETTINGS, settings.formatJson());
                }
            });
        }


        @Override
        public void setResult(int transactionID, int res) {
            Transaction transaction = mTransactionMap.remove(transactionID);
            if (transaction == null) {
                XposedLog.debug("Can not find transaction for:" + transactionID);
                return;
            }
            if (res == XAppVerifyMode.MODE_ALLOWED) {
                mVerifiedPackages.add(transaction.pkg);
            }
            transaction.listener.onVerifyRes(transaction.pkg, transaction.uid, transaction.pid, res);
            mServiceHandler.removeMessages(MSG_TRANSACTION_EXPIRE_BASE + transactionID);
        }

        @Override
        public void verify(VerifyArgs args) {
            XposedLog.debug("onVerify:" + args);
            int tid = TransactionFactory.transactionID();
            int uid = args.uid;
            int pid = args.pid;
            String pkg = args.pkg;
            Bundle bnds = args.bnds;
            boolean injectHome = args.injectHomeOnFail;
            VerifyListener listener = args.listener;

            Transaction transaction = new Transaction(listener, uid, pid, tid, pkg);

            synchronized (mTransactionMap) {
                mTransactionMap.put(tid, transaction);
            }

            onNewTransaction(tid);

            Intent intent = buildVerifyIntent(injectHome, tid, pkg);
            try {
                getContext().startActivity(intent, bnds);
            } catch (ActivityNotFoundException anf) {
                XposedLog.debug("*** FATAL ERROR *** ActivityNotFoundException!!!");
                setResult(tid, XAppVerifyMode.MODE_ALLOWED);
            }
        }

        private void onNewTransaction(int transaction) {
            sendMessageDelayed(obtainMessage(MSG_TRANSACTION_EXPIRE_BASE + transaction, transaction), TRANSACTION_EXPIRE_TIME);
        }

        @Override
        public void watch(IAppGuardWatcher w) {
            Preconditions.checkNotNull(w);
            synchronized (mWatchers) { //FIXME Link to death~~~
                if (!mWatchers.contains(w)) {
                    mWatchers.add(w);
                    XposedLog.debug("iWatcher.watch-OK " + w);
                }
            }
        }

        @Override
        public void unWatch(IAppGuardWatcher w) {
            enforceCallingPermissions();
            Preconditions.checkNotNull(w);
            synchronized (mWatchers) { //FIXME Link to death~~~
                if (mWatchers.contains(w)) {
                    mWatchers.remove(w);
                    XposedLog.debug("iWatcher.unWatch-OK " + w);
                }
            }
        }

        @Override
        public void mockCrash() {
            throw new IllegalStateException("Let's CRASH, bye bye you...");
        }

        @Override
        public void setDebug(boolean debug) {
            if (mDebugEnabled.compareAndSet(!debug, debug)) {
                SystemSettings.APP_GUARD_DEBUG_MODE_B_S.writeToSystemSettings(getContext(), debug);
            }
            XposedLog.setLogLevel(debug ? XposedLog.LogLevel.ALL : XposedLog.LogLevel.WARN);
        }

        @Override
        public void onActivityPackageResume(String pkg) {
            onAppSwitchedTo(pkg);
            mTopActivityPkg.setData(pkg);
        }

        @Override
        public void onUserPresent() {
            String pkg = mTopActivityPkg.getData();
            if (pkg == null) {
                return;
            }
            if (!onEarlyVerifyConfirm(pkg)) {
                return;
            }
            verifyInternal(null, pkg, 0, 0, true, VerifyListenerAdapter.getDefault());
        }

        @Override
        public void setInterruptFPEventVBEnabled(int event, boolean enabled) {
            switch (event) {
                case XAppGuardManager.FPEvent.SUCCESS:
                    if (mInterruptFPSuccessVB.compareAndSet(!enabled, enabled)) {
                        SystemSettings.INTERRUPT_FP_SUCCESS_VB_ENABLED_B.writeToSystemSettings(getContext(), enabled);
                    }
                    break;
                case XAppGuardManager.FPEvent.ERROR:
                    if (mInterruptFPERRORVB.compareAndSet(!enabled, enabled)) {
                        SystemSettings.INTERRUPT_FP_ERROR_VB_ENABLED_B.writeToSystemSettings(getContext(), enabled);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add) {
            if (add) {
                mComponentReplacementsMap.put(from, to);
            } else {
                mComponentReplacementsMap.remove(from);
            }
        }

        /**
         * @param who Only Keep the switched package.
         */
        private void onAppSwitchedTo(String who) {
            if (mVerifySettings != null) {
                boolean clearOnAppSwitch = mVerifySettings.isVerifyOnAppSwitch();
                if (clearOnAppSwitch) {
                    mVerifiedPackages.clear();
                    mVerifiedPackages.add(who);
                }
            }
        }
    }

}
