package github.tornaco.xposedmoduletest.xposed.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageUserState;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkPolicyManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManagerPolicy;
import android.webkit.IWebViewUpdateService;
import android.webkit.WebViewProviderInfo;
import android.widget.Toast;

import com.android.internal.os.Zygote;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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

import de.robv.android.xposed.SELinuxHelper;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.android.common.Holder;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAshmanWatcher;
import github.tornaco.xposedmoduletest.IBooleanCallback1;
import github.tornaco.xposedmoduletest.IPackageUninstallCallback;
import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.IServiceControl;
import github.tornaco.xposedmoduletest.ITaskRemoveListener;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.ui.widget.ClickableToastManager;
import github.tornaco.xposedmoduletest.ui.widget.FloatView;
import github.tornaco.xposedmoduletest.util.ArrayUtil;
import github.tornaco.xposedmoduletest.util.GsonUtil;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.GlobalWhiteList;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.IProcessClearListenerAdapter;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.bean.DozeEvent;
import github.tornaco.xposedmoduletest.xposed.bean.NetworkRestriction;
import github.tornaco.xposedmoduletest.xposed.bean.OpLog;
import github.tornaco.xposedmoduletest.xposed.bean.OpsSettings;
import github.tornaco.xposedmoduletest.xposed.bean.SystemPropProfile;
import github.tornaco.xposedmoduletest.xposed.bean.TypePack;
import github.tornaco.xposedmoduletest.xposed.bean.VerifySettings;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.repo.SetRepo;
import github.tornaco.xposedmoduletest.xposed.repo.SettingsProvider;
import github.tornaco.xposedmoduletest.xposed.service.am.AMSProxy;
import github.tornaco.xposedmoduletest.xposed.service.am.ActiveServicesProxy;
import github.tornaco.xposedmoduletest.xposed.service.am.ActiveServicesServiceStopper;
import github.tornaco.xposedmoduletest.xposed.service.am.AppIdler;
import github.tornaco.xposedmoduletest.xposed.service.am.AppServiceControlServiceStopper;
import github.tornaco.xposedmoduletest.xposed.service.am.AppServiceController;
import github.tornaco.xposedmoduletest.xposed.service.am.InactiveAppIdler;
import github.tornaco.xposedmoduletest.xposed.service.am.KillAppIdler;
import github.tornaco.xposedmoduletest.xposed.service.am.PackageStateManager;
import github.tornaco.xposedmoduletest.xposed.service.am.ServiceRecordProxy;
import github.tornaco.xposedmoduletest.xposed.service.am.UsageStatsServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.bandwidth.BandwidthCommandCompat;
import github.tornaco.xposedmoduletest.xposed.service.doze.BatterState;
import github.tornaco.xposedmoduletest.xposed.service.doze.DeviceIdleControllerProxy;
import github.tornaco.xposedmoduletest.xposed.service.doze.DozeStateRetriever;
import github.tornaco.xposedmoduletest.xposed.service.doze.PowerWhitelistBackend;
import github.tornaco.xposedmoduletest.xposed.service.dpm.DevicePolicyManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.multipleapps.MultipleAppsManager;
import github.tornaco.xposedmoduletest.xposed.service.notification.NotificationManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.GCMFCMHelper;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.NotificationHandlerSettingsRetriever;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.PushNotificationHandler;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.TGPushNotificationHandler;
import github.tornaco.xposedmoduletest.xposed.service.opt.gcm.WeChatPushNotificationHandler;
import github.tornaco.xposedmoduletest.xposed.service.pm.InstallArgsProxy;
import github.tornaco.xposedmoduletest.xposed.service.pm.InstallerUtil;
import github.tornaco.xposedmoduletest.xposed.service.pm.OriginInfoProxy;
import github.tornaco.xposedmoduletest.xposed.service.pm.PackageInstallerManager;
import github.tornaco.xposedmoduletest.xposed.service.policy.PhoneWindowManagerProxy;
import github.tornaco.xposedmoduletest.xposed.service.power.PowerManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.provider.SystemSettings;
import github.tornaco.xposedmoduletest.xposed.service.rule.Rule;
import github.tornaco.xposedmoduletest.xposed.service.rule.RuleParser;
import github.tornaco.xposedmoduletest.xposed.service.shell.AshShellCommand;
import github.tornaco.xposedmoduletest.xposed.service.wm.SystemGesturesPointerEventListener;
import github.tornaco.xposedmoduletest.xposed.service.wm.SystemGesturesPointerEventListenerCallbackImpl;
import github.tornaco.xposedmoduletest.xposed.submodules.InputManagerInjectInputSubModule;
import github.tornaco.xposedmoduletest.xposed.submodules.SubModuleManager;
import github.tornaco.xposedmoduletest.xposed.submodules.debug.TestXposedMethod;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;
import github.tornaco.xposedmoduletest.xposed.util.ObjectToStringUtil;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XStopWatch;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static android.content.Context.KEYGUARD_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static github.tornaco.xposedmoduletest.xposed.app.XAshmanManager.POLICY_REJECT_NONE;
import static github.tornaco.xposedmoduletest.xposed.app.XAshmanManager.POLICY_REJECT_ON_DATA;
import static github.tornaco.xposedmoduletest.xposed.app.XAshmanManager.POLICY_REJECT_ON_WIFI;
import static github.tornaco.xposedmoduletest.xposed.bean.DozeEvent.FAIL_DEVICE_INTERACTIVE;
import static github.tornaco.xposedmoduletest.xposed.bean.DozeEvent.FAIL_GENERIC_FAILURE;
import static github.tornaco.xposedmoduletest.xposed.bean.DozeEvent.FAIL_RETRY_TIMEOUT;

/**
 * Created by guohao4 on 2017/11/9.
 * Email: Tornaco@163.com
 */

// TODO This file is really too long, please make sub-modules.
public class XAshmanServiceImpl extends XAshmanServiceAbs
        implements NotificationHandlerSettingsRetriever {

    private static final String SYSTEM_UI_PKG = "com.android.systemui";
    private static final String SYSTEM_UI_PKG_HTC = "com.htc.lockscreen";
    private static final String SYSTEM_UI_PKG_HUAWEI = "com.huawei.bd";

    private static final String TAG_LK = "LOCK-KILL-";

    private static final boolean DEBUG_BROADCAST;
    private static final boolean DEBUG_SERVICE;

    private static final boolean DEBUG_OP = false && BuildConfig.DEBUG;
    private static final boolean DEBUG_COMP = false && BuildConfig.DEBUG;

    static {
        boolean isBuildVarDebug = XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.DEBUG);
        DEBUG_BROADCAST = DEBUG_SERVICE = BuildConfig.DEBUG;
        XposedLog.boot("DEBUG_BROADCAST & DEBUG_SERVICE: " + DEBUG_BROADCAST);
    }

    private static final Set<String> WHITE_LIST = new HashSet<>();
    private static final Set<Pattern> WHITE_LIST_PATTERNS = new HashSet<>();
    // To prevent the apps with system signature added to white list.
    private static final Set<String> WHITE_LIST_HOOK = new HashSet<>();
    // Installed in system/, not contains system-packages and persist packages.
    private static final Set<String> SYSTEM_APPS = new HashSet<>();

    private static final Set<String> SYSTEM_UID_APPS = new HashSet<>();
    private static final Set<String> MEDIA_UID_APPS = new HashSet<>();
    private static final Set<String> PHONE_UID_APPS = new HashSet<>();

    private UUID mSerialUUID = UUID.randomUUID();

    private static int sClientUID = 0;

    private final ExecutorService mWorkingService = Executors.newCachedThreadPool();
    private final ExecutorService mLoggingService = Executors.newSingleThreadExecutor();

    // For debug, also for user.
    private final OpsCache mOpsCache = OpsCache.singleInstance();
    private final StartRecordCache mStartRecordCache = StartRecordCache.singleInstance();

    private final Map<String, Integer> mPackagesCache = new HashMap<>();

    private final Map<String, BlockRecord2> mBlockRecords = new HashMap<>();

    // Save the service name and starter uid.
    private final Map<String, Integer> mServiceStartRecords = new HashMap<>();

    private Handler mainHandler, mLazyHandler, mDozeHandler;

    private final Holder<String> mAudioFocusedPackage = new Holder<>();

    private final AtomicBoolean mWhiteSysAppEnabled = new AtomicBoolean(true);
    private final AtomicBoolean mBootBlockEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mStartBlockEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mStartRuleEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mLockKillEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mGreeningEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mResidentEnabled = new AtomicBoolean(false);

    private final AtomicBoolean mPermissionControlEnabled = new AtomicBoolean(true);

    private final AtomicBoolean mPrivacyEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mPanicHomeEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mPanicLockEnabled = new AtomicBoolean(false);

    private final AtomicBoolean mDataHasBeenMigrated = new AtomicBoolean(false);
    private final AtomicBoolean mShowAppCrashDumpEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mLazyEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mLazyRuleEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mLazySolutionApp = new AtomicBoolean(false);
    private final AtomicBoolean mLazySolutionFW = new AtomicBoolean(false);
    private final AtomicBoolean mDozeEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mForeDozeEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mDisableMotionEnabled = new AtomicBoolean(false);

    private final AtomicBoolean mPowerSaveModeEnabled = new AtomicBoolean(false);

    private final AtomicBoolean mAutoAddToBlackListForNewApp = new AtomicBoolean(false);
    private final AtomicBoolean mAutoAddNotificationToBlackListForNewApp = new AtomicBoolean(false);
    private final AtomicBoolean mShowFocusedActivityInfoEnabled = new AtomicBoolean(false);

    private final AtomicBoolean mLockKillDoNotKillAudioEnabled = new AtomicBoolean(true);
    private final AtomicBoolean mShowAppProcessUpdateNotification = new AtomicBoolean(false);

    private final AtomicBoolean mDoNotKillSBNEnabled = new AtomicBoolean(true);
    private final AtomicBoolean mDoNotKillSBNGreenEnabled = new AtomicBoolean(true);

    private final AtomicBoolean mRootActivityFinishKillEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mTaskRemovedKillEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mLongPressBackKillEnabled = new AtomicBoolean(false);
    private final AtomicBoolean mCompSettingBlockEnabled = new AtomicBoolean(false);

    private final AtomicBoolean mWakeupOnNotificationPosted = new AtomicBoolean(BuildConfig.DEBUG);

    private final Holder<String> mUserDefinedDeviceId = new Holder<>();
    private final Holder<String> mUserDefinedAndroidId = new Holder<>();
    private final Holder<String> mUserDefinedLine1Number = new Holder<>();

    // FIXME Now we force set control mode to BLACK LIST.
    private AtomicInteger mControlMode = new AtomicInteger(XAshmanManager.ControlMode.BLACK_LIST);

    private long mLockKillDelay, mDozeDelay;

    // FIXME Change to remote callbacks.
    private final Set<AshManHandler.WatcherClient> mWatcherClients = new HashSet<>();

    private NotificationManagerServiceProxy mNotificationService;

    // Safe mode is the last clear place user can stay.
    private boolean mIsSafeMode = false;

    private boolean mIsSystemReady = false;
    private boolean mIsNotificationPostReady = false;

    private AMSProxy mAmsProxy;
    private ActiveServicesProxy mActiveServicesProxy;
    private PowerManagerServiceProxy mPowerManagerServiceProxy;

    // App idler.
    private AppIdler mKillIdler, mInactiveIdler;
    private AppIdler mDummyIdler = new AppIdler() {
        @Override
        public void setAppIdle(String pkg) {
            XposedLog.wtf("I am a dummy idler, please file a bug!!!!!!!!");
        }

        @Override
        public void setListener(OnAppIdleListener listener) {
            // Nothing.
        }
    };

    private static final String ACTION_CLEAR_PROCESS = "github.tornaco.broadcast.action.clear_process";

    private final ErrorCatchRunnable mClearCompleteActionRunnable
            = new ErrorCatchRunnable(new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getContext(), "清理完成", Toast.LENGTH_SHORT).show();
            mRunningProcessPackages.clear();
            clearRunningAppProcessUpdateNotification();
        }
    }, "mClearCompleteActionRunnable");

    private ProtectedBroadcastReceiver mClearProcessBroadcast =
            new ProtectedBroadcastReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    try {
                        XposedLog.verbose("mClearProcessBroadcast, receive");
                        clearProcess(new IProcessClearListenerAdapter() {
                            @Override
                            public boolean doNotClearWhenIntervative() {
                                return false;
                            }

                            @Override
                            public void onAllCleared(String[] pkg) throws RemoteException {
                                super.onAllCleared(pkg);
                                mLazyHandler.post(mClearCompleteActionRunnable);
                            }
                        });
                    } catch (Exception e) {
                        XposedLog.wtf("Fail call clearProcess: " + Log.getStackTraceString(e));
                    }
                }
            });

    // Dynamic updated when AMS add app or remove app process.
    private final LinkedList<String> mRunningProcessPackages = new LinkedList<>();

    private void addToRunningProcessPackages(String pkg) {
        if (isInWhiteList(pkg)) return;
        if (isWhiteSysAppEnabled() && isInSystemAppList(pkg)) return;

        boolean isLKList = isPackageLKByUser(pkg);
        if (!isLKList) {
            XposedLog.verbose("addToRunningProcessPackages, skip for none LK: " + pkg);
            return;
        }

        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("addToRunningProcessPackages: " + pkg);
        }
        if (!mRunningProcessPackages.contains(pkg)) {
            mRunningProcessPackages.addFirst(pkg);
            onRunningProcessPackagesUpdate();
        }
    }

    private void removeFromRunningProcessPackages(String... pkg) {
        if (pkg == null || pkg.length == 0) return;
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("removeFromRunningProcessPackages: " + Arrays.toString(pkg));
        }
        boolean changed = false;
        for (String p : pkg) {
            if (mRunningProcessPackages.remove(p)) {
                changed = true;
            }
        }
        if (changed) {
            onRunningProcessPackagesUpdate();
        }
    }

    private Runnable mShowRunningAppProcessUpdateNotificationRunnable
            = new ErrorCatchRunnable(this::showRunningAppProcessUpdateNotification, "showRunningAppProcessUpdateNotification");

    private void onRunningProcessPackagesUpdate() {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onRunningProcessPackagesUpdate");
        }

        // always handle in lazy handler.
        mLazyHandler.removeCallbacks(mShowRunningAppProcessUpdateNotificationRunnable);
        mLazyHandler.post(mShowRunningAppProcessUpdateNotificationRunnable);
    }

    private AppIdler.OnAppIdleListener mOnAppIdleListener = this::removeFromRunningProcessPackages;

    private BroadcastReceiver mBatteryStateReceiver =
            new ProtectedBroadcastReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (mDozeHandler != null && action != null && action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN);
                        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                        BatterState bs = new BatterState(status, level);
                        mDozeHandler.obtainMessage(DozeHandlerMessages.MSG_ONBATTERYSTATECHANGE, bs)
                                .sendToTarget();
                    }
                }
            });


    private BroadcastReceiver mScreenReceiver =
            new ProtectedBroadcastReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                        onScreenOff();
                    }

                    if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                        onScreenOn();
                    }

                    if (Intent.ACTION_USER_PRESENT.equals(intent.getAction())) {
                        onUserPresent();
                    }
                }
            });

    private BroadcastReceiver mUserReceiver
            = new ProtectedBroadcastReceiver(new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            if (Intent.ACTION_USER_SWITCHED.equals(intent.getAction())) {
                try {
                    int userHandler = intent.getIntExtra(Intent.EXTRA_USER_HANDLE, -1);
                    XposedLog.verbose(XposedLog.PREFIX_USER + "User changed: " + userHandler);
                } catch (Throwable e) {
                    XposedLog.wtf(e);
                }
            }
        }
    });

    @SuppressLint("UseSparseArrays")
    private final Map<Integer, ComponentName> mTaskIdMap = new HashMap<>();

    private XAppGuardServiceImpl mAppGuardService;

    XAshmanServiceImpl() {
        mAppGuardService = new XAppGuardServiceImplDev(this);
    }

    public Map<String, Integer> getPackagesCache() {
        return mPackagesCache;
    }

    private void onUserPresent() {
        mainHandler.sendEmptyMessage(AshManHandlerMessages.MSG_ONSCREENON);
    }

    private void onScreenOff() {
        mainHandler.sendEmptyMessage(AshManHandlerMessages.MSG_ONSCREENOFF);
        if (mDozeHandler != null) {
            mDozeHandler.sendEmptyMessage(DozeHandlerMessages.MSG_ONSCREENOFF);
        }
    }

    private void onScreenOn() {
        if (mDozeHandler != null) {
            mDozeHandler.sendEmptyMessage(DozeHandlerMessages.MSG_ONSCREENON);

            cancelEnterIdleModePosts("Screen is on");
            // Check if this is an end of doze.
            postDozeEndCheck();
        }
    }

    private BroadcastReceiver mPackageReceiver =
            new ProtectedBroadcastReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    String action = intent.getAction();
                    if (action == null || intent.getData() == null) {
                        // They send us bad action~
                        return;
                    }
                    mLazyHandler.obtainMessage(AshManLZHandlerMessages.MSG_ONBROADCASTACTION, intent).sendToTarget();
                }
            });

    private BroadcastReceiver mTestProtectedBroadcastReceiver =
            new ProtectedBroadcastReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    throw new IllegalStateException("This is a test");
                }
            });

    private BroadcastReceiver mTestSystemErrorBroadcastReceiver =
            (new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    throw new IllegalStateException("This is a system error test");
                }
            });

    private void onAppGuardClientUninstalled() {
        if (PkgUtil.isPkgInstalled(getContext(), BuildConfig.APPLICATION_ID)) return;

        long id = Binder.clearCallingIdentity();
        try {
            // Disable app guard.
            SystemSettings.APP_GUARD_ENABLED_NEW_B.writeToSystemSettings(getContext(), false);

            AlertDialog d = new AlertDialog.Builder(getContext())
                    .setTitle("应用管理")
                    .setMessage("应用管理已经被卸载，是否要清除 自启动/关联启动/锁屏清理/后台限制 的名单等设置数据？" +
                            "如果你是想安装新版本，强烈建议你保留该数据。")
                    .setCancelable(false)
                    .setPositiveButton("清除数据",
                            (dialog, which) -> RepoProxy.getProxy().deleteAll())
                    .setNegativeButton("暂不清除", (dialog, which) -> {

                    })
                    .create();
            d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
            d.show();

        } catch (Exception e) {
            XposedLog.wtf("Fail show system dialog: " + Log.getStackTraceString(e));
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    private void parsePackageAsync(final String... pkg) {
        mWorkingService.execute(() -> cachePackages(pkg));
    }

    private static final AtomicInteger NOTIFICATION_ID_DYNAMIC = new AtomicInteger(0);
    private static final int NOTIFICATION_ID_APP_PROCESS = Integer.MAX_VALUE - 2018;

    private static final String NOTIFICATION_CHANNEL_ID_DEFAULT = "dev.tornaco.notification.channel.id.X-APM-DEFAULT";
    private static final String NOTIFICATION_CHANNEL_ID_APP_PROCESS = "dev.tornaco.notification.channel.id.X-APM-PROCESS";

    private void createDefaultNotificationChannelForO() {
        if (OSUtil.isOOrAbove()) {
            NotificationManager notificationManager = (NotificationManager)
                    getContext().getSystemService(
                            Context.NOTIFICATION_SERVICE);
            NotificationChannel nc = null;
            if (notificationManager != null) {
                nc = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_DEFAULT);
            }
            if (nc != null) {
                return;
            }
            NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_DEFAULT,
                    "应用管理默认频道",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400});
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private void createAppProcessNotificationChannelForO() {
        if (OSUtil.isOOrAbove()) {
            NotificationManager notificationManager = (NotificationManager)
                    getContext().getSystemService(
                            Context.NOTIFICATION_SERVICE);
            NotificationChannel nc = null;
            if (notificationManager != null) {
                nc = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID_APP_PROCESS);
            }
            if (nc != null) {
                return;
            }
            NotificationChannel notificationChannel;
            notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_APP_PROCESS,
                    "应用管理进程提示频道",
                    NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    private void showNewAppRestrictedNotification(Context context, String pkg, String name) {
        XposedLog.verbose("Add to black list showNewAppRestrictedNotification: " + name);

        createDefaultNotificationChannelForO();

        // FIXME Extract an intent.
        Intent viewer = new Intent();
        viewer.setPackage(BuildConfig.APPLICATION_ID);
        viewer.setClassName(BuildConfig.APPLICATION_ID,
                "github.tornaco.xposedmoduletest.ui.activity.app.PerAppSettingsDashboardActivity");
        viewer.putExtra("pkg_name", pkg);
        viewer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_DEFAULT);

        Notification n = builder
                .setContentIntent(PendingIntent.getActivity(getContext(), 0x1, viewer,
                        PendingIntent.FLAG_CANCEL_CURRENT))
                .setContentTitle("模板已应用")
                .setContentText("已按照模板将 " + name + " 完成设置")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .build();

        if (OSUtil.isMOrAbove()) {
            n.setSmallIcon(new AppResource(getContext()).loadIconFromAPMApp("ic_stat_apply_template"));
        }

        NotificationManagerCompat.from(context)
                .notify(NOTIFICATION_ID_DYNAMIC.getAndIncrement(), n);
    }

    private void clearRunningAppProcessUpdateNotification() {
        NotificationManagerCompat.from(getContext())
                .cancel(NOTIFICATION_ID_APP_PROCESS);
    }

    private void showRunningAppProcessUpdateNotification() {
        if (!isNotificationPostReady()) {
            return;
        }
        if (!isShowAppProcessUpdateNotificationEnabled()) {
            return;
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("showRunningAppProcessUpdateNotification");
        }

        createAppProcessNotificationChannelForO();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(),
                NOTIFICATION_CHANNEL_ID_APP_PROCESS);

        Intent clearBroadcastIntent = new Intent(ACTION_CLEAR_PROCESS);
        PendingIntent clearIntent = PendingIntent.getBroadcast(getContext(), 0, clearBroadcastIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent viewerIntent = new Intent();
        viewerIntent.setPackage(BuildConfig.APPLICATION_ID);
        viewerIntent.setClassName(BuildConfig.APPLICATION_ID,
                "github.tornaco.xposedmoduletest.ui.activity.helper.RunningServicesActivity");
        viewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent detailsIntent = PendingIntent.getActivity(getContext(), 0, viewerIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (mRunningProcessPackages.size() < 1) {
            // Now no apps need to be clear.
            clearRunningAppProcessUpdateNotification();
            return;
        }

        // FIXME May occur index err. need sync action.
        String recentApp = String.valueOf(PkgUtil.loadNameByPkgName(getContext(), mRunningProcessPackages.get(0)));

        Notification n = builder
                .setContentTitle("等待清理的应用")
                .setContentText("当前有" + recentApp + "等" + mRunningProcessPackages.size() + "个应用等待被清理。")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setContentIntent(clearIntent)
                .setAutoCancel(true)
                .addAction(0, "立即清理", clearIntent)
                .addAction(0, "查看更多", detailsIntent)
                .build();

        if (OSUtil.isMOrAbove()) {
            n.setSmallIcon(new AppResource(getContext()).loadIconFromAPMApp("ic_stat_process_update"));
        }

        NotificationManagerCompat.from(getContext())
                .notify(NOTIFICATION_ID_APP_PROCESS, n);

        if (BuildConfig.DEBUG) {
            XposedLog.verbose("showRunningAppProcessUpdateNotification:"
                    + Arrays.toString(mRunningProcessPackages.toArray()));
        }
    }

    private void cachePackages(final String... pkg) {
        if (pkg == null) return;

        final PackageManager pm = getContext().getPackageManager();

        Collections.consumeRemaining(pkg, s -> {
            ApplicationInfo applicationInfo;
            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    applicationInfo = pm.getApplicationInfo(s, PackageManager.MATCH_UNINSTALLED_PACKAGES);
                } else {
                    applicationInfo = pm.getApplicationInfo(s, PackageManager.GET_UNINSTALLED_PACKAGES);
                }

                int uid = applicationInfo.uid;
                String pkg1 = applicationInfo.packageName;
                if (TextUtils.isEmpty(pkg1)) return;

                if (XposedLog.isVerboseLoggable()) {
                    XposedLog.verbose("Cached pkg:" + pkg1 + "-" + uid);
                }

                // Cache it.
                mPackagesCache.put(pkg1, uid);
                PkgUtil.cachePkgUid(pkg1, uid);

            } catch (Exception ignored) {
                XposedLog.wtf("Fail cachePackages: " + ignored);
            }
        });
    }

    private void cachePackages() {
        XposedLog.verbose("cachePackages");
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
                    applicationInfo -> {
                        String pkg = applicationInfo.packageName;
                        int uid = applicationInfo.uid;
                        if (TextUtils.isEmpty(pkg)) {
                            XposedLog.wtf("Found no pkg app:" + applicationInfo);
                            return;
                        }

                        mPackagesCache.put(pkg, uid);
                        PkgUtil.cachePkgUid(pkg, uid);

                        // Add system apps to system list.
                        boolean isSystemApp = (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                        if (isSystemApp) {
                            addToSystemApps(pkg);
                        }

                        android.content.pm.PackageInfo packageInfo;
                        // Check if android system uid or media, phone.
                        try {
                            packageInfo = pm.getPackageInfo(pkg, 0);
                            String sharedUserId = packageInfo.sharedUserId;
                            if ("android.uid.phone".equals(sharedUserId)) {
                                addToPhoneApps(pkg);
                            }
                            if ("android.media".equals(sharedUserId)) {
                                addToMediaApps(pkg);
                            }
                            if ("android.uid.system".equals(sharedUserId)) {
                                addToCoreApps(pkg);
                            }

                        } catch (Exception e) {
                            XposedLog.wtf("NameNotFoundException: " + e + ", for: " + pkg);
                        }
                    });
        } catch (Exception ignored) {
            XposedLog.debug("Can not getSingleton UID for our client:" + ignored);
        }
    }

    private boolean isInWhiteList(String pkg) {
        if (pkg == null) return false;
        // Owner package is always white listed.
        if (pkg.equals(BuildConfig.APPLICATION_ID)) return true;

        boolean inWhite = WHITE_LIST.contains(pkg);
        if (inWhite) return true;

        // Check if webview provider.
        // if (isWebviewProvider(pkg)) return true;

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

        // Check dynamic white list hook.
        if (RepoProxy.getProxy().getWhite_list_hooks_dynamic().has(pkg)) {
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("Not add to white list because it is dynamic hooked: " + pkg);
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

    boolean isInSystemAppList(String pkg) {
        return SYSTEM_APPS.contains(pkg);
    }

    private synchronized static void addToSystemApps(String pkg) {
        if (!SYSTEM_APPS.contains(pkg)) {
            SYSTEM_APPS.add(pkg);
        }
    }

    private synchronized static void addToMediaApps(String pkg) {
        if (!MEDIA_UID_APPS.contains(pkg)) {
            MEDIA_UID_APPS.add(pkg);
        }
    }

    private synchronized static void addToPhoneApps(String pkg) {
        if (!PHONE_UID_APPS.contains(pkg)) {
            PHONE_UID_APPS.add(pkg);
        }
    }

    private synchronized static void addToCoreApps(String pkg) {
        if (!SYSTEM_UID_APPS.contains(pkg)) {
            SYSTEM_UID_APPS.add(pkg);
        }
    }

    private void checkSafeMode() {
        mIsSafeMode = getContext().getPackageManager().isSafeMode();
    }

    @Override
    public boolean isSystemReady() {
        return mIsSystemReady;
    }

    public boolean isNotificationPostReady() {
        return mIsNotificationPostReady;
    }

    private void loadConfigFromSettings() {
        try {
            boolean whiteSysApp = (boolean) SystemSettings.ASH_WHITE_SYS_APP_ENABLED_B.readFromSystemSettings(getContext());
            mWhiteSysAppEnabled.set(whiteSysApp);
            XposedLog.boot("whiteSysAapp: " + String.valueOf(whiteSysApp));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean bootBlockEnabled = (boolean) SystemSettings.BOOT_BLOCK_ENABLED_B.readFromSystemSettings(getContext());
            mBootBlockEnabled.set(bootBlockEnabled);
            XposedLog.boot("bootBlockEnabled: " + String.valueOf(bootBlockEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean startBlockEnabled = (boolean) SystemSettings.START_BLOCK_ENABLED_B.readFromSystemSettings(getContext());
            mStartBlockEnabled.set(startBlockEnabled);
            XposedLog.boot("startBlockEnabled:" + String.valueOf(startBlockEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean startRuleEnabled = (boolean) SystemSettings.APM_START_RULE_B.readFromSystemSettings(getContext());
            mStartRuleEnabled.set(startRuleEnabled);
            XposedLog.boot("startRuleEnabled:" + String.valueOf(startRuleEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean lockKillEnabled = (boolean) SystemSettings.LOCK_KILL_ENABLED_B.readFromSystemSettings(getContext());
            mLockKillEnabled.set(lockKillEnabled);
            XposedLog.boot("lockKillEnabled: " + String.valueOf(lockKillEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean residentEnabled = (boolean) SystemSettings.APM_RESIDENT_B.readFromSystemSettings(getContext());
            mResidentEnabled.set(residentEnabled);
            XposedLog.boot("residentEnabled: " + String.valueOf(residentEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean panicHome = (boolean) SystemSettings.APM_PANIC_HOME_B.readFromSystemSettings(getContext());
            mPanicHomeEnabled.set(panicHome);
            XposedLog.boot("panicHome: " + String.valueOf(panicHome));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean panicLock = (boolean) SystemSettings.APM_PANIC_LOCK_B.readFromSystemSettings(getContext());
            mPanicLockEnabled.set(panicLock);
            XposedLog.boot("panicLock: " + String.valueOf(panicLock));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean privEnabled = (boolean) SystemSettings.PRIVACY_ENABLED_B.readFromSystemSettings(getContext());
            mPrivacyEnabled.set(privEnabled);
            XposedLog.boot("lockKillEnabled: " + String.valueOf(privEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean greeningEnabled = (boolean) SystemSettings.GREENING_ENABLED_B.readFromSystemSettings(getContext());
            mGreeningEnabled.set(greeningEnabled);
            XposedLog.boot("greeningEnabled: " + String.valueOf(greeningEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            String userDeviceId = (String) SystemSettings.USER_DEFINED_DEVICE_ID_T_S.readFromSystemSettings(getContext());
            mUserDefinedDeviceId.setData(userDeviceId);
            XposedLog.boot("userDeviceId: " + String.valueOf(userDeviceId));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            String userAndroidId = (String) SystemSettings.USER_DEFINED_ANDROID_ID_T_S.readFromSystemSettings(getContext());
            mUserDefinedAndroidId.setData(userAndroidId);
            XposedLog.boot("userAndroidId: " + String.valueOf(userAndroidId));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            String userLine1Number = (String) SystemSettings.USER_DEFINED_LINE1_NUM_T_S.readFromSystemSettings(getContext());
            mUserDefinedLine1Number.setData(userLine1Number);
            XposedLog.boot("userLine1Number: " + String.valueOf(userLine1Number));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean migrated = (boolean) SystemSettings.DATA_MIGRATE_B.readFromSystemSettings(getContext());
            mDataHasBeenMigrated.set(migrated);
            XposedLog.boot("migrated: " + String.valueOf(migrated));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean dumpCrash = (boolean) SystemSettings.SHOW_CRASH_DUMP_B.readFromSystemSettings(getContext());
            mShowAppCrashDumpEnabled.set(dumpCrash);
            XposedLog.boot("dumpCrash: " + String.valueOf(dumpCrash));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean lazy = (boolean) SystemSettings.LAZY_ENABLED_B.readFromSystemSettings(getContext());
            mLazyEnabled.set(lazy);
            XposedLog.boot("lazy: " + String.valueOf(lazy));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean lazyRule = (boolean) SystemSettings.APM_LAZY_RULE_B.readFromSystemSettings(getContext());
            mLazyRuleEnabled.set(lazyRule);
            XposedLog.boot("lazyRule: " + String.valueOf(lazyRule));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean lazySolutionApp = (boolean) SystemSettings.APM_LAZY_SOLUTION_APP_B.readFromSystemSettings(getContext());
            mLazySolutionApp.set(lazySolutionApp);
            XposedLog.boot("lazySolutionApp: " + String.valueOf(lazySolutionApp));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean lazySolutionFW = (boolean) SystemSettings.APM_LAZY_SOLUTION_FW_B.readFromSystemSettings(getContext());
            mLazySolutionFW.set(lazySolutionFW);
            XposedLog.boot("mLazySolutionFW: " + String.valueOf(lazySolutionFW));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean autoAddBlack = (boolean) SystemSettings.AUTO_BLACK_FOR_NEW_INSTALLED_APP_B.readFromSystemSettings(getContext());
            mAutoAddToBlackListForNewApp.set(autoAddBlack);
            XposedLog.boot("autoAddBlack: " + String.valueOf(autoAddBlack));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean autoAddBlackNotification = (boolean) SystemSettings.AUTO_BLACK_NOTIFICATION_FOR_NEW_INSTALLED_APP_B
                    .readFromSystemSettings(getContext());
            mAutoAddNotificationToBlackListForNewApp.set(autoAddBlackNotification);
            XposedLog.boot("autoAddBlackNotification: " + String.valueOf(autoAddBlackNotification));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        // Do not read from settings anymore, this is dangerous...
//        try {
//            boolean showFocusedActivity = (boolean) SystemSettings.SHOW_FOCUSED_ACTIVITY_INFO_B.readFromSystemSettings(getContext());
//            mShowFocusedActivityInfoEnabled.set(showFocusedActivity);
//            XposedLog.boot("showFocusedActivity: " + String.valueOf(showFocusedActivity));
//        } catch (Throwable e) {
//            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
//        }

        try {
            boolean lockKillDoNotKillAudioEnabled = (boolean) SystemSettings.LOCK_KILL_DONT_KILL_AUDIO_ENABLED_B
                    .readFromSystemSettings(getContext());
            mLockKillDoNotKillAudioEnabled.set(lockKillDoNotKillAudioEnabled);
            XposedLog.boot("lockKillDoNotKillAudioEnabled: " + String.valueOf(lockKillDoNotKillAudioEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean showAppProcessUpdateNotification = (boolean) SystemSettings.APM_SHOW_APP_PROCESS_UPDATE_B
                    .readFromSystemSettings(getContext());
            mShowAppProcessUpdateNotification.set(showAppProcessUpdateNotification);
            XposedLog.boot("showAppProcessUpdateNotification: " + String.valueOf(showAppProcessUpdateNotification));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean doNotKillSBNEnabled = (boolean) SystemSettings.ASH_WONT_KILL_SBN_APP_B
                    .readFromSystemSettings(getContext());
            mDoNotKillSBNEnabled.set(doNotKillSBNEnabled);
            XposedLog.boot("doNotKillSBNEnabled: " + String.valueOf(doNotKillSBNEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean doNotKillSBNGreenEnabled = (boolean) SystemSettings.ASH_WONT_KILL_SBN_APP_GREEN_B
                    .readFromSystemSettings(getContext());
            mDoNotKillSBNGreenEnabled.set(doNotKillSBNGreenEnabled);
            XposedLog.boot("doNotKillSBNGreenEnabled: " + String.valueOf(doNotKillSBNGreenEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean rootKillEnabled = (boolean) SystemSettings.ROOT_ACTIVITY_KILL_ENABLED_B
                    .readFromSystemSettings(getContext());
            mRootActivityFinishKillEnabled.set(rootKillEnabled);
            XposedLog.boot("rootKillEnabled: " + String.valueOf(rootKillEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean taskRemovedKillEnabled = (boolean) SystemSettings.REMOVE_TASK_KILL_ENABLED_B
                    .readFromSystemSettings(getContext());
            mTaskRemovedKillEnabled.set(taskRemovedKillEnabled);
            XposedLog.boot("taskRemovedKillEnabled: " + String.valueOf(taskRemovedKillEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean longPressBackKill = (boolean) SystemSettings.LONG_PRESS_BACK_KILL_ENABLED_B
                    .readFromSystemSettings(getContext());
            mLongPressBackKillEnabled.set(longPressBackKill);
            XposedLog.boot("longPressBackKill: " + String.valueOf(longPressBackKill));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean compSettingBlockEnabled = (boolean) SystemSettings.COMP_SETTING_BLOCK_ENABLED_B
                    .readFromSystemSettings(getContext());
            mCompSettingBlockEnabled.set(compSettingBlockEnabled);
            XposedLog.boot("compSettingBlockEnabled: " + String.valueOf(compSettingBlockEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            mLockKillDelay = (long) SystemSettings.LOCK_KILL_DELAY_L.readFromSystemSettings(getContext());
            XposedLog.boot("mLockKillDelay: " + String.valueOf(mLockKillDelay));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            mDozeDelay = (long) SystemSettings.DOZE_DELAY_L.readFromSystemSettings(getContext());
            XposedLog.boot("mDozeDelay: " + String.valueOf(mDozeDelay));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean doze = (boolean) SystemSettings.APM_DOZE_ENABLE_B.readFromSystemSettings(getContext());
            mDozeEnabled.set(doze);
            XposedLog.boot("doze: " + String.valueOf(doze));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean forceDoze = (boolean) SystemSettings.APM_FORCE_DOZE_ENABLE_B.readFromSystemSettings(getContext());
            mForeDozeEnabled.set(forceDoze);
            XposedLog.boot("forceDoze: " + String.valueOf(forceDoze));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean disableMotion = (boolean) SystemSettings.APM_DISABLE_MOTION_ENABLE_B.readFromSystemSettings(getContext());
            mDisableMotionEnabled.set(disableMotion);
            XposedLog.boot("disableMotion: " + String.valueOf(disableMotion));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean powerSave = (boolean) SystemSettings.APM_POWER_SAVE_B.readFromSystemSettings(getContext());
            mPowerSaveModeEnabled.set(powerSave);
            XposedLog.boot("powerSave: " + String.valueOf(powerSave));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        try {
            boolean wakeupOnNotificationPosted = (boolean) SystemSettings.WAKE_UP_ON_NOTIFICATION_POSTED_ENABLED_B
                    .readFromSystemSettings(getContext());
            mWakeupOnNotificationPosted.set(wakeupOnNotificationPosted);
            XposedLog.boot("wakeupOnNotificationPosted: " + String.valueOf(wakeupOnNotificationPosted));
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadConfigFromSettings:" + Log.getStackTraceString(e));
        }

        // NEW SETTINGS ARE STORED BY SETTINGS-PROVIDER.
        // THIS IS SIMPLE FOR US TO CONTROL.
        // SO PLEASE USE THIS WAY INSTEAD OF SETTINGS FROM SYSTEM.
        try {
            mInactiveInsteadOfKillAppInstead.set(SettingsProvider.get().getBoolean("INACTIVE_INSTEAD_OF_KILL", false));
            XposedLog.boot("mInactiveInsteadOfKillAppInstead: " + String.valueOf(mInactiveInsteadOfKillAppInstead));
        } catch (Throwable e) {
            XposedLog.wtf("Fail load settings from SettingsProvider:" + Log.getStackTraceString(e));
        }

        try {
            mIsPushMessageHandleEnabled.set(SettingsProvider.get().getBoolean(PUSH_MESSAGE_HANDLER_ENABLE_TAG, false));
            XposedLog.boot("mIsPushMessageHandleEnabled: " + String.valueOf(mIsPushMessageHandleEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail load settings from SettingsProvider:" + Log.getStackTraceString(e));
        }

        try {
            mIsSystemPropEnabled.set(SettingsProvider.get().getBoolean(SYSTEM_PROP_ENABLED, false));
            XposedLog.boot("mIsSystemPropEnabled: " + String.valueOf(mIsSystemPropEnabled));
        } catch (Throwable e) {
            XposedLog.wtf("Fail load settings from SettingsProvider:" + Log.getStackTraceString(e));
        }
    }

    @Override
    @Deprecated
    public boolean checkService(Intent service, String callingPackage, int callingPid, int callingUid,
                                boolean callingFromFg) {
        return true;
    }

    // If we fail into doze, retry in 5min.
    private static final long REPOST_DOZE_DELAY = 2 * 1000;
    private static final long END_DOZE_CHECK_DELAY = 2000;
    private static final int MAX_RETRY_TIME_TO_SIZE = 99;

    private DeviceIdleControllerProxy mDeviceIdleController;
    private PowerWhitelistBackend mPowerWhitelistBackend;

    private final DozeEvent mLastDozeEvent = new DozeEvent();

    private final static int MAX_DOZE_HISTORY_SIZE = 20;

    private final LinkedList<DozeEvent> mDozeHistory = new LinkedList<>();

    private final Object mDozeLock = new Object();

    @SuppressLint("HandlerLeak")
    private class DozeHandlerImpl extends Handler implements DozeHandler {

        // This should be executed on worker thread.
        private final ErrorCatchRunnable mDozeStepperErrorCatch
                = new ErrorCatchRunnable(new Runnable() {
            @Override
            public void run() {

                if (!isDozeSupported()) {
                    XposedLog.verbose(XposedLog.PREFIX_DOZE + "mDozeStepper execute but doze not supported");
                    return;
                }

                XposedLog.verbose(XposedLog.PREFIX_DOZE + "mDozeStepper execute delay: " + mDozeDelay);

                if (mDeviceIdleController == null) {
                    XposedLog.wtf(XposedLog.PREFIX_DOZE
                            + "Calling postEnterIdleMode with mDeviceIdleController is null");
                    return;
                }

                final AtomicInteger enterDozeTryingTimes = new AtomicInteger(0);

                boolean alreadyInDoze = DozeStateRetriever.isDeviceIdleMode(getContext());

                // We are not in doze mode now, will start to doze.
                if (!alreadyInDoze) {
                    XposedLog.verbose("isForceIdle: " + mDeviceIdleController.isForceIdle());
                    mDeviceIdleController.setDeepIdle(true);
                    mDeviceIdleController.setForceIdle(isForceDozeEnabled());
                    XposedLog.verbose("isForceIdle: " + mDeviceIdleController.isForceIdle());

                    // Apply motion state.
                    // FIXME MIUI may fail for this setting, tmp remove.
                    // I have no MIUI device and User can not provide
                    // useful info for this, so, this is a ?
                    if (!OSUtil.isMIUI()) {

                    }

                    if (isDisableMotionEnabled()) {
                        mDeviceIdleController.stopMonitoringMotionLocked();
                    } else {
                        // mDeviceIdleController.startMonitoringMotionLocked();
                    }

                    mDeviceIdleController.becomeInactiveIfAppropriateLocked();

                    onDozeEnterStart();
                }

                int curState = mDeviceIdleController.getState();
                while (curState != DeviceIdleControllerProxy.STATE_IDLE) {

                    if (enterDozeTryingTimes.get() > MAX_RETRY_TIME_TO_SIZE) {
                        XposedLog.wtf(XposedLog.PREFIX_DOZE + "Fail enter doze mode after trying max times");
                        // Post doze message again.
                        postEnterIdleMode(mDozeDelay);

                        // Add to events.
                        onDozeEnterFail(FAIL_RETRY_TIMEOUT);

                        // Exit force.
                        mDeviceIdleController.exitForceIdleLocked();
                        return;
                    }

                    // Increase try times.
                    int time = enterDozeTryingTimes.incrementAndGet();

                    // Check if we are interactive.
                    PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                    boolean isInteractive = false;

                    if (powerManager != null) {
                        isInteractive = powerManager.isInteractive();
                    }

                    if (isInteractive) {
                        XposedLog.wtf("isInteractive when trying to setup idle mode");
                        // Add to events.
                        cancelEnterIdleModePosts("isInteractive");
                        onDozeEnterFail(FAIL_DEVICE_INTERACTIVE);

                        return;
                    }

                    stepIdleStateLocked();

                    if (curState == mDeviceIdleController.getState()) {
                        XposedLog.wtf("Unable to go deep idle, stopped at "
                                + DeviceIdleControllerProxy.stateToString(curState));
                        mDeviceIdleController.exitForceIdleLocked();
                        cancelEnterIdleModePosts("Fail doze");

                        onDozeEnterFail(FAIL_GENERIC_FAILURE);

                        return;
                    }

                    curState = mDeviceIdleController.getState();

                    XposedLog.verbose(XposedLog.PREFIX_DOZE + "Step idle @" + time + ", state " + curState);
                }

                XposedLog.debug(XposedLog.PREFIX_DOZE + "We are in doze mode!");

                // Cancel any pending post.
                cancelEnterIdleModePosts("Doze success");

                // Add to events.
                onDozeEnterSuccess();
            }
        }, "mDozeStepper");

        private BatterState mBatteryState;

        DozeHandlerImpl() {
            // Set doze event to init state.
            resetDozeEvent();
        }

        @Override
        public void handleMessage(Message msg) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose(DozeHandlerMessages.decodeMessage(msg.what));
            }

            switch (msg.what) {
                case DozeHandlerMessages.MSG_ENTERIDLEMODE:
                    DozeHandlerImpl.this.enterIdleMode();
                    break;
                case DozeHandlerMessages.MSG_STEPIDLESTATELOCKED:
                    DozeHandlerImpl.this.stepIdleStateLocked();
                    break;
                case DozeHandlerMessages.MSG_ONSCREENOFF:
                    DozeHandlerImpl.this.onScreenOff();
                    break;
                case DozeHandlerMessages.MSG_ONBATTERYSTATECHANGE:
                    DozeHandlerImpl.this.onBatteryStateChange((BatterState) msg.obj);
                    break;
                case DozeHandlerMessages.MSG_SETDOZEDELAYMILLS:
                    DozeHandlerImpl.this.setDozeDelayMills((Long) msg.obj);
                    break;
                case DozeHandlerMessages.MSG_SETDOZEENABLED:
                    DozeHandlerImpl.this.setDozeEnabled((Boolean) msg.obj);
                    break;
                case DozeHandlerMessages.MSG_UPDATEDOZEENDSTATE:
                    DozeHandlerImpl.this.updateDozeEndState();
                    break;
                case DozeHandlerMessages.MSG_SETFORCEDOZEENABLED:
                    DozeHandlerImpl.this.setForceDozeEnabled((Boolean) msg.obj);
                    break;
                case DozeHandlerMessages.MSG_SETDISABLEMOTIONENABLED:
                    DozeHandlerImpl.this.setDisableMotionEnabled((Boolean) msg.obj);
                    break;
                case DozeHandlerMessages.MSG_ONSCREENON:
                    DozeHandlerImpl.this.onScreenOn();
                    break;
            }

            super.handleMessage(msg);
        }

        @Override
        public void enterIdleMode() {
            int preCheckCode = isDeviceStateReadyToDoze();
            if (preCheckCode != DozeEvent.FAIL_NOOP) {
                XposedLog.wtf(XposedLog.PREFIX_DOZE + "Device not ready!!!");
                // Add to events.
                onDozeEnterFail(preCheckCode);
                return;
            }

            boolean alreadyPost = hasCallbacks(mDozeStepperErrorCatch);
            if (alreadyPost) {
                XposedLog.wtf(XposedLog.PREFIX_DOZE + "Already post mDozeStepperErrorCatch!!!");
                return;
            }

            post(mDozeStepperErrorCatch);
        }

        @Override
        public void stepIdleStateLocked() {
            if (mDeviceIdleController == null) {
                XposedLog.wtf(XposedLog.PREFIX_DOZE + "Calling postEnterIdleMode with mDeviceIdleController is null");
                return;
            }

            mDeviceIdleController.stepIdleStateLocked();

            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose(XposedLog.PREFIX_DOZE + "stepIdleStateLocked");
                int state = mDeviceIdleController.getState();
                XposedLog.verbose("state: " + DeviceIdleControllerProxy.stateToString(state));
            }
        }

        @Override
        public void onScreenOff() {
            if (!hasDozeFeature()) {
                XposedLog.verbose(XposedLog.PREFIX_DOZE + "onScreenOff, no doze feature on this build");
                return;
            }
            boolean isDozeEnabled = isDozeEnabled();
            if (isDozeEnabled) {
                postEnterIdleMode(mDozeDelay);
            } else {
                XposedLog.verbose(XposedLog.PREFIX_DOZE + "onScreenOff, doze is not enabled");
            }
        }

        @Override
        public void onBatteryStateChange(BatterState batterState) {
            XposedLog.verbose("onBatteryStateChange: " + batterState);
            mBatteryState = batterState;
        }

        @Override
        public void setDozeDelayMills(long delayMills) {
            mDozeDelay = delayMills;
            SystemSettings.DOZE_DELAY_L.writeToSystemSettings(getContext(), delayMills);
            if (XposedLog.isVerboseLoggable())
                XposedLog.verbose("setDozeDelayMills to: " + mDozeDelay);
        }

        @Override
        public void setDozeEnabled(boolean enabled) {
            if (mDozeEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_DOZE_ENABLE_B.writeToSystemSettings(getContext(), enabled);
            }
            if (!enabled) {
                resetDozeEvent();
            }
        }

        @Override
        public void setForceDozeEnabled(boolean enabled) {
            if (mForeDozeEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_FORCE_DOZE_ENABLE_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setDisableMotionEnabled(boolean enabled) {
            if (mDisableMotionEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_DISABLE_MOTION_ENABLE_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void updateDozeEndState() {
            boolean isIdleMode = DozeStateRetriever.isDeviceIdleMode(getContext());
            XposedLog.verbose(XposedLog.PREFIX_DOZE + "updateDozeEndState, isDeviceIdleMode: " + isIdleMode);
            if (!isIdleMode) {
                synchronized (mDozeLock) {
                    DozeEvent de = mLastDozeEvent;
                    if (de != null) {
                        if (de.getResult() == DozeEvent.RESULT_SUCCESS) {
                            onDozeEnd();
                        }
                    }
                }
            }
        }

        @Override
        public void onScreenOn() {
            // Exit doze force state.
            if (mDeviceIdleController != null) {
                mDeviceIdleController.exitForceIdleLocked();
                XposedLog.verbose("exitForceIdleLocked, state " + mDeviceIdleController.getState());
            }
        }

        private int isDeviceStateReadyToDoze() {
            XposedLog.verbose("isDeviceStateReadyToDoze: " + mBatteryState);
            if (mBatteryState == null) return DozeEvent.FAIL_NOOP;

            int state = mBatteryState.getStatus();
            if (state == BatteryManager.BATTERY_STATUS_CHARGING
                    || state == BatteryManager.BATTERY_STATUS_UNKNOWN) {
                // Do not block when in debug
//                if (!BuildConfig.DEBUG) {
//                    return DozeEvent.FAIL_POWER_CHARGING;
//                }
                // Ingore battery status.
            }

            PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            boolean isInteractive = powerManager != null && powerManager.isInteractive();
            if (isInteractive) return DozeEvent.FAIL_DEVICE_INTERACTIVE;

            return DozeEvent.FAIL_NOOP;
        }
    }

    // Notification handlers for GCM.
    private final Set<PushNotificationHandler> mPushNotificationHandlers = new HashSet<>();

    private void registerPushNotificationHandler(PushNotificationHandler handler) {
        mPushNotificationHandlers.add(handler);
        XposedLog.verbose("registerPushNotificationHandler: " + handler);
    }

    @Override
    @CommonBringUpApi
    public boolean checkBroadcastIntentSending(IApplicationThread caller, Intent intent) {
        mAppGuardService.checkBroadcastIntent(caller, intent);

        if (BuildConfig.DEBUG) {
            int callingUid = Binder.getCallingUid();
            XposedLog.verbose("checkBroadcastIntentSending: %s, callingUid %s", intent, callingUid);
        }

        // Run this in lazy handler.
        // Note. please make sure all calling of pmh in same handler.
        mLazyHandler.post(new ErrorCatchRunnable(() -> checkBroadcastIntentSendingInternal(intent), "checkBroadcastIntentSendingInternal"));

        return true;
    }

    @Override
    public boolean beforeHookBroadcastPerformResult() {
        return isPushMessageHandleEnabled();
    }

    @Override
    public int onHookBroadcastPerformResult(Intent intent, int resultCode) {
        if (intent == null) {
            return resultCode;
        }
        // Check if PMH is enabled.
        if (!isPushMessageHandleEnabled()) {
            return resultCode;
        }
        // Check if GCM intent.
        if (!GCMFCMHelper.isGcmOrFcmIntent(intent)) {
            return resultCode;
        }
        // Check if PMH for this pkg is enabled.
        String pkgName = intent.getPackage();
        if (TextUtils.isEmpty(pkgName)) {
            return resultCode;
        }
        if (!isPushMessageHandlerEnabled(pkgName)) {
            return resultCode;
        }

        // Hooked!
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onHookBroadcastPerformResult, returning OK for %s", pkgName);
        }
        return Activity.RESULT_OK;
    }

    private void checkBroadcastIntentSendingInternal(Intent intent) {

        // Enhance for GCM/FCM.
        if (GCMFCMHelper.isGcmOrFcmIntent(intent)) {
            final String targetPkg = intent.getPackage();
            if (targetPkg != null) {
                XposedLog.verbose("checkBroadcastIntentSendingInternal isGcmOrFcmIntent: " + targetPkg);

                if (BuildConfig.DEBUG) {
                    XposedLog.verbose("PushNotificationHandler@ intent: "
                            + intent + "extra: " + intent.getExtras()
                            + ObjectToStringUtil.intentToString(intent));
                }

                // Notify handlers and go!
                if (isPushMessageHandleEnabled()) {
                    XposedLog.verbose("checkBroadcastIntentSendingInternal PMH not enabled");

                    // Notification handlers.
                    for (PushNotificationHandler handler : mPushNotificationHandlers) {
                        handler.handleIncomingIntent(targetPkg, intent);
                    }
                }

                @StartRuleCheck
                boolean shouldBeEnhanced =
                        isStartRuleEnabled() && RepoProxy.getProxy()
                                .getStart_rules()
                                .has(constructGCMEnhanceRuleForPackage(targetPkg));
                if (shouldBeEnhanced) {
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    // Notify GCM pending.
                    GCMFCMHelper.onGcmIntentReceived(targetPkg);
                    // Start app process.
                    // FIXME No need to add for system app? Need a real user test.
                    int appLevel = getAppLevel(targetPkg);
                    if (appLevel == XAshmanManager.AppLevel.THIRD_PARTY) {
                        addApp(targetPkg);
                    }
                    XposedLog.verbose("Introduce FLAG_INCLUDE_STOPPED_PACKAGES for GCM/FCM package: "
                            + targetPkg + ", newIntent: " + intent);

                    // Now post a lazy app check!
                    postLazyServiceKillerIfNecessary(targetPkg, GCMFCMHelper.GCM_INTENT_HANDLE_INTERVAL_MILLS, "GCM-ENHANCE");
                } else {
                    XposedLog.verbose("Won't Introduce FLAG_INCLUDE_STOPPED_PACKAGES for GCM/FCM package: " + targetPkg);
                }
            }
        }
    }

    // ALLOW GCMENHANCE *
    // ALLOW GCMENHANCE A
    private static String[] constructGCMEnhanceRuleForPackage(String packageName) {
        return new String[]{"ALLOW GCMENHANCE " + packageName,
                "ALLOW GCMENHANCE *"};
    }

    private void addApp(String targetPkg) {
        try {
            PackageManager pm = getContext().getPackageManager();
            ApplicationInfo info;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                info = pm.getApplicationInfo(targetPkg, PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                info = pm.getApplicationInfo(targetPkg, PackageManager.GET_UNINSTALLED_PACKAGES);
            }
            if (mAmsProxy != null) {
                mAmsProxy.addAppLocked(info, false, null);
            }
        } catch (Exception e) {
            XposedLog.wtf("Fail addApp: " + Log.getStackTraceString(e));
        }
    }

    @Override
    @CommonBringUpApi
    @SinceSDK(Build.VERSION_CODES.O)
    public void notifyTaskCreated(int taskId, ComponentName componentName) {
        if (componentName == null) return;
        // Use a dup package.
        ComponentName dup = new ComponentName(componentName.getPackageName(), componentName.getClassName());
        mTaskIdMap.put(taskId, dup);
    }

    @Override
    @CommonBringUpApi
    public ComponentName componentNameForTaskId(int taskId) {
        return mTaskIdMap.get(taskId);
    }

    @Override
    @InternalCall
    public boolean interruptPackageRemoval(String pkg) {
        return mAppGuardService.interruptPackageRemoval(pkg);
    }

    @Override
    @InternalCall
    public boolean interruptPackageDataClear(String pkg) {
        if (BuildConfig.APPLICATION_ID.equals(pkg)) {
            // Always allow clear data for our app???
        }
        return interruptPackageRemoval(pkg);
    }

    @Override
    @InternalCall
    public void notifyPackageDataClearInterrupt(String pkg) {
        mLazyHandler.post(new ErrorCatchRunnable(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "受保护应用清除数据请求已经被应用管理拦截", Toast.LENGTH_SHORT).show();
            }
        }, "notifyPackageDataClearInterrupt"));
    }

    @Override
    @InternalCall
    public void notifyPackageRemovalInterrupt(String pkg) {
        mLazyHandler.post(new ErrorCatchRunnable(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), "受保护应用卸载请求已经被应用管理拦截", Toast.LENGTH_SHORT).show();
            }
        }, "notifyPackageDataClearInterrupt"));
    }

    @Override
    public boolean onEarlyVerifyConfirm(String pkg, String reason) {
        return mAppGuardService.onEarlyVerifyConfirm(pkg, reason);
    }

    @Override
    public void verify(Bundle options, String pkg, ComponentName componentName, int uid, int pid, VerifyListener listener) {
        mAppGuardService.verify(options, pkg, componentName, uid, pid, listener);
    }

    @Override
    public void reportActivityLaunching(Intent intent, String reason) {
        XposedLog.verbose("reportActivityLaunching: %s %s", reason, intent);
        PkgUtil.onAppLaunching(PkgUtil.packageNameOf(intent), reason);
        onPackageMoveToFront(intent);
    }

    @Override
    public Intent checkIntent(Intent from) {
        return mAppGuardService.checkIntent(from);
    }

    @Override
    public long wrapCallingUidForIntent(long from, Intent intent) {
        return mAppGuardService.wrapCallingUidForIntent(from, intent);
    }

    @Override
    public boolean isBlurForPkg(String pkg) {
        return pkg != null && mAppGuardService.isBlurForPkg(pkg);
    }

    @Override
    public float getBlurScale() {
        return mAppGuardService.getBlurScale();
    }

    @Override
    public boolean interruptFPSuccessVibrate() {
        return mAppGuardService.interruptFPSuccessVibrate();
    }

    @Override
    public boolean interruptFPErrorVibrate() {
        return mAppGuardService.interruptFPErrorVibrate();
    }

    @Override
    public boolean isActivityStartShouldBeInterrupted(ComponentName componentName) {
        return mAppGuardService.isActivityStartShouldBeInterrupted(componentName);
    }

    @Override
    @CommonBringUpApi
    public void attachAMS(AMSProxy proxy) {
        mAmsProxy = proxy;
        XposedLog.boot("attachAMS, proxy: " + proxy);
    }

    @Override
    @CommonBringUpApi
    public void attachActiveServices(ActiveServicesProxy proxy) {
        mActiveServicesProxy = proxy;
        XposedLog.boot("attachActiveServices, proxy: " + proxy);
        XposedLog.boot("attachActiveServices, proxy: " + proxy.getHost());

        // workaroundForHwActiveServices(proxy);
    }

    private void workaroundForHwActiveServices(ActiveServicesProxy proxy) {
        try {
            XposedLog.wtf("workaroundForHwActiveServices??? " + proxy.getHost().getClass().getName());
            // Fix for HUAWEI
            String HWActiveServiceClassName = "com.android.server.am.HwActiveServices";
            if (proxy.getHost().getClass().getName().contains(HWActiveServiceClassName)) {
                XposedLog.wtf("workaroundForHwActiveServices!!!!!!!!!!");
                Object realService = XposedHelpers.callMethod(proxy.getHost(), "getInstance");
                XposedLog.wtf("workaroundForHwActiveServices, real one: " + realService);
                if (realService != null) {
                    proxy.setHost(realService);
                }
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail workaroundForHwActiveServices: " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void attachPowerManagerServices(PowerManagerServiceProxy proxy) {
        mPowerManagerServiceProxy = proxy;
        XposedLog.boot("attachPowerManagerServices, mPowerManagerServiceProxy: " + proxy);
    }

    @Override
    public void attachUsageStatsService(UsageStatsServiceProxy proxy) {
        mInactiveIdler = new InactiveAppIdler(proxy);
        mInactiveIdler.setListener(mOnAppIdleListener);
        XposedLog.boot("attachUsageStatsService, proxy: " + proxy);
        XposedLog.boot("attachUsageStatsService, idler: " + mInactiveIdler);
    }

    @Override
    public void attachDeviceIdleController(DeviceIdleControllerProxy proxy) {
        mDeviceIdleController = proxy;
        mPowerWhitelistBackend = PowerWhitelistBackend.getInstance(mDeviceIdleController);
        XposedLog.boot("mDeviceIdleController: " + proxy);
        XposedLog.boot("mPowerWhitelistBackend: " + mPowerWhitelistBackend);
    }

    @Override
    public void attachNotificationService(NotificationManagerServiceProxy proxy) {
        mNotificationService = proxy;
        XposedLog.boot("mNotificationService: " + proxy);
    }

    private PhoneWindowManagerProxy mPhoneWindowManagerProxy;

    @Override
    public void attachPhoneWindowManager(PhoneWindowManagerProxy proxy) {
        mPhoneWindowManagerProxy = proxy;
        XposedLog.boot("attachPhoneWindowManager: " + proxy);
    }

    private DevicePolicyManagerServiceProxy mDevicePolicyManagerService;

    @Override
    public void attachDevicePolicyManagerService(DevicePolicyManagerServiceProxy proxy) {
        mDevicePolicyManagerService = proxy;
        XposedLog.boot("attachDevicePolicyManagerService: " + proxy);
    }

    @Override
    @BinderCall
    public boolean hasNotificationForPackage(String pkg) {
        long iden = Binder.clearCallingIdentity();
        try {
            return hasNotificationForPackageInternal(pkg);
        } catch (Throwable e) {
            return false;
        } finally {
            Binder.restoreCallingIdentity(iden);
        }
    }

    @Override
    @BinderCall
    public int getAppLevel(String pkg) {
        if (SYSTEM_UID_APPS.contains(pkg)) {
            return XAshmanManager.AppLevel.SYSTEM_UID;
        }
        if (MEDIA_UID_APPS.contains(pkg)) {
            return XAshmanManager.AppLevel.MEDIA_UID;
        }
        if (PHONE_UID_APPS.contains(pkg)) {
            return XAshmanManager.AppLevel.PHONE_UID;
        }

        if (isWebviewProvider(pkg)) {
            return XAshmanManager.AppLevel.WEBVIEW_IMPL;
        }

        // Do not change this order.
        if (SYSTEM_APPS.contains(pkg)) {
            return XAshmanManager.AppLevel.SYSTEM;
        }
        return XAshmanManager.AppLevel.THIRD_PARTY;
    }

    @Override
    @BinderCall
    public String packageForTaskId(int taskId) {
        ComponentName c = mTaskIdMap.get(taskId);
        return c == null ? null : c.getPackageName();
    }

    private boolean hasNotificationForPackageInternal(String pkg) {
        if (mNotificationService == null) {
            XposedLog.wtf("hasNotificationForPackageInternal called when nms is null");
            return false;
        }
        ArrayList<StatusBarNotification> sbns = mNotificationService.getStatusBarNotifications();
        if (sbns == null || sbns.size() == 0) return false;

        if (BuildConfig.DEBUG) {
            for (StatusBarNotification sbn : sbns) {
                XposedLog.verbose("StatusBarNotification: " + sbn + ", from pkg: " + sbn.getPackageName());
            }
        }

        for (StatusBarNotification sbn : sbns) {
            if (pkg.equals(sbn.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    // No-null
    private Set<String> getStatusBarNotificationsPackagesInternal() {
        if (mNotificationService == null) {
            XposedLog.wtf("getStatusBarNotificationsPackagesInternal called when nms is null");
            return new HashSet<>(0);
        }
        ArrayList<StatusBarNotification> sbns = mNotificationService.getStatusBarNotifications();
        if (sbns == null || sbns.size() == 0) return new HashSet<>(0);

        if (BuildConfig.DEBUG) {
            for (StatusBarNotification sbn : sbns) {
                XposedLog.verbose("getStatusBarNotificationsPackagesInternal, StatusBarNotification: " + sbn + ", from pkg: " + sbn.getPackageName());
            }
        }

        Set<String> res = new HashSet<>();
        for (StatusBarNotification sbn : sbns) {
            res.add(sbn.getPackageName());
        }
        return res;
    }

    private void dumpNotifications() {
        XposedLog.wtf("------dumpNotifications START-------");
        ArrayList<StatusBarNotification> sbns = mNotificationService.getStatusBarNotifications();
        if (sbns == null || sbns.size() == 0) return;
        for (StatusBarNotification sbn : sbns) {
            XposedLog.verbose("StatusBarNotification: " + sbn + ", from pkg: " + sbn.getPackageName());
        }
        XposedLog.wtf("------dumpNotifications END-------");
    }

    // Go to doze mode.
    private void postEnterIdleMode(long delay) {
        XposedLog.verbose(XposedLog.PREFIX_DOZE + "postEnterIdleMode");

        // Check again, maybe called by doze stepper.
        boolean isDozeEnabled = isDozeEnabled();
        if (!isDozeEnabled) {
            XposedLog.wtf("postEnterIdleMode when doze is not enabled, ignore.");
            return;
        }

        if (mDozeHandler != null) {
            mDozeHandler.sendEmptyMessageDelayed(DozeHandlerMessages.MSG_ENTERIDLEMODE, delay);
        } else {
            XposedLog.wtf(XposedLog.PREFIX_DOZE + "postEnterIdleMode while handler is null");
        }
    }

    private void cancelEnterIdleModePosts(String reason) {
        XposedLog.verbose(XposedLog.PREFIX_DOZE + "cancelEnterIdleModePosts: " + reason);
        if (mDozeHandler != null) {
            mDozeHandler.removeMessages(DozeHandlerMessages.MSG_ENTERIDLEMODE);
        } else {
            XposedLog.wtf(XposedLog.PREFIX_DOZE + "cancelEnterIdleModePosts while handler is null");
        }

        if (XposedLog.isVerboseLoggable()) {
            boolean isIdleMode = DozeStateRetriever.isDeviceIdleMode(getContext());
            XposedLog.verbose(XposedLog.PREFIX_DOZE + "cancelEnterIdleModePosts, isDeviceIdleMode: " + isIdleMode);
        }
    }

    private void onDozeEnterStart() {
        // Save to history first.
        addToDozeHistory(mLastDozeEvent.duplicate());

        synchronized (mDozeLock) {
            mLastDozeEvent.setStartTimeMills(System.currentTimeMillis());
            mLastDozeEvent.setResult(DozeEvent.RESULT_PENDING);
            mLastDozeEvent.setFailCode(DozeEvent.FAIL_UNKNOWN);
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onDozeEnterStart: " + mLastDozeEvent);
        }
    }

    private void onDozeEnterSuccess() {
        synchronized (mDozeLock) {
            mLastDozeEvent.setEnterTimeMills(System.currentTimeMillis());
            mLastDozeEvent.setResult(DozeEvent.RESULT_SUCCESS);
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onDozeEnterSuccess: " + mLastDozeEvent);
        }
    }

    private void onDozeEnterFail(int failCode) {
        if (!isDozeSupported()) {
            XposedLog.wtf("onDozeEnterFail while doze not supported");
            return;
        }
        synchronized (mDozeLock) {
            mLastDozeEvent.setEnterTimeMills(System.currentTimeMillis());
            mLastDozeEvent.setResult(DozeEvent.RESULT_FAIL);
            mLastDozeEvent.setFailCode(failCode);
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onDozeEnterFail: " + mLastDozeEvent);
        }
    }

    private void postDozeEndCheck() {
        if (!isDozeSupported()) {
            XposedLog.wtf("postDozeEndCheck while doze not supported");
            return;
        }
        if (mDozeHandler != null) {
            mDozeHandler.sendEmptyMessageDelayed(DozeHandlerMessages.MSG_UPDATEDOZEENDSTATE, END_DOZE_CHECK_DELAY);
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("postDozeEndCheck: " + mLastDozeEvent);
        }
    }

    private void onDozeEnd() {
        if (!isDozeSupported()) {
            XposedLog.wtf("onDozeEnd while doze not supported");
            return;
        }
        synchronized (mDozeLock) {
            mLastDozeEvent.setEndTimeMills(System.currentTimeMillis());
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onDozeEnd: " + mLastDozeEvent);
        }
    }

    private void addToDozeHistory(DozeEvent event) {
        if (!isDozeSupported()) {
            XposedLog.wtf("addToDozeHistory while doze not supported");
            return;
        }
        synchronized (mDozeHistory) {
            checkDozeHistorySize();
            mDozeHistory.addFirst(event);
        }
    }

    // If history larger than MAX, remove last one.
    private void checkDozeHistorySize() {
        if (!isDozeSupported()) {
            XposedLog.wtf("checkDozeHistorySize while doze not supported");
            return;
        }
        synchronized (mDozeHistory) {
            int size = mDozeHistory.size();
            XposedLog.verbose("checkDozeHistorySize: " + size);
            if (size > MAX_DOZE_HISTORY_SIZE) {
                mDozeHistory.removeLast();
            }
        }
    }

    private void resetDozeEvent() {
        if (!isDozeSupported()) {
            XposedLog.wtf("resetDozeEvent while doze not supported");
            return;
        }
        XposedLog.verbose("resetDozeEvent");
        synchronized (mDozeLock) {
            mLastDozeEvent.setResult(DozeEvent.RESULT_UNKNOWN);
            mLastDozeEvent.setEnterTimeMills(-1);
            mLastDozeEvent.setStartTimeMills(-1);
            mLastDozeEvent.setEndTimeMills(-1);
            mLastDozeEvent.setFailCode(DozeEvent.FAIL_UNKNOWN);
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("resetDozeEvent: " + mLastDozeEvent);
        }
    }

    private static final String HOST_TYPE_ACTIVITY = "activity";
    private static final String HOST_TYPE_BROADCAST = "broadcast";
    private static final String HOST_TYPE_CONTENT_PROVIDER = "content provider";
    private static final String HOST_TYPE_SERVICE = "service";

    private static final Set<String> sProcessCheckType = Sets.newHashSet(HOST_TYPE_CONTENT_PROVIDER, HOST_TYPE_BROADCAST);

    @InternalCall
    @Override
    public boolean checkStartProcess(ApplicationInfo applicationInfo, String hostType, String hostName) {
        // Always allow for activity.
        if (!sProcessCheckType.contains(hostType)) {
            return true;
        }

        CheckResult checkResult = checkStartProcessDetailed(applicationInfo, hostType, hostName);

        logStartProcessEventToMemory(StartProcessEvent.builder()
                .allowed(checkResult.res)
                .caller(Binder.getCallingUid())
                .hostName(hostName)
                .hostType(hostType)
                .when(System.currentTimeMillis())
                .why(checkResult.why)
                .packageName(applicationInfo.packageName)
                .build());

        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("checkStartProcess: callingUid: %s %s %s %s %s",
                    Binder.getCallingUid(),
                    applicationInfo,
                    hostType,
                    hostName,
                    checkResult);
        }
        return checkResult.res;
    }

    private CheckResult checkStartProcessDetailed(ApplicationInfo applicationInfo, String hostType, String hostName) {
        String processPackage = applicationInfo.packageName;

        if (TextUtils.isEmpty(processPackage)) return CheckResult.BAD_ARGS;

        if (isInWhiteList(processPackage)) {
            return CheckResult.WHITE_LISTED;
        }

        boolean isSystemApp = isInSystemAppList(processPackage);
        if (isWhiteSysAppEnabled() && isSystemApp) {
            return CheckResult.SYSTEM_APP;
        }

        // Check GCM intent.
        if (GCMFCMHelper.isHandlingGcmIntent(processPackage)) {
            return CheckResult.HAS_GCM_INTENT;
        }

        if (PkgUtil.justBringDown(processPackage)) {
            return CheckResult.JUST_BRING_DOWN;
        }

        // Disabled case.
        if (!isStartBlockEnabled()) return CheckResult.SERVICE_CHECK_DISABLED;

        boolean isOnTop = isPackageRunningOnTop(processPackage);
        if (isOnTop) {
            return CheckResult.APP_RUNNING_TOP;
        }

        // If this app is not in good condition, and user choose to block:
        boolean blockedByUser = isPackageStartBlockByUser(processPackage);
        // User block!!!
        if (blockedByUser) {
            return CheckResult.USER_DENIED;
        }

        try {
            // By default, we allow.
            return CheckResult.ALLOWED_GENERAL;
        } finally {
            // Nothing.
        }
    }

    @Override
    @InternalCall
    public boolean checkService(Intent intent, ComponentName serviceComp, int callerUid) {
        if (serviceComp == null) return true;
        String appPkg = serviceComp.getPackageName();
        CheckResult res = checkServiceDetailed(intent, appPkg, serviceComp, callerUid);

        // Saving res record.
        logServiceBlockEventToMemory(ServiceEvent.builder()
                .service("Service")
                .why(res.why)
                .allowed(res.res)
                .appName(null)
                .pkg(appPkg)
                .why(res.getWhy())
                .callerUid(callerUid)
                .when(System.currentTimeMillis())
                .build());

        if (DEBUG_SERVICE) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verboseOn(
                        XposedLog.PREFIX_SERVICE + "checkService returning: " + res
                                + "target: " + appPkg
                                + ", comp: " + serviceComp
                                + ", caller: " + callerUid,
                        mLoggingService);
            }
        }
        return res.res;
    }

    @Override
    public boolean checkRestartService(String packageName, ComponentName componentName) throws RemoteException {

        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("checkRestartService: " + componentName + ", pkg: " + packageName);
        }

        if (TextUtils.isEmpty(packageName)) return true;
        if (isInWhiteList(packageName)) {
            XposedLog.verbose("checkRestartService: allow white");
            return true;
        }

        boolean isSystemApp = isInSystemAppList(packageName);
        if (isWhiteSysAppEnabled() && isSystemApp) {
            XposedLog.verbose("checkRestartService: allow system");
            return true;
        }

        // Check Op first for this package.
        int mode = getPermissionControlBlockModeForPkg(
                AppOpsManagerCompat.OP_START_SERVICE, packageName, true, new String[]{String.valueOf(componentName)});
        if (mode == AppOpsManagerCompat.MODE_IGNORED) {
            XposedLog.verbose("checkRestartService: deny op");
            return false;
        }

        // Check if in rf/lk list.
        if (isPackageLKByUser(packageName) && isKeyguard()) {
            XposedLog.verbose("checkRestartService: deny in lock screen and lk");
            return false;
        }

        if (isPackageRFKByUser(packageName)) {
            XposedLog.verbose("checkRestartService: deny in rfk list");
            return false;
        }

        if (PkgUtil.justBringDown(packageName)) {
            XposedLog.verbose("checkRestartService: deny just bring down");
            return false;
        }

        if (PkgUtil.isHomeApp(getContext(), packageName)) {
            XposedLog.verbose("checkRestartService: allow home");
            return true;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), packageName)) {
            XposedLog.verbose("checkRestartService: allow sms");
            return true;
        }

        if (PkgUtil.isAppRunning(getContext(), packageName, isSystemApp)) {
            XposedLog.verbose("checkRestartService: allow is running");
            return true;
        }

        if (isStartBlockEnabled() && isPackageStartBlockByUser(packageName)) {
            XposedLog.verbose("checkRestartService: deny start block");
            return false;
        }

        if (isBlockBlockEnabled() && isPackageBootBlockByUser(packageName)) {
            XposedLog.verbose("checkRestartService: deny boot block");
            return false;
        }

        return true;
    }

    private CheckResult checkServiceDetailed(Intent intent,
                                             String servicePkgName,
                                             ComponentName componentName,
                                             int callerUid) {
        if (TextUtils.isEmpty(servicePkgName)) return CheckResult.BAD_ARGS;

        if (isInWhiteList(servicePkgName)) {
            return CheckResult.WHITE_LISTED;
        }

        boolean isSystemApp = isInSystemAppList(servicePkgName);
        if (isWhiteSysAppEnabled() && isSystemApp) {
            return CheckResult.SYSTEM_APP;
        }

        // Check Op first for this package.
        String shortString = componentName.flattenToShortString();
        int mode = getPermissionControlBlockModeForPkg(
                AppOpsManagerCompat.OP_START_SERVICE, servicePkgName, true, new String[]{shortString});
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("checkService, get op mode for service: %s, mode: %s", shortString, mode);
        }
        if (mode == AppOpsManagerCompat.MODE_IGNORED) {
            return CheckResult.DENIED_OP_DENIED;
        }

        // First check the user rules.
        boolean checkRule = isStartRuleEnabled();
        if (checkRule) {
            @StartRuleCheck
            CheckResult ruleCheckRes = getStartCheckResultInRules(intent, callerUid, servicePkgName);
            if (ruleCheckRes != null) {
                return ruleCheckRes;
            }

            // Start rule enabled, may be has GCM intent.
            boolean hasGcmIntent = GCMFCMHelper.isHandlingGcmIntent(servicePkgName);
            if (hasGcmIntent) {
                return CheckResult.HAS_GCM_INTENT;
            }
        }

        if (PkgUtil.justBringDown(servicePkgName)) {
            return CheckResult.JUST_BRING_DOWN;
        }

        Integer serviceUidInt = mPackagesCache.get(servicePkgName);
        int serviceUid = serviceUidInt == null ? -1 : serviceUidInt;
        boolean isSameCaller = serviceUid == callerUid;

        // Early check.
        if (isSameCaller && PkgUtil.isSystemOrPhoneOrShell(serviceUid)) {
            return CheckResult.SAME_CALLER;
        }

        // Lazy but not running on top.
        // Retrieve imd top package ensure our top pkg correct.
        boolean isLazy = isLazyModeEnabled()
                && isPackageLazyByUser(servicePkgName);
        // Lazy, and not on top.
        // !servicePkgName.equals(mTopPackageImd.getData()).
        if (isLazy && !isPackageRunningOnTop(servicePkgName) && !isPackageRunningOnTopDelay(servicePkgName)) {
            @LazyRuleCheck
            boolean keepForLazy = isSameCaller && !confirmToStopLazyService(servicePkgName, componentName);
            return keepForLazy ? CheckResult.ALLOWED_LAZY_KEEPED : CheckResult.DENIED_LAZY;
        }

        // if (!isSystemReady()) return CheckResult.SYSTEM_NOT_READY;
        // Disabled case.
        if (!isStartBlockEnabled()) return CheckResult.SERVICE_CHECK_DISABLED;

        // Check if this is green app.
        boolean isGreeningApp = isGreeningEnabled()
                && isPackageGreeningByUser(servicePkgName);
        if (isGreeningApp) {
            return CheckResult.DENIED_GREEN_APP;
        }

        // Same app for system-core/media/phone is allowed.
        if (serviceUid == callerUid) {
            int appLevel = getAppLevel(servicePkgName);
            if (appLevel > XAshmanManager.AppLevel.SYSTEM) {
                return CheckResult.SAME_CALLER_CORE;
            }

            // Note. This is a workaround for MIUI.
            // We don't know why the path is 'THIS' to 'THIS' when
            // click a notification to launch the pending intent.
            // maybe MIPUSH?
            // Fk it.
            if (checkRule) {
                @StartRuleCheck
                boolean isAllowedThisToThis = RepoProxy.getProxy().getStart_rules().has(RULE_PATTERN_THIS_TO_THIS);
                if (isAllowedThisToThis) {
                    return CheckResult.SAME_CALLER_RULE;
                }
            }
        }

        boolean isOnTop = isPackageRunningOnTop(servicePkgName);
        if (isOnTop) {
            return CheckResult.APP_RUNNING_TOP;
        }

        if (PkgUtil.isAppRunning(getContext(), servicePkgName, isSystemApp)) {
            return CheckResult.APP_RUNNING;
        }

        // If this app is not in good condition, and user choose to block:
        boolean blockedByUser = isPackageStartBlockByUser(servicePkgName);
        // User block!!!
        if (blockedByUser) {
            return CheckResult.USER_DENIED;
        }

        try {
            // By default, we allow.
            return CheckResult.ALLOWED_GENERAL;
        } finally {
            // Nothing.
        }
    }

    private static final String[] RULE_PATTERN_THIS_TO_THIS = new String[]{
            "ALLOW THIS THIS"
    };

    private CheckResult getStartCheckResultInRules(Intent intent, int callerUid, String targetPackage) {
        String callerIdentify = PkgUtil.pkgForUid(getContext(), callerUid);
        return getStartCheckResultInRules(intent, callerIdentify, targetPackage);
    }

    private CheckResult getStartCheckResultInRules(Intent intent, String caller, String targetPackage) {
        if (caller == null || targetPackage == null) return null;
        XStopWatch stopWatch = null;
        if (BuildConfig.DEBUG) {
            stopWatch = XStopWatch.start("SERVICE START RULE CHECK, is GCM: " + GCMFCMHelper.isGcmOrFcmIntent(intent));
        }
        try {

            if (caller != null) {
                String[] patternAllow = constructStartAllowedRulePattern(intent, caller, targetPackage);
                boolean isThisCallerAllowedInRule = RepoProxy.getProxy().getStart_rules().has(patternAllow);
                if (BuildConfig.DEBUG) {
                    XposedLog.verbose("check rules patternAllow: " + Arrays.toString(patternAllow)
                            + ", has rule: " + isThisCallerAllowedInRule);
                }
                if (isThisCallerAllowedInRule) {
                    return CheckResult.ALLOWED_IN_RULE;
                }

                String[] patternDeny = constructStartDenyRulePattern(intent, caller, targetPackage);
                boolean isThisCallerDeniedInRule = RepoProxy.getProxy().getStart_rules().has(patternDeny);
                if (BuildConfig.DEBUG) {
                    XposedLog.verbose("check rules patternDeny: " + Arrays.toString(patternDeny)
                            + ", has rule: " + isThisCallerDeniedInRule);
                }
                if (isThisCallerDeniedInRule) {
                    return CheckResult.DENIED_IN_RULE;
                }
            }
        } finally {
            if (BuildConfig.DEBUG) {
                if (stopWatch != null) {
                    stopWatch.stop();
                }
            }
        }
        return null;
    }

    // Rule caches.
    private final Map<Pair<String, String>, String[]> mAllowRuleMap = new HashMap<>();
    private final Map<Pair<String, String>, String[]> mDenyRuleMap = new HashMap<>();

    private String[] getAllowRulesFromCache(String callerPackage, String targetPackage) {
        return mAllowRuleMap.get(new Pair<>(callerPackage, targetPackage));
    }

    private String[] getDenyRulesFromCache(String callerPackage, String targetPackage) {
        return mDenyRuleMap.get(new Pair<>(callerPackage, targetPackage));
    }

    private void addToAllowRulesCache(String callerPackage, String targetPackage, String[] rules) {
        mAllowRuleMap.put(new Pair<>(callerPackage, targetPackage), rules);
    }

    private void addToDenyRulesCache(String callerPackage, String targetPackage, String[] rules) {
        mDenyRuleMap.put(new Pair<>(callerPackage, targetPackage), rules);
    }

    // Example A B
    // ALLOW A B
    // ALLOW * B
    // ALLOW A *
    // ALLOW * *

    // ALLOW GCM *
    // ALLOW GCM B

    // ALLOW THIS THIS
    private String[] constructStartAllowedRulePattern(Intent intent, String callerPackage, String targetPackage) {
        String[] rules = getAllowRulesFromCache(callerPackage, targetPackage);
        if (rules == null) {
            boolean isCMIntent = GCMFCMHelper.isGcmOrFcmIntent(intent);

            if (BuildConfig.DEBUG) {
                XposedLog.verbose("constructStartAllowedRulePattern, GCM? " + isCMIntent
                                + ", intent: " + intent +
                                ", targetPackage: " + targetPackage,
                        ", callerPackage: " + callerPackage);
            }

            rules = new String[]{
                    String.format("ALLOW %s %s", callerPackage, targetPackage),
                    String.format("ALLOW * %s", targetPackage),
                    String.format("ALLOW %s *", callerPackage),
                    "ALLOW * *",
                    isCMIntent ? "ALLOW GCM *" : null,
                    isCMIntent ? String.format("ALLOW GCM %s", targetPackage) : null,
                    (targetPackage.equals(callerPackage)) ? "ALLOW THIS THIS" : null,
            };
            addToAllowRulesCache(callerPackage, targetPackage, rules);
        }
        return rules;
    }

    // Example A B
    // DENY A B
    // DENY * B
    // DENY A *

    // DENY GCM *
    // DENY GCM B
    private String[] constructStartDenyRulePattern(Intent intent, String callerPackage, String targetPackage) {
        String[] rules = getDenyRulesFromCache(callerPackage, targetPackage);
        if (rules == null) {
            boolean isCMIntent = GCMFCMHelper.isGcmOrFcmIntent(intent);

            if (BuildConfig.DEBUG) {
                XposedLog.verbose("constructStartDenyRulePattern, GCM? " + isCMIntent + ", intent: " + intent);
            }

            rules = new String[]{
                    String.format("DENY %s %s", callerPackage, targetPackage),
                    "DENY * " + targetPackage,
                    "DENY " + callerPackage + " *",
                    isCMIntent ? "DENY GCM *" : null,
                    isCMIntent ? String.format("DENY GCM %s", targetPackage) : null,

            };
            addToDenyRulesCache(callerPackage, targetPackage, rules);
        }
        return rules;
    }

    @Override
    @InternalCall
    public boolean checkBroadcast(Intent intent, int receiverUid, int callerUid) {
        CheckResult res = checkBroadcastDetailed(intent, receiverUid, callerUid);
        // Saving res record.
        logBroadcastBlockEventToMemory(
                BroadcastEvent.builder()
                        .action(intent.getAction())
                        .allowed(res.res)
                        .why(res.getWhy())
                        .appName(null)
                        .receiver(receiverUid)
                        .caller(callerUid)
                        .when(System.currentTimeMillis())
                        .why(res.why)
                        .build());

        if (DEBUG_BROADCAST) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verboseOn(
                        XposedLog.PREFIX_BROADCAST + "checkBroadcast returning: "
                                + res + " for: "
                                + PkgUtil.pkgForUid(getContext(), receiverUid)
                                + " receiverUid: " + receiverUid
                                + " callerUid: " + callerUid
                                + " action: " + intent
                                + " comp: " + intent.getComponent()
                                + ", caller: " + PkgUtil.pkgForUid(getContext(), callerUid),
                        mLoggingService);
            }
        }
        return res.res;
    }

    @Override
    public boolean checkBroadcastDeliver(Intent intent, String callerPackage, int callingPid, int callingUid) {
        if (DEBUG_BROADCAST) {
            XposedLog.verbose("checkBroadcastDeliver: " + intent);
        }
        return true;
    }

    @Override
    @InternalCall
    public boolean checkComponentSetting(ComponentName componentName, int newState,
                                         int flags, int callingUid) {

        if (DEBUG_COMP && XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("checkComponentSetting: " + componentName
                    + ", calling uid: " + callingUid
                    + ", state: " + newState);
        }

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
            if (DEBUG_COMP && XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("It is not enable state, allow component setting.");
            }
            return true;
        }

        if (isInWhiteList(pkgName)) {
            if (DEBUG_COMP && XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("It is from white list, allow component setting.");
            }
            return true;
        }

        if (isWhiteSysAppEnabled() && isInSystemAppList(pkgName)) {
            if (DEBUG_COMP && XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("It is from system app list, allow component setting.");
            }
            return true;
        }

        if (callingUid == sClientUID || callingUid <= 1000
                || callingUid == android.os.Process.myUid()) {
            // Do not block system settings.
            if (DEBUG_COMP && XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("It is us or the system, allow component setting.");
            }
            return true;
        }

        if (!isCompSettingBlockEnabledEnabled()) {
            if (DEBUG_COMP && XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("Block is not enabled, allow component setting.");
            }
            return true;
        }

        if (RepoProxy.getProxy().getComps().has(componentName.flattenToString())) {
            if (DEBUG_COMP && XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("Block component setting.");
            }
            return false;
        }

        // It is not disabled by us, allow.
        if (DEBUG_COMP && XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("It is not disabled by us, allow.");
        }
        return true;
    }

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Override
    @InternalCall
    @Deprecated
    @DeprecatedSince("3.0.2")
    public void onActivityDestroy(Intent intent, String reason) {
        // Nothing to do.
    }

    @Override
    @InternalCall
    @CommonBringUpApi
    public boolean onKeyEvent(KeyEvent keyEvent, String source) {
        mAppGuardService.onKeyEvent(keyEvent, source);

        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("source: " + source + ", onKeyEvent: " + keyEvent);
        }

        if (source.equals(InputManagerInjectInputSubModule.EVENT_SOURCE)) {
            return false;
        }

        if (keyEvent != null) {
            mLazyHandler.obtainMessage(AshManLZHandlerMessages.MSG_ONKEYEVENT, keyEvent).sendToTarget();
        }
        return false;
    }

    private KeyguardManager mKeyguardManager;

    private KeyguardManager getKeyguardManager() {
        if (mKeyguardManager == null) {
            mKeyguardManager = (KeyguardManager) getContext().getSystemService(KEYGUARD_SERVICE);
        }
        return mKeyguardManager;
    }

    private boolean isKeyguard() {
        KeyguardManager keyguardManager = getKeyguardManager();
        return keyguardManager != null && keyguardManager.inKeyguardRestrictedInputMode();
    }

    private boolean shouldRFKPackage(String pkg) {
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

        return !PkgUtil.isDefaultSmsApp(getContext(), pkg);
    }

    private boolean shouldTRKPackage(String pkg) {
        // If this app is not in good condition, but user
        // does not block, we also allow it to start.
        boolean trkByUser = isPackageTRKByUser(pkg);
        if (!trkByUser) {
            return false;
        }

        if (isInWhiteList(pkg)) {
            return false;
        }

        if (isWhiteSysAppEnabled() && isInSystemAppList(pkg)) {
            return false;
        }

        return !PkgUtil.isDefaultSmsApp(getContext(), pkg);
    }

    @Override
    @BinderCall
    public List<BlockRecord2> getBlockRecords() {
        enforceCallingPermissions();
        synchronized (mBlockRecords) {
            return Lists.newArrayList(mBlockRecords.values());
        }
    }

    @Override
    @BinderCall
    public void clearBlockRecords() {
        enforceCallingPermissions();
        mainHandler.removeMessages(AshManHandlerMessages.MSG_CLEARBLOCKRECORDS);
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_CLEARBLOCKRECORDS).sendToTarget();
    }

    @Override
    @BinderCall
    public void setComponentEnabledSetting(ComponentName componentName, int newState, int flags) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETCOMPONENTENABLEDSETTING,
                newState, flags, componentName).sendToTarget();
    }

    @Override
    @BinderCall
    public int getComponentEnabledSetting(ComponentName componentName) {
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
    public int getApplicationEnabledSetting(final String packageName) {
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
    public void setApplicationEnabledSetting(String packageName, int newState, int flags, boolean tmp) {
        enforceCallingPermissions();
        Pair<String, Boolean> extra = new Pair<>(packageName, tmp);
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETAPPLICATIONENABLEDSETTING, newState, flags, extra).sendToTarget();
    }

    @Override
    public void watch(IAshmanWatcher w) {
        enforceCallingPermissions();
        Preconditions.checkNotNull(w, "IAshmanWatcher is null");
        AshManHandler.WatcherClient watcherClient = new AshManHandler.WatcherClient(w);
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_WATCH, watcherClient).sendToTarget();
    }

    @Override
    @BinderCall
    public void unWatch(IAshmanWatcher w) {
        enforceCallingPermissions();
        Preconditions.checkNotNull(w, "IAshmanWatcher is null");
        AshManHandler.WatcherClient watcherClient = new AshManHandler.WatcherClient(w);
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_UNWATCH, watcherClient).sendToTarget();
    }

    @Override
    @BinderCall
    public void setNetworkPolicyUidPolicy(int uid, int policy) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETNETWORKPOLICYUIDPOLICY, uid, policy).sendToTarget();
    }

    @Override
    public void restart() {
        enforceCallingPermissions();
        mLazyHandler.post(new Runnable() {
            @Override
            public void run() {
                Zygote.execShell("reboot"); //FIXME Change to soft reboot?
            }
        });
    }

    @Override
    @BinderCall
    public void setCompSettingBlockEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETCOMPSETTINGBLOCKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isCompSettingBlockEnabledEnabled() {
        enforceCallingPermissions();
        return mCompSettingBlockEnabled.get();
    }

    private static String[] convertObjectArrayToStringArray(Object[] objArr) {
        return ArrayUtil.convertObjectArrayToStringArray(objArr);
    }

    @Override
    public String[] getWhiteListApps(int filterOptions) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getWhiteListApps: " + filterOptions);
        enforceCallingPermissions();
        Object[] data = WHITE_LIST.toArray(); // FIXME, no sync protect?
        return convertObjectArrayToStringArray(data);
    }

    @Override
    public String[] getInstalledApps(int filterOptions) {
        Collection<String> packages = mPackagesCache.keySet();
        if (packages.size() == 0) {
            return new String[0];
        }

        List<String> outList = Lists.newArrayList();
        outList.addAll(packages);

        final boolean showSystem = filterOptions == XAshmanManager.FLAG_SHOW_SYSTEM_APP
                || filterOptions == XAshmanManager.FLAG_SHOW_SYSTEM_APP_WITHOUT_CORE_APP;
        final boolean withoutCore = filterOptions == XAshmanManager.FLAG_SHOW_SYSTEM_APP_WITHOUT_CORE_APP;
        final List<String> filtered = Lists.newArrayList();
        Collections.consumeRemaining(outList, new Consumer<String>() {
            @Override
            public void accept(String s) {
                if (!showSystem && (isInSystemAppList(s) || isInWhiteList(s))) return;
                if (withoutCore && isInWhiteList(s)) return;
                filtered.add(s);
            }
        });
        return convertObjectArrayToStringArray(filtered.toArray());
    }

    private void addOrRemoveFromRepo(String[] packages, SetRepo<String> repo, boolean add) {
        long id = Binder.clearCallingIdentity();
        try {
            for (String p : packages) {
                if (add) repo.add(p);
                else repo.remove(p);
            }
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public String[] getBootBlockApps(boolean block) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getBootBlockApps: " + block);
        enforceCallingPermissions();
        if (!block) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());

            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (outList.contains(s)) {
                        XposedLog.verbose("// Kik dup package: " + s);
                        return;// Kik dup package.
                    }
                    if (isPackageBootBlockByUser(s)) {
                        XposedLog.verbose("// Kik blocked package: " + s);
                        return;
                    }
                    if (isInWhiteList(s)) {
                        XposedLog.verbose("// Kik white package: " + s);
                        return;
                    }
                    if (isWhiteSysAppEnabled() && isInSystemAppList(s)) {
                        XposedLog.verbose("// Kik system package: " + s);
                        return;
                    }
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Set<String> packages = RepoProxy.getProxy().getBoots().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    if (isWhiteSysAppEnabled() && isInSystemAppList(p)) {
                        return;
                    }
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    public void addOrRemoveBootBlockApps(String[] packages, int op) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveBootBlockApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        addOrRemoveFromRepo(packages, RepoProxy.getProxy().getBoots(), op == XAshmanManager.Op.ADD);
    }

    @Override
    public String[] getStartBlockApps(boolean block) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getStartBlockApps: " + block);
        enforceCallingPermissions();
        if (!block) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (outList.contains(s)) return;// Kik dup package.
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
            Set<String> packages = RepoProxy.getProxy().getStarts().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    if (isWhiteSysAppEnabled() && isInSystemAppList(p)) {
                        return;
                    }
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    public void addOrRemoveStartBlockApps(String[] packages, int op) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveStartBlockApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        addOrRemoveFromRepo(packages, RepoProxy.getProxy().getStarts(), op == XAshmanManager.Op.ADD);
    }

    @Override
    public String[] getLKApps(boolean kill) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getLKApps: " + kill);
        enforceCallingPermissions();
        if (!kill) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, s -> {
                if (outList.contains(s)) return;// Kik dup package.
                if (isPackageLKByUser(s)) return;
                if (isInWhiteList(s)) return;
                if (isWhiteSysAppEnabled() && isInSystemAppList(s)) return;
                outList.add(s);
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Set<String> packages = RepoProxy.getProxy().getLks().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, p -> {
                if (isWhiteSysAppEnabled() && isInSystemAppList(p)) {
                    return;
                }
                noSys.add(p);
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    public void addOrRemoveLKApps(String[] packages, int op) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveLKApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        addOrRemoveFromRepo(packages, RepoProxy.getProxy().getLks(), op == XAshmanManager.Op.ADD);
        if (op == XAshmanManager.Op.REMOVE) {
            removeFromRunningProcessPackages(packages);
        }
    }

    @Override
    public String[] getRFKApps(boolean kill) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getRFKApps: " + kill);
        enforceCallingPermissions();
        if (!kill) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (outList.contains(s)) return;// Kik dup package.
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
            Set<String> packages = RepoProxy.getProxy().getRfks().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    if (isWhiteSysAppEnabled() && isInSystemAppList(p)) {
                        return;
                    }
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    public void addOrRemoveRFKApps(String[] packages, int op) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveRFKApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        addOrRemoveFromRepo(packages, RepoProxy.getProxy().getRfks(), op == XAshmanManager.Op.ADD);
    }

    @Override
    public String[] getGreeningApps(boolean greening) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getGreeningApps: " + greening);
        enforceCallingPermissions();
        if (!greening) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (outList.contains(s)) return;// Kik dup package.
                    if (isPackageGreeningByUser(s)) return;
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
            Set<String> packages = RepoProxy.getProxy().getGreens().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    if (isWhiteSysAppEnabled() && isInSystemAppList(p)) {
                        return;
                    }
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    public void addOrRemoveGreeningApps(String[] packages, int op) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveGreeningApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        addOrRemoveFromRepo(packages, RepoProxy.getProxy().getGreens(), op == XAshmanManager.Op.ADD);
    }

    @Override
    @BinderCall(restrict = "any")
    public boolean isPackageGreening(String packageName) {
        if (packageName == null) return false;
        long id = Binder.clearCallingIdentity();
        try {
            if (!isGreeningEnabled()) return false;

            if (isInSystemAppList(packageName)) return false;
            if (isWhiteSysAppEnabled() && isInSystemAppList(packageName)) return false;
            return RepoProxy.getProxy().getGreens().has(packageName);
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    @BinderCall(restrict = "any")
    public boolean isUidGreening(int uid) {
        if (PkgUtil.isSystemOrPhoneOrShell(uid)) return false;

        long id = Binder.clearCallingIdentity();
        try {

            if (!isGreeningEnabled()) return false;

            // FIXME Too slow.
            String packageName = PkgUtil.pkgForUid(getContext(), uid);
            if (packageName == null) return false;
            if (isInSystemAppList(packageName)) return false;
            if (isWhiteSysAppEnabled() && isInSystemAppList(packageName)) return false;

            return RepoProxy.getProxy().getGreens().has(packageName);
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    @BinderCall
    @Deprecated
    public void unInstallPackage(final String pkg, final IPackageUninstallCallback callback) {
        enforceCallingPermissions();
    }

    @Override
    public boolean isLockKillDoNotKillAudioEnabled() {
        enforceCallingPermissions();
        return mLockKillDoNotKillAudioEnabled.get();
    }

    @Override
    public void setLockKillDoNotKillAudioEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLDONOTKILLAUDIOENABLED, enabled)
                .sendToTarget();
    }

    @Override
    public int getControlMode() {
        enforceCallingPermissions();
        return mControlMode.get();
    }

    @Override
    public void setControlMode(int mode) {
        if (mode != XAshmanManager.ControlMode.BLACK_LIST && mode != XAshmanManager.ControlMode.WHITE_LIST) {
            throw new IllegalArgumentException("Bad mode:" + mode);
        }
        enforceCallingPermissions();

        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETCONTROLMODE, mode).sendToTarget();
    }

    @Override
    public String getBuildSerial() {
        return BuildFingerprintBuildHostInfo.BUILD_FINGER_PRINT;
    }

    @Override
    @BinderCall(restrict = "any")
    public boolean isAutoAddBlackEnabled() {
        return mAutoAddToBlackListForNewApp.get();
    }

    @Override
    @BinderCall(restrict = "any")
    public boolean isAutoAddBlackNotificationEnabled() {
        return mAutoAddNotificationToBlackListForNewApp.get();
    }

    @Override
    public void setAutoAddBlackNotificationEnabled(boolean value) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETAUTOADDBLACKNOTIFICATIONENABLED, value)
                .sendToTarget();
    }

    @Override
    public boolean isOptFeatureEnabled(String tag) {
        return SettingsProvider.get().getBoolean("OPT_FEATURE_" + tag, false);
    }

    @Override
    public void setOptFeatureEnabled(String tag, boolean enable) {
        SettingsProvider.get().putBoolean("OPT_FEATURE_" + tag, enable);
    }

    @Override
    public int getRecentTaskExcludeSetting(ComponentName c) {
        if (c == null) return XAshmanManager.ExcludeRecentSetting.NONE;
        return SettingsProvider.get().getInt("RECENT_EXCLUDE_" + c.getPackageName(), XAshmanManager.ExcludeRecentSetting.NONE);
    }

    @Override
    @InternalCall
    public void onStartProcessLocked(ApplicationInfo applicationInfo) {
        mLazyHandler.obtainMessage(AshManLZHandlerMessages.MSG_ONSTARTPROCESSLOCKED, applicationInfo)
                .sendToTarget();
    }

    @Override
    @InternalCall
    public void onRemoveProcessLocked(ApplicationInfo applicationInfo,
                                      boolean callerWillRestart,
                                      boolean allowRestart,
                                      String reason) {
        TypePack typePack = TypePack.builder()
                .o1(applicationInfo)
                .boolean1(callerWillRestart)
                .boolean2(allowRestart)
                .s1(reason)
                .build();
        mLazyHandler.obtainMessage(AshManLZHandlerMessages.MSG_ONREMOVEPROCESSLOCKED, typePack)
                .sendToTarget();
    }

    @Override
    @InternalCall
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("NotificationListeners onNotificationPosted: " + sbn);

            boolean isLightScreenEnabled = isWakeupOnNotificationEnabled();
            if (isLightScreenEnabled) {
                PowerManager pm = (PowerManager) getContext().getSystemService(POWER_SERVICE);
                if (pm != null && !pm.isInteractive()) {
                    pm.wakeUp(SystemClock.uptimeMillis());
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("NotificationListeners onNotificationRemoved: " + sbn);
        }

        // Check for lazy.
        if (sbn != null) {
            String packageName = sbn.getPackageName();
            postLazyServiceKillerIfNecessary(packageName, LAZY_KILL_SERVICE_NOTIFICATION_INTERVAL, "Nofification-Removed");
        }
    }

    private SystemGesturesPointerEventListener mSystemGesturesPointerEventListener;

    @Override
    public void onInputEvent(Object arg) {
        if (false && mSystemGesturesPointerEventListener != null && arg instanceof MotionEvent) {
            MotionEvent me = (MotionEvent) arg;
            mSystemGesturesPointerEventListener.onPointerEvent(me);
        }
    }

    @Override
    @BinderCall
    public void onSourceApkFileDetected(String path, String apkPackageName) {
        PackageInstallerManager.from(getContext()).onSourceApkFileDetected(path, apkPackageName);
    }

    @Override
    @BinderCall(restrict = "any")
    public String getCurrentTopPackage() {
        return mTopPackageImd.getData();
    }

    @Override
    @BinderCall(restrict = "any")
    public void registerTaskRemoveListener(ITaskRemoveListener listener) {
        if (getContext() != null) {
            PackageStateManager.from(getContext())
                    .registerTaskRemoveListener(listener);
        }
    }

    @Override
    @BinderCall(restrict = "any")
    public void unRegisterTaskRemoveListener(ITaskRemoveListener listener) {
        if (getContext() != null) {
            PackageStateManager.from(getContext())
                    .unRegisterTaskRemoveListener(listener);
        }
    }

    @Override
    @InternalCall
    public boolean checkInstallApk(Object argsFrom) {
        return checkInstallApkInternal(argsFrom);
    }

    private boolean checkInstallApkInternal(Object argsFrom) {
        if (!OSUtil.isMOrAbove()) { // Not supported.
            return true;
        }
        // Enabled?
        boolean featureEnabled = isPackageInstallVerifyEnabled();
        if (!featureEnabled) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "checkInstallApk: feature not enabled" + argsFrom);
            return true;
        }

        PackageInstallerManager.VerifyArgs verifyArgs = parseInstallArgs(argsFrom);
        if (verifyArgs == null) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "checkInstallApk: VerifyArgs is null");
            return true;
        }

        Holder<Boolean> resultHolder = new Holder<>();
        resultHolder.setData(true); // Default, allow install.

        PackageInstallerManager.VerifyReceiver receiver = (reason, mode) -> {
            resultHolder.setData(mode == AppOpsManagerCompat.MODE_ALLOWED);
            XposedLog.verbose(XposedLog.PREFIX_PM + "VerifyReceiver reason: " + reason);
        };

        PackageInstallerManager pm = PackageInstallerManager.from(getContext());
        pm.verifyIncomingInstallRequest(verifyArgs, receiver, mainHandler);

        XposedLog.verbose(XposedLog.PREFIX_PM + "get verify result: " + resultHolder.getData());

        return resultHolder.getData();
    }

    private PackageInstallerManager.VerifyArgs parseInstallArgs(Object argsFrom) {
        InstallArgsProxy argsProxy = new InstallArgsProxy(argsFrom);
        Object originObject = argsProxy.getOriginInfoObject();
        OriginInfoProxy originInfoProxy = new OriginInfoProxy(originObject);

        File apkFile = argsProxy.getTmpPackageFile();
        if (apkFile == null) {
            apkFile = originInfoProxy.getFile();
        }

        XposedLog.verbose(XposedLog.PREFIX_PM
                + "checkInstallApk: " + argsFrom
                + ", installer: " + argsProxy.getInstallerPackageName()
                + ", origin: " + originObject
                + ", file: " + apkFile);

        if (apkFile == null) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "checkInstallApk: apkFile is null");
            return null; // Bad package, skip check.
        }

        boolean canRead = apkFile.canRead();
        XposedLog.verbose(XposedLog.PREFIX_PM + "apkFile canRead:  " + canRead);

        PackageParser.Package parsed = PkgUtil.getPackageInfo(apkFile);
        XposedLog.verbose(XposedLog.PREFIX_PM + "getPackageInfo: " + parsed);
        if (parsed == null) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "checkInstallApk: parsed is null");
            return null; // Bad package, skip check.
        }

        PackageInfo packageInfo = PackageParser.generatePackageInfo(parsed, null, 0, 0, 0, null,
                new PackageUserState());

        XposedLog.verbose(XposedLog.PREFIX_PM + "generatePackageInfo: " + packageInfo);

        if (packageInfo == null) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "checkInstallApk: packageInfo is null");
            return null; // Bad package, skip check.
        }

        return InstallerUtil.generateVerifyArgs(getContext(),
                packageInfo,
                apkFile.getAbsolutePath(),
                argsProxy.getInstallerPackageName());
    }

    @Override
    public void setRecentTaskExcludeSetting(ComponentName c, int setting) {
        SettingsProvider.get().putInt("RECENT_EXCLUDE_" + c.getPackageName(), setting);
    }

    @Override
    public int getAppConfigOverlayIntSetting(String appPackageName, String tag) {
        return SettingsProvider.get().getInt("CONFIG_OVERLAY_" + tag + "_" + appPackageName,
                XAshmanManager.ConfigOverlays.NONE);// Invalid.
    }

    @Override
    public void setAppConfigOverlayIntSetting(String appPackageName, String tag, int value) {
        SettingsProvider.get().putInt("CONFIG_OVERLAY_" + tag + "_" + appPackageName, value);
    }

    @Override
    public void injectPowerEvent() {
        mainHandler.post(new ErrorCatchRunnable(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) getContext().getSystemService(POWER_SERVICE);
                powerManager.goToSleep(SystemClock.uptimeMillis());
            }
        }, "goToSleep"));
    }

    @Override
    public void setAutoAddBlackEnable(boolean enable) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETAUTOADDBLACKENABLE, enable)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public void forceReloadPackages() {
        mainHandler.removeMessages(AshManHandlerMessages.MSG_FORCERELOADPACKAGES);
        mainHandler.sendEmptyMessage(AshManHandlerMessages.MSG_FORCERELOADPACKAGES);
    }

    private CheckResult checkBroadcastDetailed(Intent action, int receiverUid, int callerUid) {

        // Check if this is a boot complete action.
        if (isBootCompleteBroadcastAction(action.getAction())) {
            return checkBootCompleteBroadcast(receiverUid, callerUid);
        }

        // if (!isSystemReady()) return CheckResult.SYSTEM_NOT_READY;

        // Disabled case.
        if (!isStartBlockEnabled()) return CheckResult.BROADCAST_CHECK_DISABLED;

        // Broadcast from/to same app is allowed.
        if (callerUid == receiverUid && PkgUtil.isSystemOrPhoneOrShell(callerUid)) {
            return CheckResult.SAME_CALLER;
        }

        if (callerUid == receiverUid) {
            // Note. This is a workaround for MIUI.
            // We don't know why the path is 'THIS' to 'THIS' when
            // click a notification to launch the pending intent.
            // maybe MIPUSH?
            // Fk it.
            if (isStartRuleEnabled()) {
                @StartRuleCheck
                boolean isAllowedThisToThis = RepoProxy.getProxy().getStart_rules().has(RULE_PATTERN_THIS_TO_THIS);
                if (isAllowedThisToThis) {
                    return CheckResult.SAME_CALLER_RULE;
                }
            }
        }

        String receiverPkgName = PkgUtil.pkgForUid(getContext(), receiverUid);
        if (TextUtils.isEmpty(receiverPkgName)) return CheckResult.BAD_ARGS;

        return checkBroadcastDetailed(action, receiverPkgName, PkgUtil.pkgForUid(getContext(), callerUid));
    }

    private CheckResult checkBroadcastDetailed(Intent intent,
                                               String receiverPkgName,
                                               String callerPackageName) {

        if (isInWhiteList(receiverPkgName)) {
            return CheckResult.WHITE_LISTED;
        }

        // First check the user rules.
        boolean checkRule = isStartRuleEnabled();
        if (checkRule) {
            @StartRuleCheck
            CheckResult ruleCheckRes = getStartCheckResultInRules(intent, callerPackageName, receiverPkgName);
            if (ruleCheckRes != null) {
                return ruleCheckRes;
            } else {
                // May be has GCM intent?
                boolean hasGcmIntent = GCMFCMHelper.isHandlingGcmIntent(receiverPkgName);
                if (hasGcmIntent) {
                    return CheckResult.HAS_GCM_INTENT;
                }
            }
        }

        boolean isOnTop = isPackageRunningOnTop(receiverPkgName);
        if (isOnTop) {
            return CheckResult.APP_RUNNING_TOP;
        }

        // Lazy but not running on top.
        // Retrieve imd top package ensure our top pkg correct.
        boolean isLazy = isLazyModeEnabled()
                && isPackageLazyByUser(receiverPkgName);
        // receiverPkgName.equals(mTopPackageImd.getData()).
        if (isLazy && !isPackageRunningOnTop(receiverPkgName) && !isPackageRunningOnTopDelay(receiverPkgName)) {
            return CheckResult.DENIED_LAZY;
        }

        boolean isSystemApp = isInSystemAppList(receiverPkgName);
        if (isWhiteSysAppEnabled() && isSystemApp) {
            return CheckResult.SYSTEM_APP;
        }

        if (PkgUtil.isDefaultSmsApp(getContext(), receiverPkgName)) {
            return CheckResult.SMS_APP;
        }

        if (PkgUtil.justBringDown(receiverPkgName)) {
            return CheckResult.JUST_BRING_DOWN;
        }

        if (PkgUtil.isAppRunning(getContext(), receiverPkgName, isSystemApp)) {
            return CheckResult.APP_RUNNING;
        }

        // It is in user black list.
        boolean blockByUser = isPackageStartBlockByUser(receiverPkgName);
        if (blockByUser) {
            return CheckResult.USER_DENIED;
        }
        return CheckResult.ALLOWED_GENERAL;
    }

    private boolean isInStringRepo(SetRepo<String> repo, String pkg) {
        return repo.has(pkg);
    }

    private boolean isPackageBootBlockByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getBoots(), pkg);
    }

    private boolean isPackageStartBlockByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getStarts(), pkg);
    }

    private boolean isPackageResidentByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getResident(), pkg);
    }

    private boolean isPackageprivacyByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getPrivacy(), pkg);
    }

    private boolean isPackageLKByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getLks(), pkg);
    }

    private boolean isPackageRFKByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getRfks(), pkg);
    }

    private boolean isPackageTRKByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getTrks(), pkg);
    }

    private boolean isPackageLazyByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getLazy(), pkg);
    }

    private boolean isPackagePropApplyByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getProps(), pkg);
    }

    private boolean isPackageGreeningByUser(String pkg) {
        return isInStringRepo(RepoProxy.getProxy().getGreens(), pkg);
    }

    private CheckResult checkBootCompleteBroadcast(int receiverUid, int callerUid) {

        // Disabled case.
        if (!isBlockBlockEnabled()) return CheckResult.BOOT_CHECK_DISABLED;

        // FIXME Too Slow.
        String receiverPkgName = PkgUtil.pkgForUid(getContext(), receiverUid);

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
        return CheckResult.ALLOWED_GENERAL;
    }

    private static boolean isBootCompleteBroadcastAction(String action) {
        return Intent.ACTION_BOOT_COMPLETED.equals(action);
    }

    private void logStartProcessEventToMemory(StartProcessEvent startProcessEvent) {
        if (isPowerSaveModeEnabled()) {
            return;
        }
        Runnable r = () -> {
            String callerPkg =
                    PkgUtil.isSystemOrPhoneOrShell(startProcessEvent.caller)
                            ? "android"
                            : PkgUtil.pkgForUid(getContext(), startProcessEvent.caller);
            BlockRecord2 old = getBlockRecord(startProcessEvent.packageName);
            long blockedTimes = old == null ? 0 : old.getHowManyTimesBlocked();
            long allowedTimes = old == null ? 0 : old.getHowManyTimesAllowed();

            BlockRecord2 blockRecord2 = BlockRecord2.builder()
                    .pkgName(startProcessEvent.packageName)
                    .callerPkgName(callerPkg)
                    .howManyTimesBlocked(startProcessEvent.allowed ? blockedTimes : blockedTimes + 1)
                    .howManyTimesAllowed(startProcessEvent.allowed ? allowedTimes + 1 : allowedTimes)
                    .reason(startProcessEvent.why)
                    .timeWhen(System.currentTimeMillis())
                    .block(!startProcessEvent.allowed)
                    .type(blockRecordTypeFromHostType(startProcessEvent.hostType))
                    .build();

            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("PROCESS BlockRecord2: " + blockRecord2);
            }
            addBlockRecord(blockRecord2);

            // Add to cache.
            mStartRecordCache.addStartRecordForPackage(startProcessEvent.packageName, blockRecord2);
        };

        mLoggingService.execute(new ErrorCatchRunnable(r, "logStartProcessEventToMemory"));

        mainHandler.obtainMessage(AshManHandlerMessages.MSG_NOTIFYSTARTBLOCK, startProcessEvent.packageName).sendToTarget();
    }

    private static int blockRecordTypeFromHostType(String hostType) {
        if (HOST_TYPE_BROADCAST.equals(hostType)) return BlockRecord2.TYPE_BROADCAST;
        if (HOST_TYPE_ACTIVITY.equals(hostType)) return BlockRecord2.TYPE_ACTIVITY;
        if (HOST_TYPE_SERVICE.equals(hostType)) return BlockRecord2.TYPE_SERVICE;
        if (HOST_TYPE_CONTENT_PROVIDER.equals(hostType)) return BlockRecord2.TYPE_CONTENT_PROVIDER;
        return BlockRecord2.TYPE_UNKNOWN;
    }

    private void logServiceBlockEventToMemory(final ServiceEvent serviceEvent) {
        if (isPowerSaveModeEnabled()) {
            return;
        }
        Runnable r = () -> {
            String callerPkg =
                    PkgUtil.isSystemOrPhoneOrShell(serviceEvent.callerUid)
                            ? "android"
                            : PkgUtil.pkgForUid(getContext(), serviceEvent.callerUid);
            BlockRecord2 old = getBlockRecord(serviceEvent.pkg);
            long blockedTimes = old == null ? 0 : old.getHowManyTimesBlocked();
            long allowedTimes = old == null ? 0 : old.getHowManyTimesAllowed();
            BlockRecord2 blockRecord2 = BlockRecord2.builder()
                    .pkgName(serviceEvent.pkg)
                    .callerPkgName(callerPkg)
                    .appName(null)
                    .howManyTimesBlocked(serviceEvent.allowed ? blockedTimes : blockedTimes + 1)
                    .howManyTimesAllowed(serviceEvent.allowed ? allowedTimes + 1 : allowedTimes)
                    .reason(serviceEvent.why)
                    .timeWhen(System.currentTimeMillis())
                    .block(!serviceEvent.allowed)
                    .type(BlockRecord2.TYPE_SERVICE)
                    .build();
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("SVC BlockRecord2: " + blockRecord2);
            }
            addBlockRecord(blockRecord2);

            // Add to cache.
            mStartRecordCache.addStartRecordForPackage(serviceEvent.pkg, blockRecord2);
        };

        mLoggingService.execute(new ErrorCatchRunnable(r, "logServiceBlockEventToMemory"));

        mainHandler.obtainMessage(AshManHandlerMessages.MSG_NOTIFYSTARTBLOCK, serviceEvent.getPkg()).sendToTarget();
    }

    private void logBroadcastBlockEventToMemory(final BroadcastEvent broadcastEvent) {
        if (isPowerSaveModeEnabled()) {
            return;
        }
        Runnable r = () -> {
            String receiverPkgName =
                    PkgUtil.pkgForUid(getContext(), broadcastEvent.receiver);
            if (receiverPkgName == null) {
                receiverPkgName = PkgUtil.pkgForUid(getContext(), broadcastEvent.receiver);
                if (receiverPkgName == null) return;
            }

            String callerPkg =
                    PkgUtil.isSystemOrPhoneOrShell(broadcastEvent.caller)
                            ? "android"
                            : PkgUtil.pkgForUid(getContext(), broadcastEvent.caller);

            mainHandler.obtainMessage(AshManHandlerMessages.MSG_NOTIFYSTARTBLOCK, receiverPkgName).sendToTarget();

            BlockRecord2 old = getBlockRecord(receiverPkgName);
            long blockedTimes = old == null ? 0 : old.getHowManyTimesBlocked();
            long allowedTimes = old == null ? 0 : old.getHowManyTimesAllowed();
            BlockRecord2 blockRecord2 = BlockRecord2.builder()
                    .pkgName(receiverPkgName)
                    .appName(null)
                    .callerPkgName(callerPkg)
                    .howManyTimesBlocked(broadcastEvent.allowed ? blockedTimes : blockedTimes + 1)
                    .howManyTimesAllowed(broadcastEvent.allowed ? allowedTimes + 1 : allowedTimes)
                    .reason(broadcastEvent.why)
                    .timeWhen(System.currentTimeMillis())
                    .block(!broadcastEvent.allowed)
                    .type(BlockRecord2.TYPE_BROADCAST)
                    .build();
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("BRD BlockRecord2: " + blockRecord2);
            }
            addBlockRecord(blockRecord2);

            // Add to cache.
            mStartRecordCache.addStartRecordForPackage(receiverPkgName, blockRecord2);
        };
        mLoggingService.execute(new ErrorCatchRunnable(r, "logBroadcastBlockEventToMemory"));
    }

    private void logOpEventToMemory(final String pkg, final int op, final int mode, final String[] payload) {
        Runnable r = () -> mOpsCache.logPackageOp(op, mode, pkg, payload);
        mLoggingService.execute(new ErrorCatchRunnable(r, "logOpEventToMemory"));
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        getContext().registerReceiver(mScreenReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");
        getContext().registerReceiver(mPackageReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_SWITCHED);
        getContext().registerReceiver(mUserReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        getContext().registerReceiver(mBatteryStateReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_CLEAR_PROCESS);
        getContext().registerReceiver(mClearProcessBroadcast, intentFilter);

        // This is a test.
        // FIMXE THIS IS FUCKING DANGEROUS FOR USER. BE CAREFUL.
        if (BuildConfig.DEBUG) {
            boolean hasErrorIndicator = RepoProxy.hasFileIndicator("mock_system_err");
            if (hasErrorIndicator) {
                intentFilter = new IntentFilter();
                intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                getContext().registerReceiver(mTestProtectedBroadcastReceiver, intentFilter);
                getContext().registerReceiver(mTestSystemErrorBroadcastReceiver, intentFilter);
            }
        }
    }

    private void inflateWhiteList() {
        String[] whiteListArr = new AppResource(getContext()).readStringArrayFromAPMApp("default_ash_white_list_packages");
        XposedLog.debug("Res default_ash_white_list_packages: " + Arrays.toString(whiteListArr));
        Collections.consumeRemaining(whiteListArr, s -> {
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
        });

        String[] lockWhiteListArr = new AppResource(getContext()).readStringArrayFromAPMApp("app_lock_white_list_activity");
        XposedLog.debug("Res app_lock_white_list_activity: " + Arrays.toString(lockWhiteListArr));
        addAppLockWhiteListActivity(lockWhiteListArr);
    }

    private void inflateWhiteListHook() {
        String[] whiteListArr = new AppResource(getContext()).readStringArrayFromAPMApp("ash_white_list_packages_hooks");
        XposedLog.debug("Res ash_white_list_packages_hooks: " + Arrays.toString(whiteListArr));
        Collections.consumeRemaining(whiteListArr, XAshmanServiceImpl::addToWhiteListHook);
    }


    @Override
    @CommonBringUpApi
    public void attachContext(Context context) {
        super.attachContext(context);
        mAppGuardService.attachContext(context);
    }

    @Override
    @CommonBringUpApi
    public void publish() {
        try {
            String serviceName = XAshmanManager.SERVICE_NAME;
            XposedLog.boot("publishing ash to: " + serviceName);
            ServiceManager.addService(serviceName, asBinder());
        } catch (Throwable e) {
            XposedLog.debug("*** FATAL*** Fail publish our svc:" + e);
        }
        construct();

        mAppGuardService.publish();
    }

    @Override
    @CommonBringUpApi
    public void systemReady() {
        XposedLog.wtf("systemReady@" + getClass().getSimpleName());

        mAppGuardService.systemReady();

        inflateWhiteList();
        inflateWhiteListHook();
        // Update system ready, since we can call providers now.
        mIsSystemReady = true;
        checkSafeMode();
        registerReceiver();

        // Dump build vars.
        //noinspection unchecked
        Collections.consumeRemaining(XAppBuildVar.BUILD_VARS, o -> XposedLog.wtf("BUILD_VARS: " + o));

        // Try to setup the list after 15s if network control is enabled.
        if (XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_FIREWALL)) {
            mLazyHandler.postDelayed(() -> {
                try {
                    applyRestrictionBlackList();
                } catch (Throwable ignored) {
                }
            }, 10 * 1000);
        }

        // Reload packages after 15s, for those apps installed on sd.
        mLazyHandler.postDelayed(new ErrorCatchRunnable(() -> {
            forceReloadPackages();

            // It is safe.
            if (OSUtil.isHuaWeiDevice()) {
                applyDozeWhiteList();
            }
        }, "reload installed apps"), 15 * 1000);

        mLazyHandler.postDelayed(new ErrorCatchRunnable(() -> {
            mIsNotificationPostReady = true;
        }, "Ready to post notifications"), 3 * 1000);

        cacheWebviewPackacgaes();

        // Disable layout debug incase our logic make the system dead in loop.
        if (BuildConfig.DEBUG) {
            SystemProperties.set(View.DEBUG_LAYOUT_PROPERTY, String.valueOf(false));

            MultipleAppsManager multipleAppsManager = MultipleAppsManager.getInstance();
            multipleAppsManager.onCreate(getContext());
        }
    }

    private void applyDozeWhiteList() {
        XposedLog.verbose("applyDozeWhiteList: " + mDeviceIdleController);
        try {
            if (mDeviceIdleController == null) return;
            Set<String> adding = RepoProxy.getProxy().getDoze_whitelist_adding().getAll();
            for (String add : adding) {
                mDeviceIdleController.addPowerSaveWhitelistAppInternal(add);
            }
            Set<String> removing = RepoProxy.getProxy().getDoze_whitelist_removal().getAll();
            for (String r : removing) {
                mDeviceIdleController.removePowerSaveWhitelistAppInternal(r);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail applyDozeWhiteList: " + Log.getStackTraceString(e));
        }
    }

    private final Set<String> mWebviewProviders = new HashSet<>();

    private boolean isWebviewProvider(String pkg) {
        return mWebviewProviders.contains(pkg);
    }

    private void cacheWebviewPackacgaes() {
        try {
            IWebViewUpdateService w = IWebViewUpdateService.Stub.asInterface(ServiceManager
                    .getService("webviewupdate"));
            WebViewProviderInfo[] providerInfos = w.getValidWebViewPackages();
            if (providerInfos == null || providerInfos.length == 0) {
                XposedLog.wtf("No webview providers found.");
                return;
            }

            for (WebViewProviderInfo info : providerInfos) {
                String pkgName = info.packageName;
                XposedLog.boot("Add webview provider: " + pkgName + ", description: " + info.description);

                mWebviewProviders.add(pkgName);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail cacheWebviewPackacgaes: " + Log.getStackTraceString(e));
        }
    }

    private final Set<String> mGCMSupportPackages = new HashSet<>();

    @Override
    public boolean isGCMSupportPackage(String pkg) {
        enforceCallingPermissions();
        return mGCMSupportPackages.contains(pkg);
    }

    @Override
    public boolean isShowAppProcessUpdateNotificationEnabled() {
        return mShowAppProcessUpdateNotification.get();
    }

    @Override
    public void setShowAppProcessUpdateNotificationEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETSHOWAPPPROCESSUPDATENOTIFICATIONENABLED, enabled).sendToTarget();
        if (!enabled) {
            mLazyHandler.post(new ErrorCatchRunnable(this::clearRunningAppProcessUpdateNotification, "clearRunningAppProcessUpdateNotification"));
        }
    }

    @Override
    public boolean isStartRuleEnabled() {
        return mStartRuleEnabled.get();
    }

    @Override
    public void setStartRuleEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETSTARTRULEENABLED, enabled).sendToTarget();
    }

    private static final String PUSH_MESSAGE_HANDLER_PKG_ENABLE_STATE_TAG = "pmh_enabled_";
    private static final String PUSH_MESSAGE_HANDLER_ENABLE_TAG = "pmh_enabled";
    private static final String PUSH_MESSAGE_HANDLER_SHOW_CONTENT_TAG = "pmh_show_content_";
    private static final String PUSH_MESSAGE_HANDLER_SOUND_TAG = "pmh_sound_";
    private static final String PUSH_MESSAGE_HANDLER_VIBRATE_TAG = "pmh_vibrate_";
    private static final String PUSH_MESSAGE_HANDLER_NOTIFICATION_BY_APP = "pmh_notification_by_app_";

    private static String getPushMessageHandlerPkgEnableStateKeyForPackage(String pkg) {
        return PUSH_MESSAGE_HANDLER_PKG_ENABLE_STATE_TAG + pkg;
    }

    private static String getPushMessageHandlerShowContentKeyForPackage(String pkg) {
        return PUSH_MESSAGE_HANDLER_SHOW_CONTENT_TAG + pkg;
    }

    private static String getPushMessageHandlerSoundKeyForPackage(String pkg) {
        return PUSH_MESSAGE_HANDLER_SOUND_TAG + pkg;
    }

    private static String getPushMessageHandlerVibrateKeyForPackage(String pkg) {
        return PUSH_MESSAGE_HANDLER_VIBRATE_TAG + pkg;
    }

    private static String getPushMessageHandlerNotificationPostByAppKeyForPackage(String pkg) {
        return PUSH_MESSAGE_HANDLER_NOTIFICATION_BY_APP + pkg;
    }

    @Override
    @BinderCall
    public boolean isPushMessageHandlerEnabled(String pkg) {
        if (pkg == null) return false;
        return SettingsProvider.get().getBoolean(getPushMessageHandlerPkgEnableStateKeyForPackage(pkg), false);
    }

    @Override
    @BinderCall
    public void setPushMessageHandlerEnabled(String pkg, boolean enabled) {
        SettingsProvider.get().putBoolean(getPushMessageHandlerPkgEnableStateKeyForPackage(pkg), enabled);
        notifyPushMessageHandlerSettingsChanged(pkg);
    }

    @Override
    @BinderCall
    public boolean isPushMessageHandlerShowContentEnabled(String pkg) {
        return SettingsProvider.get().getBoolean(getPushMessageHandlerShowContentKeyForPackage(pkg), false);
    }

    @Override
    @BinderCall
    public void setPushMessageHandlerShowContentEnabled(String pkg, boolean enabled) {
        SettingsProvider.get().putBoolean(getPushMessageHandlerShowContentKeyForPackage(pkg), enabled);
        notifyPushMessageHandlerSettingsChanged(pkg);
    }

    @Override
    public boolean isPushMessageHandlerNotificationSoundEnabled(String pkg) {
        return SettingsProvider.get().getBoolean(getPushMessageHandlerSoundKeyForPackage(pkg), false);
    }

    @Override
    public void setPushMessageHandlerNotificationSoundEnabled(String pkg, boolean enabled) {
        SettingsProvider.get().putBoolean(getPushMessageHandlerSoundKeyForPackage(pkg), enabled);
        notifyPushMessageHandlerSettingsChanged(pkg);
    }

    @Override
    public boolean isPushMessageHandlerNotificationVibrateEnabled(String pkg) {
        return SettingsProvider.get().getBoolean(getPushMessageHandlerVibrateKeyForPackage(pkg), false);
    }

    @Override
    public void setPushMessageHandlerNotificationVibrateEnabled(String pkg, boolean enabled) {
        SettingsProvider.get().putBoolean(getPushMessageHandlerVibrateKeyForPackage(pkg), enabled);
        notifyPushMessageHandlerSettingsChanged(pkg);
    }

    @Override
    public boolean isPushMessageHandlerMessageNotificationByAppEnabled(String pkg) {
        return SettingsProvider.get().getBoolean(getPushMessageHandlerNotificationPostByAppKeyForPackage(pkg), false);
    }

    @Override
    public void setPushMessageHandlerMessageNotificationByAppEnabled(String pkg, boolean enabled) {
        SettingsProvider.get().putBoolean(getPushMessageHandlerNotificationPostByAppKeyForPackage(pkg), enabled);
        notifyPushMessageHandlerSettingsChanged(pkg);
    }

    private AtomicBoolean mIsPushMessageHandleEnabled = new AtomicBoolean(false);

    @Override
    @BinderCall
    public boolean isPushMessageHandleEnabled() {
        return mIsPushMessageHandleEnabled.get();
    }

    @Override
    @BinderCall
    public void setPushMessageHandleEnabled(boolean enabled) {
        SettingsProvider.get().putBoolean(PUSH_MESSAGE_HANDLER_ENABLE_TAG, enabled);
        mIsPushMessageHandleEnabled.set(enabled);
        notifyPushMessageHandlerSettingsChanged(null);
    }

    @Override
    public boolean isHandlingPushMessageIntent(String packageName) {
        enforceCallingPermissions();
        return GCMFCMHelper.isHandlingGcmIntent(packageName);
    }

    @Override
    @BinderCall(restrict = "any")
    public boolean showToast(String message) {
        mLazyHandler.post(new ErrorCatchRunnable(() -> Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show(), "showToast  @BinderCall"));
        return true;
    }

    @Override
    public List<BlockRecord2> getStartRecordsForPackage(String pkg) {
        enforceCallingPermissions();
        return mStartRecordCache.getStartRecordsForPackage(pkg);
    }

    @Override
    public void clearStartRecordsForPackage(String pkg) {
        enforceCallingPermissions();
        mStartRecordCache.clearStartRecordsForPackage(pkg);
    }

    @Override
    public boolean isWakeupOnNotificationEnabled() {
        enforceCallingPermissions();
        return mWakeupOnNotificationPosted.get();
    }

    @Override
    public void setWakeupOnNotificationEnabled(boolean enable) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETWAKEUPONNOTIFICATIONENABLED, enable).sendToTarget();
    }

    @Override
    public boolean addOrRemoveLazyRules(String rule, boolean add) {
        XposedLog.verbose("addOrRemoveLazyRules: " + rule + ", " + add);
        RuleParser p = RuleParser.Factory.newParser();
        Rule r = p.parse(rule);
        XposedLog.verbose("addOrRemoveLazyRules: " + r);
        if (r == null) return false;
        String rulePattern = r.toInternalPattern();
        if (add) {
            RepoProxy.getProxy().getLazy_rules().add(rulePattern);
            return true;
        } else {
            return RepoProxy.getProxy().getLazy_rules().remove(rulePattern);
        }
    }

    @Override
    public String[] getLazyRules() {
        enforceCallingPermissions();
        return convertObjectArrayToStringArray(RepoProxy.getProxy().getLazy_rules().getAll().toArray());
    }

    @Override
    public boolean isLazyRuleEnabled() {
        enforceCallingPermissions();
        return mLazyRuleEnabled.get();
    }

    @Override
    public void setLazyRuleEnabled(boolean enable) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETLAZYRULEENABLED, enable).sendToTarget();
    }

    @Override
    public void createMultipleProfile() {
        enforceCallingPermissions();
        enforceDebugBuild();
        wrapCallingIdetUnCaught(() -> MultipleAppsManager.getInstance().createMultipleProfileIfNeed());
    }

    @Override
    public boolean installAppToMultipleAppsUser(String pkgName) {
        enforceCallingPermissions();
        enforceDebugBuild();
        wrapCallingIdetUnCaught(() -> MultipleAppsManager.getInstance().installAppToMultipleAppsUser(pkgName));
        return true;
    }

    @Override
    public void startActivityAsUser(Intent intent, int userId) {
        enforceCallingPermissions();
        enforceDebugBuild();
        wrapCallingIdetUnCaught(() -> getContext().startActivityAsUser(intent, UserHandle.of(userId)));
    }

    @Override
    public void launchMultipleAppsForPackage(String packageName) {
        enforceCallingPermissions();
        enforceDebugBuild();
        wrapCallingIdetUnCaught(() -> {
            PackageManager pm = getContext().getPackageManager();
            Intent launcher = pm.getLaunchIntentForPackage(packageName);
        });
    }

    @Override
    public void mockPushMessageReceived(String pkg, String message) {
        enforceCallingPermissions();

        mLazyHandler.post(new ErrorCatchRunnable(() -> {
            // Find target and invoke.
            for (PushNotificationHandler ph : mPushNotificationHandlers) {
                if (ph.getTargetPackageName().equals(pkg)) {
                    Intent dummy = new Intent();
                    dummy.putExtra(PushNotificationHandler.KEY_MOCK_MESSAGE, message);
                    ph.handleIncomingIntent(pkg, dummy);
                }
            }
        }, "mockPushMessageReceived"));
    }

    // App service controler.

    private AppServiceController mAppServiceController = new AppServiceController();

    @Override
    @BinderCall(restrict = "any")
    public void registerController(IServiceControl control) {
        new ErrorCatchRunnable(() -> mAppServiceController.registerController(control), "mAppServiceController registerController").run();
    }

    @Override
    @BinderCall(restrict = "any")
    public void unRegisterController(IServiceControl control) {
        new ErrorCatchRunnable(() -> mAppServiceController.unRegisterController(control), "mAppServiceController unRegisterController").run();
    }

    @Override
    @BinderCall(restrict = "any")
    public void stopService(Intent serviceIntent) {
        mainHandler.post(new ErrorCatchRunnable(() -> stopServiceInternal(serviceIntent), "(Binder call)stopService"));
    }

    @Override
    public void setAppServiceLazyControlSolution(int solutionFlags, boolean enable) {
        enforceCallingPermissions();
        XposedLog.verbose("LAZY setAppServiceLazyControlSolution: " + XAshmanManager.AppServiceControlSolutions.decode(solutionFlags));
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETAPPSERVICELAZYCONTROLSOLUTION, solutionFlags, solutionFlags, enable).sendToTarget();
    }

    @Override
    public boolean isAppServiceLazyControlSolutionEnable(int solutionFlags) {
        enforceCallingPermissions();
        if (solutionFlags == XAshmanManager.AppServiceControlSolutions.FLAG_APP) {
            return mLazySolutionApp.get();
        }
        if (solutionFlags == XAshmanManager.AppServiceControlSolutions.FLAG_FW) {
            return mLazySolutionFW.get();
        }
        return false;
    }

    @Override
    @BinderCall
    public void forceIdlePackages(String[] packages) {
        enforceCallingPermissions();
        XposedLog.verbose("forceIdlePackages: " + Arrays.toString(packages));
        wrapCallingIdetUnCaught(new ErrorCatchRunnable(() -> {
            for (String p : packages) {
                getAppIdler().setAppIdle(p);
            }
        }, "forceStopPackages"));
    }

    private static final String SYSTEM_PROP_ENABLED = "system_prop_enabled";
    private AtomicBoolean mIsSystemPropEnabled = new AtomicBoolean(false);

    @Override
    public boolean isSystemPropEnabled() {
        enforceCallingPermissions();
        return mIsSystemPropEnabled.get();
    }

    @Override
    public void setSystemPropEnabled(boolean enabled) {
        enforceCallingPermissions();
        SettingsProvider.get().putBoolean(SYSTEM_PROP_ENABLED, enabled);
        mIsSystemPropEnabled.set(enabled);
    }

    @Override
    @BinderCall
    public void addOrRemoveSystemPropProfile(SystemPropProfile profile, boolean add) {
        enforceCallingPermissions();
        XposedLog.verbose("addOrRemoveSystemPropProfile: " + profile);
        if (validateSystemPropProfile(profile)) {
            String id = profile.getProfileId();
            if (add) {
                String js = profile.toJson();
                if (js != null) {
                    RepoProxy.getProxy().getSystemPropProfiles().put(id, js);
                }
            } else {
                RepoProxy.getProxy().getSystemPropProfiles().remove(id);
            }
        }
    }

    private static boolean validateSystemPropProfile(SystemPropProfile profile) {
        return profile != null && profile.getProfileId() != null && profile.getSystemProp() != null;
    }

    @Override
    @BinderCall
    public Map getSystemPropProfiles() {
        enforceCallingPermissions();
        return RepoProxy.getProxy().getSystemPropProfiles().dup();
    }

    @Override
    public void setActiveSystemPropProfileId(String profileId) {

    }

    @Override
    public String getActiveSystemPropProfileId() {
        return null;
    }

    @Override
    public SystemPropProfile getActiveSystemPropProfile() {
        return null;
    }

    @Override
    public void addOrRemoveSystemPropProfileApplyApps(String[] pkgs, boolean add) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveSystemPropProfileApplyApps: "
                    + Arrays.toString(pkgs));
        if (pkgs == null || pkgs.length == 0) return;
        enforceCallingPermissions();
        long id = Binder.clearCallingIdentity();
        try {
            for (String p : pkgs) {
                if (add) {
                    // Check if this is 3-rd app.
                    int appLevel = getAppLevel(p);
                    if (appLevel == XAshmanManager.AppLevel.THIRD_PARTY) {
                        RepoProxy.getProxy().getProps().add(p);
                    } else {
                        XposedLog.wtf("addOrRemoveSystemPropProfileApplyApps skip for no-3rd app: " + p);
                    }
                } else {
                    RepoProxy.getProxy().getProps().remove(p);
                }
            }
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public String[] getSystemPropProfileApplyApps(boolean apply) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("getSystemPropProfileApplyApps: " + apply);
        enforceCallingPermissions();
        if (!apply) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr,
                    s -> {
                        if (outList.contains(s)) return;// Kik dup package.
                        if (isPackagePropApplyByUser(s)) return;
                        if (isInWhiteList(s)) return;
                        if (isInSystemAppList(s)) return; // Only for 3-rd.
                        outList.add(s);
                    });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Set<String> packages = RepoProxy.getProxy().getProps().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, p -> {
                if (isInSystemAppList(p)) {
                    return;
                }
                noSys.add(p);
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    @BinderCall(restrict = "any")
    public boolean isSystemPropProfileApplyApp(String packageName) {
        int appLevel = getAppLevel(packageName);
        // 3-rd and in list.
        return appLevel == XAshmanManager.AppLevel.THIRD_PARTY
                && isPackagePropApplyByUser(packageName);
    }

    @Override
    public boolean isPackageInstallVerifyEnabled() {
        enforceCallingPermissions();
        return isOptFeatureEnabled("package_install_verify");
    }

    @Override
    public void setPackageInstallVerifyEnabled(boolean enabled) {
        enforceCallingPermissions();
        setOptFeatureEnabled("package_install_verify", enabled);
    }

    @Override
    public String[] getPackageInstallerVerifyRules() {
        enforceCallingPermissions();
        return convertObjectArrayToStringArray(RepoProxy.getProxy().getPm_rules().getAll().toArray());
    }

    @Override
    public boolean addOrRemovePackageInstallerVerifyRules(String rule, boolean add) {
        XposedLog.verbose("addOrRemovePackageInstallerVerifyRules: " + rule + ", " + add);
        RuleParser p = RuleParser.Factory.newParser();
        Rule r = p.parse(rule);
        XposedLog.verbose("addOrRemovePackageInstallerVerifyRules: " + r);
        if (r == null) return false;
        String rulePattern = r.toInternalPattern();
        if (add) {
            RepoProxy.getProxy().getPm_rules().add(rulePattern);
            return true;
        } else {
            return RepoProxy.getProxy().getPm_rules().remove(rulePattern);
        }
    }

    private void stopServiceInternal(Intent serviceIntent) {
        if (serviceIntent != null && mActiveServicesProxy != null) {
            List<Object> records = mActiveServicesProxy.getServiceRecords(Binder.getCallingUid(), serviceIntent);
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("stopServiceInternal, records service " + Arrays.toString(records.toArray()));
            }
            for (Object o : records) {
                mActiveServicesProxy.stopServiceLocked(o);
            }
        }
    }

    private void notifyPushMessageHandlerSettingsChanged(String pkg) {
        mLazyHandler.post(new ErrorCatchRunnable(() -> {
            for (PushNotificationHandler h : mPushNotificationHandlers) {
                h.onSettingsChanged(pkg);
            }
        }, "notifyPushMessageHandlerSettingsChanged"));
    }

    private void cacheGCMPackages() {
        XposedLog.verbose("cacheGCMPackages...");
        try {
            if (getContext() == null) return;
            List<ResolveInfo> list = getContext().getPackageManager()
                    .queryBroadcastReceivers(
                            new Intent(GCMFCMHelper.ACTION_FCM),
                            0);
            if (list != null) {
                for (ResolveInfo r : list) {
                    if (BuildConfig.DEBUG) {
                        XposedLog.verbose("cacheGCMPackages-FCM: r: " + r);
                    }

                    String pkg = r.activityInfo == null ? null : r.activityInfo.packageName;
                    if (pkg != null) {
                        mGCMSupportPackages.add(pkg);
                        XposedLog.verbose("cacheGCMPackages-FCM: " + pkg);
                    } else {
                        XposedLog.verbose("cacheGCMPackages-FCM, pkg is null: " + r);
                    }
                }
            }
            list = getContext().getPackageManager()
                    .queryBroadcastReceivers(
                            new Intent(GCMFCMHelper.ACTION_GCM),
                            0);
            if (list != null) {
                for (ResolveInfo r : list) {

                    if (BuildConfig.DEBUG) {
                        XposedLog.verbose("cacheGCMPackages-GCM: r: " + r);
                    }

                    String pkg = r.activityInfo == null ? null : r.activityInfo.packageName;
                    if (pkg != null) {
                        mGCMSupportPackages.add(pkg);
                        XposedLog.verbose("cacheGCMPackages-GCM: " + pkg);
                    } else {
                        XposedLog.verbose("cacheGCMPackages-GCM, pkg is null: " + r);
                    }
                }
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail cacheGCMPackages: " + Log.getStackTraceString(e));
        }
    }

    // NMS API START.
    private NativeDaemonConnector mNativeDaemonConnector;

    private String mDataInterfaceName, mWifiInterfaceName;

    private BroadcastReceiver mPendingDataRestrictReceiver;

    private SparseBooleanArray mPendingRestrictOnData = new SparseBooleanArray();

    private final Object mQuotaLock = new Object();

    private SetRepo<String> mWifiBlackList, mDataBlackList;

    private void initDataAndWifiRestrictionBlackList() {
        mWifiBlackList = RepoProxy.getProxy().getWifi_restrict();
        mDataBlackList = RepoProxy.getProxy().getData_restrict();
    }

    private void applyRestrictionBlackList() {
        synchronized (mQuotaLock) {
            String[] allWifi = convertObjectArrayToStringArray(mWifiBlackList.getAll().toArray());

            for (String l : allWifi) {
                NetworkRestriction n = NetworkRestriction.from(l);
                int key = n.getUid();
                boolean value = n.getRestrictPolicy() != POLICY_REJECT_NONE;
                restrictAppOnWifiForce(key, value);
            }

            String[] allData = convertObjectArrayToStringArray(mDataBlackList.getAll().toArray());

            for (String l : allData) {
                NetworkRestriction n = NetworkRestriction.from(l);
                int key = n.getUid();
                boolean value = n.getRestrictPolicy() != POLICY_REJECT_NONE;
                restrictAppOnDataForce(key, value);
            }
        }
    }

    @Override
    @InternalCall
    public void onNetWorkManagementServiceReady(NativeDaemonConnector connector) {
        XposedLog.debug("NMS onNetWorkManagementServiceReady: " + connector);

        if (!XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_FIREWALL)) {
            XposedLog.wtf("onNetWorkManagementServiceReady, " +
                    "What the fuck? the firewall is not enabled at this build, but we got it up?");
            return;
        }

        this.mNativeDaemonConnector = connector;
        this.mWifiInterfaceName = SystemProperties.get("wifi.interface");
        XposedLog.debug("NMS mWifiInterfaceName: " + mWifiInterfaceName);

        initDataInterface();

        initDataAndWifiRestrictionBlackList();

        applyRestrictionBlackList();

        // Note: processPendingDataRestrictRequests() will unregister
        // mPendingDataRestrictReceiver once it has been able to determine
        // the cellular network interface name.
        mPendingDataRestrictReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (!XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_FIREWALL)) {
                    XposedLog.wtf("onReceive, What the fuck? the firewall is not enabled at this build, but we got it up?");
                    return;
                }

                try {
                    applyRestrictionBlackList();
                    processPendingDataRestrictRequests();
                } catch (Exception e) {
                    XposedLog.wtf(Log.getStackTraceString(e));
                }
            }
        };
        final IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getContext().registerReceiver(mPendingDataRestrictReceiver, filter);
    }

    @Override
    @InternalCall
    public void onRequestAudioFocus(int type, int res, int callingUid, String callingPkg) {
        // FIXME Too slow
        String pkgName = PkgUtil.pkgForUid(getContext(), callingUid);
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onRequestAudioFocus: " + pkgName + " ,uid: " + callingUid
                    + ", type: " + type + ", res: " + res);
        }
        if (pkgName == null) return;

        if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            return;
        }

        // Only protect music, movie, speech.
        if (type != AudioAttributes.CONTENT_TYPE_MOVIE
                && type != AudioAttributes.CONTENT_TYPE_MUSIC
                && type != AudioAttributes.CONTENT_TYPE_SPEECH) {
            return;
        }

        mainHandler.obtainMessage(AshManHandlerMessages.MSG_ONAUDIOFOCUSEDPACKAGECHANGED, pkgName).sendToTarget();
    }

    @Override
    @InternalCall
    public void onAbandonAudioFocus(int res, int callingUid, String callingPkg) {
        if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            return;
        }
        // FIXME Too slow
        String pkgName = PkgUtil.pkgForUid(getContext(), callingUid);
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("onAbandonAudioFocus: " + callingPkg + "--" + callingUid);
        }
        if (pkgName == null) return;
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_ONAUDIOFOCUSEDPACKAGEABANDONED, pkgName).sendToTarget();
    }

    @Override
    public void setPermissionControlEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETPERMISSIONCONTROLENABLED, enabled).sendToTarget();
    }

    @Override
    public boolean isPermissionControlEnabled() {
        enforceCallingPermissions();
        // return mPermissionControlEnabled.get();
        // FIXME.
        return true;
    }

    @BinderCall
    @Override
    public int getPermissionControlBlockModeForPkg(int code, String pkg, boolean log, String[] payload) {
        int mode = getPermissionControlBlockModeForPkgInternal(code, pkg);
        if (log) {
            logOperationIfNecessary(code, Integer.MAX_VALUE, pkg, null, mode, payload);
        }
        return mode;
    }

    private int getPermissionControlBlockModeForPkgInternal(int code, String pkg) {
        if (DEBUG_OP && XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("getPermissionControlBlockModeForPkg code %s pkg %s", code, pkg);
        }

        if (isInWhiteList(pkg)) {
            if (DEBUG_OP) {
                XposedLog.verbose("getPermissionControlBlockModeForPkg white listed");
            }
            return AppOpsManagerCompat.MODE_ALLOWED;
        }

        long id = Binder.clearCallingIdentity();
        String pattern = constructPatternForPermission(code, pkg);
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("getPermissionControlBlockModeForPkg pattern %s", pattern);
        }
        try {
            if (isInPermissionBlockList(pattern)) return AppOpsManagerCompat.MODE_IGNORED;
        } catch (Throwable e) {
            XposedLog.wtf("Error getPermissionControlBlockModeForPkg: " + Log.getStackTraceString(e));
            return AppOpsManagerCompat.MODE_ALLOWED;
        } finally {
            Binder.restoreCallingIdentity(id);
        }

        return AppOpsManagerCompat.MODE_ALLOWED;
    }

    @Override
    @BinderCall(restrict = "any")
    public int getPermissionControlBlockModeForUid(int code, int uid, boolean log, String[] payload) {
        if (DEBUG_OP) {
            XposedLog.verbose("getPermissionControlBlockModeForUid code %s pkg %s", code, uid);
        }
        // FIXME Too slow.
        String pkg = PkgUtil.pkgForUid(getContext(), uid);
        if (pkg == null) {
            return AppOpsManagerCompat.MODE_ALLOWED;
        }
        return getPermissionControlBlockModeForPkg(code, pkg, log, payload);
    }

    @Override
    public void setPermissionControlBlockModeForPkg(int code, String pkg, int mode) {
        enforceCallingPermissions();

        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("setPermissionControlBlockModeForPkg: "
                    + constructPatternForPermission(code, pkg));

        long id = Binder.clearCallingIdentity();
        try {
            // Apply to appops first.
            Integer uid = mPackagesCache.get(pkg);
            int uidInt = uid == null ? -1 : uid;
            if (uidInt < 0) {
                XposedLog.wtf("Fail query uid: " + pkg);
            } else {
                // Align with appops.
                // Apply ranker.
                if (code == AppOpsManagerCompat.OP_POST_NOTIFICATION) {
                    AppOpsManager ops = (AppOpsManager) getContext().getSystemService(Context.APP_OPS_SERVICE);
                    try {
                        if (ops != null) {
                            ops.setMode(code, uid, pkg, mode);
                            XposedLog.verbose("Ops mode has been set");
                        }
                    } catch (Throwable e) {
                        XposedLog.wtf("Fail set mode to ops: " + e);
                    }
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        mNotificationService.setImportance(pkg, uidInt, NotificationManager.IMPORTANCE_DEFAULT);
//                    }
                }
            }

            if (mode != AppOpsManagerCompat.MODE_ALLOWED) {
                RepoProxy.getProxy().getPerms().add(constructPatternForPermission(code, pkg));
            } else {
                RepoProxy.getProxy().getPerms().remove(constructPatternForPermission(code, pkg));
            }
        } catch (Exception e) {
            XposedLog.wtf("Error setPermissionControlBlockModeForPkg: " + Log.getStackTraceString(e));
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public void setUserDefinedAndroidId(String id) {
        enforceCallingPermissions();
        XposedLog.verbose("setUserDefinedAndroidId: " + id);
        // Create an random ID.
        if (id == null) {
            id = Long.toHexString(new SecureRandom().nextLong());
        }
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETUSERDEFINEDANDROIDID, id).sendToTarget();
    }

    @Override
    public void setUserDefinedDeviceId(String id) {
        enforceCallingPermissions();
        XposedLog.verbose("setUserDefinedDeviceId: " + id);
        // Create an random ID.
        if (id == null) {
            id = Long.toHexString(new SecureRandom().nextLong());
        }
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETUSERDEFINEDDEVICEID, id).sendToTarget();
    }

    @Override
    public void setUserDefinedLine1Number(String id) {
        enforceCallingPermissions();
        XposedLog.verbose("setUserDefinedLine1Number: " + id);
        // Create an random ID.
        if (id == null) {
            id = String.valueOf(new SecureRandom().nextLong());
        }
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETUSERDEFINEDLINE1NUMBER, id).sendToTarget();
    }

    @SuppressLint("HardwareIds")
    @Override
    public String getAndroidId() {
        long id = Binder.clearCallingIdentity();
        try {
            return Settings.Secure.getString(getContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);
        } catch (Throwable e) {
            XposedLog.wtf("Error getAndroidId: " + Log.getStackTraceString(e));
            return null;
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    @Override
    public String getDeviceId() {
        enforceCallingPermissions();
        try {
            TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                return tm.getDeviceId();
            }
        } catch (Throwable e) {
            XposedLog.wtf("Error getAndroidId: " + Log.getStackTraceString(e));
            return null;
        }
        return null;
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    @Override
    public String getLine1Number() {
        enforceCallingPermissions();
        try {
            TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                return tm.getLine1Number();
            }
        } catch (Throwable e) {
            XposedLog.wtf("Error getLine1Number: " + Log.getStackTraceString(e));
            return null;
        }
        return null;
    }

    @Override
    public String getUserDefinedLine1Number() {
        long id = Binder.clearCallingIdentity();
        try {
            return mUserDefinedLine1Number.getData();
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public String getUserDefinedDeviceId() {
        long id = Binder.clearCallingIdentity();
        try {
            return mUserDefinedDeviceId.getData();
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public String getUserDefinedAndroidId() {
        long id = Binder.clearCallingIdentity();
        try {
            return mUserDefinedAndroidId.getData();
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    @BinderCall
    public String[] getPrivacyList(boolean priv) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getPrivacyList: " + priv);
        enforceCallingPermissions();
        if (!priv) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (outList.contains(s)) return;// Kik dup package.
                    if (isPackageprivacyByUser(s)) return;
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
            Set<String> packages = RepoProxy.getProxy().getPrivacy().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    if (isWhiteSysAppEnabled() && isInSystemAppList(p)) {
                        return;
                    }
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    @BinderCall(restrict = "any")
    public boolean isPackageInPrivacyList(String pkg) {
        if (pkg == null) return false;
        long id = Binder.clearCallingIdentity();
        try {
            if (isInWhiteList(pkg)) return false;
            if (isWhiteSysAppEnabled() && isInSystemAppList(pkg)) return false;
            return RepoProxy.getProxy().getPrivacy().has(pkg);
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }


    @Override
    @BinderCall(restrict = "any")
    public boolean isUidInPrivacyList(int uid) {
        // FIXME Too slow.
        return isPackageInPrivacyList(PkgUtil.pkgForUid(getContext(), uid));
    }

    @Override
    @BinderCall
    public int getPrivacyAppsCount() {
        return RepoProxy.getProxy().getPrivacy().size();
    }

    @Override
    @BinderCall
    public void addOrRemoveFromPrivacyList(String pkg, int op) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("addOrRemoveFromPrivacyList: " + pkg);
        enforceCallingPermissions();
        long id = Binder.clearCallingIdentity();
        try {
            if (op == XAshmanManager.Op.ADD) {
                RepoProxy.getProxy().getPrivacy().add(pkg);
            } else {
                RepoProxy.getProxy().getPrivacy().remove(pkg);
            }
        } finally {
            Binder.restoreCallingIdentity(id);
        }
    }

    @Override
    public boolean showFocusedActivityInfoEnabled() {
        return mShowFocusedActivityInfoEnabled.get();
    }

    @Override
    public void setShowFocusedActivityInfoEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETSHOWFOCUSEDACTIVITYINFOENABLED, enabled).sendToTarget();
    }

    @Override
    public void restoreDefaultSettings() {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_RESTOREDEFAULTSETTINGS).sendToTarget();
    }

    @Override
    public List<ActivityManager.RunningServiceInfo> getRunningServices(int max) {
        ActivityManager activityManager = (ActivityManager) getContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            long id = Binder.clearCallingIdentity();
            try {
                return activityManager.getRunningServices(max);
            } catch (Throwable e) {
                XposedLog.wtf("Fail getRunningServices: " + Log.getStackTraceString(e));
            } finally {
                Binder.restoreCallingIdentity(id);
            }
        }

        return new ArrayList<>(0);
    }

    @Override
    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        ActivityManager activityManager = (ActivityManager) getContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        final long id = Binder.clearCallingIdentity();
        try {
            Binder.clearCallingIdentity();
            if (activityManager != null) {
                return activityManager.getRunningAppProcesses();
            }
        } catch (Throwable e) {
            XposedLog.wtf("getRunningAppProcesses: " + Log.getStackTraceString(e));
        } finally {
            Binder.restoreCallingIdentity(id);
        }
        return new ArrayList<>(0);
    }

    @Override
    public void writeSystemSettings(final String key, final String value) {
        enforceCallingPermissions();
    }

    @Override
    public String getSystemSettings(String key) {
        return Settings.Global.getString(getContext().getContentResolver(), key);
    }

    @Override
    public long[] getProcessPss(int[] pids) {
        long id = Binder.clearCallingIdentity();
        try {
            return ActivityManagerNative.getDefault().getProcessPss(pids);
        } catch (Throwable e) {
            XposedLog.wtf("getProcessPss: " + Log.getStackTraceString(e));
        } finally {
            Binder.restoreCallingIdentity(id);
        }
        return new long[0];
    }

    @Builder
    @Getter
    private static class UncaughtException {
        String packageName, thread, exception, trace;
    }

    @Override
    public boolean onApplicationUncaughtException(String packageName, String thread, String exception, String trace) {

        XposedLog.verbose("uncaughtException on currentPackage@%s, thread@%s, throwable@%s", packageName, thread, exception);
        XposedLog.verbose("***** FATAL EXCEPTION TRACE DUMP APM-S*****\n%s", trace);

        mainHandler.removeMessages(AshManHandlerMessages.MSG_ONAPPLICATIONUNCAUGHTEXCEPTION);
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_ONAPPLICATIONUNCAUGHTEXCEPTION,
                UncaughtException.builder()
                        .exception(exception)
                        .packageName(packageName)
                        .thread(thread)
                        .trace(trace)
                        .build())
                .sendToTarget();

        int uid = Binder.getCallingUid();

        String opPackageName = PkgUtil.pkgForUid(getContext(), uid);

        // Check op, if allow to show FC dialog.
        int mode = getPermissionControlBlockModeForPkg(
                AppOpsManagerCompat.OP_FC_DIALOG, opPackageName == null ? packageName : opPackageName,
                true, new String[]{exception});

        XposedLog.verbose("uncaughtException on opPackageName:%s, packageName@%s, uid: %s, mode: %s",
                opPackageName,
                packageName,
                uid,
                mode);

        // Do not interrupt app crash.
        return mode == AppOpsManagerCompat.MODE_IGNORED;
    }

    @Override
    public boolean isAppCrashDumpEnabled() {
        return mShowAppCrashDumpEnabled.get();
    }

    @Override
    public void setAppCrashDumpEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETAPPCRASHDUMPENABLED, enabled).sendToTarget();
    }

    private RemoteCallbackList<ITopPackageChangeListener> mTopPackageListenerCallbacks;

    @Override
    public void registerOnTopPackageChangeListener(ITopPackageChangeListener listener) {
        XposedLog.verbose("registerOnTopPackageChangeListener: " + listener);
        Preconditions.checkNotNull(listener);
        mTopPackageListenerCallbacks.register(listener);
    }

    @Override
    public void unRegisterOnTopPackageChangeListener(ITopPackageChangeListener listener) {
        XposedLog.verbose(XposedLog.TAG_LAZY + "unRegisterOnTopPackageChangeListener: " + listener);
        Preconditions.checkNotNull(listener);
        mTopPackageListenerCallbacks.unregister(listener);
    }

    @Override
    public boolean isLazyModeEnabled() {
        return mLazyEnabled.get();
    }

    @Override
    public boolean isLazyModeEnabledForPackage(String pkg) {
        return isLazyModeEnabled() && isPackageLazyByUser(pkg);
    }

    @Override
    public void setLazyModeEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETLAZYMODEENABLED, enabled)
                .sendToTarget();
    }

    @Override
    public String[] getLazyApps(boolean lazy) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getLazyApps: " + lazy);
        enforceCallingPermissions();
        if (!lazy) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr,
                    new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            if (outList.contains(s)) return;// Kik dup package.
                            if (isPackageLazyByUser(s)) return;
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
            Set<String> packages = RepoProxy.getProxy().getLazy().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    if (isWhiteSysAppEnabled() && isInSystemAppList(p)) {
                        return;
                    }
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    public void addOrRemoveLazyApps(String[] packages, int op) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveLazyApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        addOrRemoveFromRepo(packages, RepoProxy.getProxy().getLazy(), op == XAshmanManager.Op.ADD);

        if (op == XAshmanManager.Op.ADD) {
            // Post a check.
            for (String p : packages) {
                postLazyServiceKillerIfNecessary(p, LAZY_KILL_SERVICE_NORMAL_INTERVAL, "Lazy-Added");
            }
        }
    }

    @Override
    @BinderCall
    public void setLPBKEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETLPBKENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isLPBKEnabled() {
        return mLongPressBackKillEnabled.get();
    }

    @Override
    @BinderCall
    public void onTaskRemoving(int callingUid, int taskId) throws RemoteException {
        String callingPkg = PkgUtil.pkgForUid(getContext(), callingUid);
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("removeTask: uid %s pkg %s task %s", callingUid, callingPkg, taskId);
        }

        if (isSystemUIPackage(callingPkg)) {
            String pkgOfThisTask = null;

            boolean isOreo = OSUtil.isOOrAbove();
            if (isOreo) {
                ComponentName targetComp = mTaskIdMap.get(taskId);
                if (targetComp != null) {
                    pkgOfThisTask = targetComp.getPackageName();
                    XposedLog.verbose("removeTask, pkgOfThisTask-IDMAP: " + pkgOfThisTask);
                }
            }

            // Retrieve package name for N and if no task comp got from cache.
            if (pkgOfThisTask == null) {
                // We will kill removed pkg.
                ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
                // Assume systemui has this permission.
                if (am != null) {
                    List<ActivityManager.RecentTaskInfo> tasks = am.getRecentTasks(99,
                            ActivityManager.RECENT_WITH_EXCLUDED);
                    if (tasks != null) {
                        for (ActivityManager.RecentTaskInfo rc : tasks) {
                            if (rc != null && rc.persistentId == taskId) {
                                pkgOfThisTask = PkgUtil.packageNameOf(rc.baseIntent);
                                break;
                            }
                        }
                        XposedLog.verbose("removeTask, pkgOfThisTask-AM: " + pkgOfThisTask);
                    }
                }
            }

            if (pkgOfThisTask != null) {
                PkgUtil.onAppBringDown(pkgOfThisTask, "removeTask");

                // Re-disable apps.
                try {
                    if (RepoProxy.getProxy().getPending_disable_apps_tr().size() != 0) {
                        // Disable pending apps.
                        for (String p : RepoProxy.getProxy().getPending_disable_apps_tr().getAll()) {
                            if (!isPackageRunningOnTop(p)) {
                                // Do not remove from pending disable.
                                setApplicationEnabledSetting(p, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0, true);
                                XposedLog.verbose("removeTask, Disable pending apps: " + p);
                                // RepoProxy.getProxy().getPending_disable_apps_tr().remove(p);
                            }
                        }
                    }
                } catch (Throwable e) {
                    XposedLog.wtf("removeTask, Fail handle disable_app: " + e);
                }

                // Tell app guard service to clean up verify res.
                mAppGuardService.onTaskRemoving(pkgOfThisTask);

                // Notify psm.
                String finalPkgOfThisTask1 = pkgOfThisTask;
                ErrorCatchRunnable psm = new ErrorCatchRunnable(() -> PackageStateManager.from(getContext()).onTaskRemoved(finalPkgOfThisTask1), "PSM onTaskRemoved");
                mLazyHandler.post(psm);

                if (!isTaskRemoveKillEnabled()) {
                    if (XposedLog.isVerboseLoggable()) {
                        XposedLog.verbose("removeTask: trk is not enabled");
                    }
                    return;
                }

                if (!shouldTRKPackage(pkgOfThisTask)) {
                    XposedLog.verbose("removeTask TRKPackage not enabled for this package");
                    return;
                }
                // Now we kill this pkg delay to let am handle first.
                final String finalPkgOfThisTask = pkgOfThisTask;
                mLazyHandler.postDelayed(new ErrorCatchRunnable(() -> {
                    XposedLog.verbose("removeTask, killing: " + finalPkgOfThisTask);
                    getAppIdler().setAppIdle(finalPkgOfThisTask);
                }, "removeTask-kill"), 888); // FIXME why 888?
            }
        }
    }

    @Override
    @BinderCall
    public void addOrRemoveAppFocusAction(String pkg, String[] actions, boolean add) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("addOrRemoveAppFocusAction: %s %s %s", pkg, Arrays.toString(actions), String.valueOf(add));
        }
        enforceCallingPermissions();
        if (add) {
            RepoProxy.getProxy().getAppFocused().put(pkg, GsonUtil.getGson().toJson(actions));
        } else {
            RepoProxy.getProxy().getAppFocused().remove(pkg);
        }
    }

    @Override
    public String[] getAppFocusActionPackages() {
        Set<String> allSet = RepoProxy.getProxy().getAppFocused().keySet();
        return convertObjectArrayToStringArray(allSet.toArray());
    }

    @Override
    public String[] getAppFocusActions(String pkg) {
        String v = RepoProxy.getProxy().getAppFocused().get(pkg);
        if (v == null) return new String[0];
        try {
            return GsonUtil.getGson().fromJson(v, String[].class);
        } catch (Exception e) {
            XposedLog.wtf("Fail from gson: " + e.getLocalizedMessage());
            return new String[0];
        }
    }

    @Override
    @BinderCall
    public void addOrRemoveAppUnFocusAction(String pkg, String[] actions, boolean add) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("addOrRemoveAppUnFocusAction: %s %s %s", pkg, Arrays.toString(actions), String.valueOf(add));
        }
        enforceCallingPermissions();
        if (add) {
            RepoProxy.getProxy().getAppUnFocused().put(pkg, GsonUtil.getGson().toJson(actions));
        } else {
            RepoProxy.getProxy().getAppUnFocused().remove(pkg);
        }
    }

    @Override
    @BinderCall
    public String[] getAppUnFocusActionPackages() {
        Set<String> allSet = RepoProxy.getProxy().getAppUnFocused().keySet();
        return convertObjectArrayToStringArray(allSet.toArray());
    }

    @Override
    @BinderCall
    public String[] getAppUnFocusActions(String pkg) {
        String v = RepoProxy.getProxy().getAppUnFocused().get(pkg);
        if (v == null) return new String[0];
        try {
            return GsonUtil.getGson().fromJson(v, String[].class);
        } catch (Exception e) {
            XposedLog.wtf("Fail from gson: " + e.getLocalizedMessage());
            return new String[0];
        }
    }

    @Override
    @BinderCall
    public void setDozeEnabled(boolean enable) {
        enforceCallingPermissions();

        if (mDozeHandler != null) {
            mDozeHandler.obtainMessage(DozeHandlerMessages.MSG_SETDOZEENABLED, enable)
                    .sendToTarget();
        }

        // This is test code for lr.
        // Binder:27874_11: type=1400 audit(0.0:37239): avc: denied { write } for name="lockscreenwallpaper" dev="mmcblk0p22" ino=938401 scontext=u:r:system_server:s0 tcontext=u:object_r:shell_data_file:s0 tclass=dir permissive=0
        if (BuildConfig.DEBUG) {
            final File f = new File("/data/local/tmp/lockscreenwallpaper/keyguard_wallpaper_land.png");
            long ident = Binder.clearCallingIdentity();
            try {
                Files.createParentDirs(f);
                f.createNewFile();
                boolean exist = f.exists();
                XposedLog.verbose("keyguard_wallpaper_land exists: " + exist);
            } catch (final IOException e) {
                XposedLog.wtf("e: " + Log.getStackTraceString(e));

                // Try using handler.
                mLazyHandler.post(new ErrorCatchRunnable(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Files.createParentDirs(f);
                            boolean exist = f.exists();
                            XposedLog.verbose("keyguard_wallpaper_land exists: " + exist);
                            f.createNewFile();
                        } catch (IOException e1) {
                            XposedLog.wtf("e2: " + Log.getStackTraceString(e));
                        }

                    }
                }, "test"));
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    @Override
    @BinderCall
    public boolean isDozeEnabled() {
        return mDozeEnabled.get() && isDozeSupported();
    }

    private boolean isDozeSupported() {
        return BuildConfig.DEBUG || OSUtil.isMOrAbove();
    }

    @Override
    public void setForceDozeEnabled(boolean enable) {
        enforceCallingPermissions();
        if (mDozeHandler != null) {
            mDozeHandler.obtainMessage(DozeHandlerMessages.MSG_SETFORCEDOZEENABLED, enable).sendToTarget();
        }
    }

    @Override
    public boolean isForceDozeEnabled() {
        return mForeDozeEnabled.get();
    }

    private boolean hasDozeFeature() {
        return XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_DOZE);
    }

    @Override
    @BinderCall
    public long getLastDozeEnterTimeMills() {
        synchronized (mDozeLock) {
            return mLastDozeEvent.getResult() == DozeEvent.RESULT_SUCCESS ?
                    mLastDozeEvent.getEnterTimeMills() : -1;
        }
    }

    @Override
    @BinderCall
    public DozeEvent getLastDozeEvent() {
        return mLastDozeEvent.duplicate();
    }

    @Override
    @BinderCall
    public long getDozeDelayMills() {
        return mDozeDelay;
    }

    @Override
    @BinderCall
    public void setDozeDelayMills(long delayMills) {
        if (delayMills < 0) {
            throw new IllegalArgumentException("Doze delayMills should be positive");
        }
        enforceCallingPermissions();
        if (mDozeHandler != null) {
            mDozeHandler.obtainMessage(DozeHandlerMessages.MSG_SETDOZEDELAYMILLS, delayMills)
                    .sendToTarget();
        }
    }

    @Override
    @BinderCall
    public void setDoNotKillSBNEnabled(boolean enable, String module) {
        enforceCallingPermissions();
        Pair<Boolean, String> data = new Pair<>(enable, module);
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETDONOTKILLSBNENABLED, data)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isDoNotKillSBNEnabled(String module) {
        return module.equals(XAppBuildVar.APP_LK)
                ? mDoNotKillSBNEnabled.get()
                : mDoNotKillSBNGreenEnabled.get();
    }

    @Override
    @BinderCall
    public void setTaskRemoveKillEnabled(boolean enable) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETTASKREMOVEKILLENABLED, enable)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isTaskRemoveKillEnabled() {
        return mTaskRemovedKillEnabled.get();
    }

    @Override
    public String[] getTRKApps(boolean kill) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getTRKApps: " + kill);
        enforceCallingPermissions();
        if (!kill) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (outList.contains(s)) return;// Kik dup package.
                    if (isPackageTRKByUser(s)) return;
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
            Set<String> packages = RepoProxy.getProxy().getTrks().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    if (isWhiteSysAppEnabled() && isInSystemAppList(p)) {
                        return;
                    }
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    public void addOrRemoveTRKApps(String[] packages, int op) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveTRKApps: " + Arrays.toString(packages));
        enforceCallingPermissions();
        if (packages == null || packages.length == 0) return;
        addOrRemoveFromRepo(packages, RepoProxy.getProxy().getTrks(), op == XAshmanManager.Op.ADD);
    }

    @Override
    @BinderCall
    public List<DozeEvent> getDozeEventHistory() {
        enforceCallingPermissions();
        synchronized (mDozeHistory) {
            List<DozeEvent> events = new ArrayList<>(mDozeHistory.size());
            events.add(mLastDozeEvent);
            events.addAll(mDozeHistory);
            return events;
        }
    }

    @Override
    public void setPrivacyEnabled(boolean enable) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETPRIVACYENABLED, enable).sendToTarget();
    }

    @Override
    public boolean isPrivacyEnabled() {
        return mPrivacyEnabled.get();
    }

    // PLUGIN API START.
    @Override
    public String[] getPluginApps() {
        return new String[0];
    }

    @Override
    public boolean isAppInPluginList(String pkg) {
        return false;
    }

    @Override
    public void addOrRemovePluginApp(String appPackageName, boolean add) {

    }

    @Override
    public boolean isAppLockEnabled() {
        return mAppGuardService.isAppLockEnabled();
    }

    @Override
    public void setAppLockEnabled(boolean enabled) throws RemoteException {
        mAppGuardService.setAppLockEnabled(enabled);
    }

    @Override
    public boolean isBlurEnabled() {
        return mAppGuardService.isBlurEnabled();
    }

    @Override
    @BinderCall
    public boolean isBlurEnabledForPackage(String packageName) {
        return mAppGuardService.isBlurForPkg(packageName);
    }

    @Override
    public void setBlurEnabled(boolean enabled) throws RemoteException {
        mAppGuardService.setBlurEnabled(enabled);
    }

    @Override
    public int getBlurRadius() {
        return mAppGuardService.getBlurRadius();
    }

    @Override
    public void setBlurRadius(int r) {
        mAppGuardService.setBlurRadius(r);
    }

    @Override
    public boolean isUninstallInterruptEnabled() {
        return mAppGuardService.isUninstallInterruptEnabled();
    }

    @Override
    public void setUninstallInterruptEnabled(boolean enabled) throws RemoteException {
        mAppGuardService.setUninstallInterruptEnabled(enabled);
    }

    @Override
    public void setVerifySettings(VerifySettings settings) throws RemoteException {
        mAppGuardService.setVerifySettings(settings);
    }

    @Override
    public VerifySettings getVerifySettings() throws RemoteException {
        return mAppGuardService.getVerifySettings();
    }

    @Override
    public void setResult(int transactionID, int res) throws RemoteException {
        mAppGuardService.setResult(transactionID, res);
    }

    @Override
    public boolean isTransactionValid(int transactionID) {
        return mAppGuardService.isTransactionValid(transactionID);
    }

    @Override
    public void mockCrash() throws RemoteException {
        mAppGuardService.mockCrash();
    }

    @Override
    public void setVerifierPackage(String pkg) throws RemoteException {
        mAppGuardService.setVerifierPackage(pkg);
    }

    @Override
    public void injectHomeEvent() throws RemoteException {
        mAppGuardService.injectHomeEvent();
    }

    @Override
    public void setDebug(boolean debug) throws RemoteException {
        mAppGuardService.setDebug(debug);
    }

    @Override
    public boolean isDebug() throws RemoteException {
        return mAppGuardService.isDebug();
    }

    @Override
    public void onActivityPackageResume(String pkg) {
        mAppGuardService.onActivityPackageResume(pkg);
    }

    @Override
    public boolean isInterruptFPEventVBEnabled(int event) throws RemoteException {
        return mAppGuardService.isInterruptFPEventVBEnabled(event);
    }

    @Override
    public void setInterruptFPEventVBEnabled(int event, boolean enabled) throws RemoteException {
        mAppGuardService.setInterruptFPEventVBEnabled(event, enabled);
    }

    @Override
    public void addOrRemoveComponentReplacement(ComponentName from, ComponentName to, boolean add) throws RemoteException {
        mAppGuardService.addOrRemoveComponentReplacement(from, to, add);
    }

    @Override
    public Map getComponentReplacements() throws RemoteException {
        return mAppGuardService.getComponentReplacements();
    }

    @Override
    public String[] getLockApps(boolean lock) throws RemoteException {
        return mAppGuardService.getLockApps(lock);
    }

    @Override
    public void addOrRemoveLockApps(String[] packages, boolean add) throws RemoteException {
        mAppGuardService.addOrRemoveLockApps(packages, add);
    }

    @Override
    public String[] getBlurApps(boolean lock) throws RemoteException {
        return mAppGuardService.getBlurApps(lock);
    }

    @Override
    public void addOrRemoveBlurApps(String[] packages, boolean blur) throws RemoteException {
        mAppGuardService.addOrRemoveBlurApps(packages, blur);
    }

    @Override
    public String[] getUPApps(boolean lock) throws RemoteException {
        return mAppGuardService.getUPApps(lock);
    }

    @Override
    public void addOrRemoveUPApps(String[] packages, boolean add) throws RemoteException {
        mAppGuardService.addOrRemoveUPApps(packages, add);
    }

    @Override
    @BinderCall
    public AppSettings retrieveAppSettingsForPackage(String pkg) {

        int mode = getPermissionControlBlockModeForPkg(
                AppOpsManagerCompat.OP_WAKE_LOCK, pkg, false, null
        );
        boolean wakelock = mode == AppOpsManagerCompat.MODE_IGNORED;

        mode = getPermissionControlBlockModeForPkg(
                AppOpsManagerCompat.OP_START_SERVICE, pkg, false, null
        );
        boolean service = mode == AppOpsManagerCompat.MODE_IGNORED;

        mode = getPermissionControlBlockModeForPkg(
                AppOpsManagerCompat.OP_SET_ALARM, pkg, false, null
        );
        boolean alarm = mode == AppOpsManagerCompat.MODE_IGNORED;

        return AppSettings.builder()
                .appLevel(getAppLevel(pkg))
                .applock(isInStringRepo(RepoProxy.getProxy().getLocks(), pkg))
                .blur(isInStringRepo(RepoProxy.getProxy().getBlurs(), pkg))
                .uninstall(isInStringRepo(RepoProxy.getProxy().getUninstall(), pkg))
                .privacy(isInStringRepo(RepoProxy.getProxy().getPrivacy(), pkg))

                .boot(isInStringRepo(RepoProxy.getProxy().getBoots(), pkg))
                .start(isInStringRepo(RepoProxy.getProxy().getStarts(), pkg))
                .lk(isInStringRepo(RepoProxy.getProxy().getLks(), pkg))
                .rfk(isInStringRepo(RepoProxy.getProxy().getRfks(), pkg))
                .trk(isInStringRepo(RepoProxy.getProxy().getTrks(), pkg))
                .lazy(isInStringRepo(RepoProxy.getProxy().getLazy(), pkg))

                .wakeLock(wakelock)
                .alarm(alarm)
                .service(service)

                .pkgName(pkg)
                .appName(String.valueOf(PkgUtil.loadNameByPkgName(getContext(), pkg)))
                .build();
    }

    @Override
    @BinderCall
    public void applyAppSettingsForPackage(String pkg, AppSettings settings) throws RemoteException {
        XposedLog.verbose("applyAppSettingsForPackage %s %s", pkg, settings);
        enforceCallingPermissions();

        String[] data = new String[]{pkg};

        addOrRemoveLockApps(data, settings.isApplock());
        addOrRemoveBlurApps(data, settings.isBlur());
        addOrRemoveUPApps(data, settings.isUninstall());
        addOrRemoveFromPrivacyList(pkg, settings.isPrivacy() ? XAshmanManager.Op.ADD : XAshmanManager.Op.REMOVE);

        addOrRemoveBootBlockApps(data, settings.isBoot() ? XAshmanManager.Op.ADD : XAshmanManager.Op.REMOVE);
        addOrRemoveStartBlockApps(data, settings.isStart() ? XAshmanManager.Op.ADD : XAshmanManager.Op.REMOVE);
        addOrRemoveLKApps(data, settings.isLk() ? XAshmanManager.Op.ADD : XAshmanManager.Op.REMOVE);
        addOrRemoveRFKApps(data, settings.isRfk() ? XAshmanManager.Op.ADD : XAshmanManager.Op.REMOVE);
        addOrRemoveTRKApps(data, settings.isTrk() ? XAshmanManager.Op.ADD : XAshmanManager.Op.REMOVE);
        addOrRemoveLazyApps(data, settings.isLazy() ? XAshmanManager.Op.ADD : XAshmanManager.Op.REMOVE);

        setPermissionControlBlockModeForPkg(AppOpsManagerCompat.OP_WAKE_LOCK,
                pkg,
                settings.isWakeLock() ? AppOpsManagerCompat.MODE_IGNORED : AppOpsManagerCompat.MODE_ALLOWED);

        setPermissionControlBlockModeForPkg(AppOpsManagerCompat.OP_SET_ALARM,
                pkg,
                settings.isAlarm() ? AppOpsManagerCompat.MODE_IGNORED : AppOpsManagerCompat.MODE_ALLOWED);

        setPermissionControlBlockModeForPkg(AppOpsManagerCompat.OP_START_SERVICE,
                pkg,
                settings.isService() ? AppOpsManagerCompat.MODE_IGNORED : AppOpsManagerCompat.MODE_ALLOWED);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    // FIX DEAD LOCK ISSUE:
    /*
    "ASHMAN-LAZY-H" prio=5 tid=22 Blocked
  | group="main" sCount=1 dsCount=0 obj=0x12c03d30 self=0xb2b6ff00
  | sysTid=26699 nice=0 cgrp=default sched=0/0 handle=0x9977f920
  | state=S schedstat=( 73325877 143207864 319 ) utm=5 stm=2 core=0 HZ=100
  | stack=0x9967d000-0x9967f000 stackSize=1038KB
  | held mutexes=
  at com.android.server.pm.PackageManagerService.getApplicationInfo(PackageManagerService.java:3448)
  - waiting to lock <0x0a1659d8> (a android.util.ArrayMap) held by thread 1
  at com.android.server.AppOpsService.getOpsRawLocked(AppOpsService.java:1436)
  at com.android.server.AppOpsService.getOpLocked(AppOpsService.java:1495)
  at com.android.server.AppOpsService.setMode(AppOpsService.java:704)
  - locked <0x08da60bb> (a com.android.server.AppOpsService)
  at android.app.AppOpsManager.setMode(AppOpsManager.java:1649)
  at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImpl.setPermissionControlBlockModeForPkg(unavailable:-1)
  at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImpl.applyOpsSettingsForPackage(unavailable:-1)
  at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImpl.access$11300(unavailable:-1)
  at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImpl$LazyHandler.onBroadcastAction(unavailable:-1)
  at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImpl$LazyHandler.handleMessage(unavailable:-1)
  at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImplDev$10$1.onCall(unavailable:-1)
  at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImplDev.makeSafeCall(unavailable:-1)
  at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImplDev.access$800(unavailable:-1)
  at github.tornaco.xposedmoduletest.xposed.service.XAshmanServiceImplDev$10.handleMessage(unavailable:-1)
  at android.os.Handler.dispatchMessage(Handler.java:98)
  at android.os.Looper.loop(Looper.java:154)
  at android.os.HandlerThread.run(HandlerThread.java:61)
     */
    private void applyOpsSettingsForPackage(String pkg) {
        XposedLog.verbose("applyOpsSettingsForPackage: " + pkg);
        try {
            for (int i = 0; i < AppOpsManagerCompat._NUM_OP; i++) {
                int code = i;
                int mode = getPermissionControlBlockModeForPkg(code, XAshmanManager.APPOPS_WORKAROUND_DUMMY_PACKAGE_NAME, false, null);
                XposedLog.verbose("Template code and mode: %s %s", code, mode);
                setPermissionControlBlockModeForPkg(code, pkg, mode);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail applyOpsSettingsForPackage for " + pkg + ", err " + Log.getStackTraceString(e));
        }
    }

    @Override
    @BinderCall
    public void backupTo(String dir) {
        long ident = Binder.clearCallingIdentity();
        try {
            RepoProxy.getProxy().backupTo(dir);
        } catch (Throwable e) {
            XposedLog.wtf("backupTo fail " + Log.getStackTraceString(e));
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    @Override
    @BinderCall
    public void restoreFrom(String dir) {
        // No impl yet.
    }

    @Override
    @BinderCall
    public String[] getRawPermSettings(int page, int countInPage) {
        return convertObjectArrayToStringArray(RepoProxy.getProxy().getPerms().getAll().toArray());
    }

    @Override
    @BinderCall
    public void setAppInstalledAutoApplyTemplate(AppSettings settings) {
        SettingsProvider.get().putString("AppInstalledAutoApplyTemplate", settings.toJson());
        if (BuildConfig.DEBUG) {
            AppSettings test = AppSettings.fromJson(SettingsProvider.get().getString("AppInstalledAutoApplyTemplate", null));
            XposedLog.verbose("setAppInstalledAutoApplyTemplate test: " + test);
        }
    }

    @Override
    @BinderCall
    public AppSettings getAppInstalledAutoApplyTemplate() {
        AppSettings as = AppSettings.fromJson(SettingsProvider.get()
                .getString("AppInstalledAutoApplyTemplate", null));
        if (as == null) as = AppSettings.builder()
                .boot(true)
                .start(true)
                .trk(true)
                .rfk(true)
                .lk(true)
                .build();
        return as;
    }

    @Override
    @BinderCall
    public void setAppOpsTemplate(OpsSettings opsSettings) {
        SettingsProvider.get().putString("AppOpsTemplate", opsSettings.toJson());
        if (BuildConfig.DEBUG) {
            OpsSettings test = OpsSettings.fromJson(SettingsProvider.get().getString("AppOpsTemplate", null));
            XposedLog.verbose("setAppOpsTemplate test: " + test);
        }
    }

    @Override
    @BinderCall
    public OpsSettings getAppOpsTemplate(OpsSettings opsSettings) {
        OpsSettings os = OpsSettings.fromJson(SettingsProvider.get().getString("AppOpsTemplate", null));
        if (os == null) os = new OpsSettings(AppOpsManagerCompat.getDefaultModes());
        return os;
    }

    @Override
    public void addPendingDisableAppsTR(String pkg) {
        XposedLog.verbose("addPendingDisableAppsTR: " + pkg);
        RepoProxy.getProxy().getPending_disable_apps_tr().add(pkg);
    }

    // PLUGIN API END.

    // Fix #126, From the log we found that the caller is "com.htc.lockscreen", so
    // we think it is OK for htc...
    private boolean isSystemUIPackage(String pkgName) {
        // Should we check caller?
        if (!BuildConfig.DEBUG) {
            // return true;// Always true for user build.
        }

        return pkgName != null
                && (pkgName.equals(SYSTEM_UI_PKG)
                || pkgName.equals(SYSTEM_UI_PKG_HTC)
                || pkgName.equals(SYSTEM_UI_PKG_HUAWEI));
    }

    private void postNotifyTopPackageChanged(final String from, final String to) {

        if (from == null || to == null) return;
        mLazyHandler.removeMessages(AshManLZHandlerMessages.MSG_NOTIFYTOPPACKAGECHANGED);
        mLazyHandler.obtainMessage(AshManLZHandlerMessages.MSG_NOTIFYTOPPACKAGECHANGED,
                new Pair<>(from, to))
                .sendToTarget();

        XposedLog.verbose("LAZY postNotifyTopPackageChanged before checking " + from + "->" + to);

        // Check if it needed to perform lazy.
        postLazyServiceKillerIfNecessary(from, LAZY_KILL_SERVICE_NORMAL_INTERVAL, "Package-Move-Front");
    }

    private static final long LAZY_KILL_SERVICE_NORMAL_INTERVAL = 5 * 1000;
    private static final long LAZY_KILL_SERVICE_NOTIFICATION_INTERVAL = LAZY_KILL_SERVICE_NORMAL_INTERVAL;
    private static final long LAZY_KILL_SERVICE_PROCESS_INTERVAL = 2 * LAZY_KILL_SERVICE_NORMAL_INTERVAL;
    private static final long LAZY_CHECK_PACKAGE_PROCESS_DELAY = 500;

    private void postLazyServiceKillerIfNecessary(String packageName, long intervalToPerform, String reason) {
        XposedLog.verbose("LAZY postLazyServiceKillerIfNecessary %s %s", packageName, reason);

        // Check white list first.
        if (GlobalWhiteList.isInGlobalWhiteList(packageName)) {
            XposedLog.verbose("LAZY postLazyServiceKillerIfNecessary, ignore in global white list");
            return;
        }

        if (!isLazyModeEnabled() || !isPackageLazyByUser(packageName)) {
            return;
        }

        // Kill services by ActiveServices!
        Runnable lazyKill = new LazyServiceKiller(packageName);
        ErrorCatchRunnable ecr = new ErrorCatchRunnable(lazyKill, "lazyKill");
        // Kill all service after xs.
        mLazyHandler.postDelayed(ecr, intervalToPerform);
        XposedLog.verbose("LAZY post lazy!!! " + packageName + ", delayed: " + intervalToPerform);
    }

    private boolean isPackageRunningOnTop(String pkg) {
        return pkg != null && pkg.equals(mTopPackageImd.getData());
    }

    private boolean isPackageRunningOnTopDelay(String pkg) {
        return pkg != null && pkg.equals(mTopPackageDelay.getData());
    }

    private boolean confirmToStopLazyService(String packageName, ComponentName name) {
        // Rule not enabled, always stop it.
        if (!isLazyRuleEnabled()) {
            XposedLog.verbose("LAZY confirmToStopLazyService, lazy rule not enabled");
            return true;
        }

        // Do not stop if we even don't know who it is.
        if (packageName == null || name == null) {
            XposedLog.verbose("LAZY confirmToStopLazyService, pkg or name is null");
            return false;
        }

        // Check if on top again.
        if (isPackageRunningOnTop(packageName) || isPackageRunningOnTopDelay(packageName)) {
            XposedLog.verbose("LAZY confirmToStopLazyService, pkg still on top!!!");
            return false;
        }

        // Check rules.
        @LazyRuleCheck
        String[] ruleKeep = constructStopServiceKeepRule(packageName, name);
        if (BuildConfig.DEBUG) {
            XposedLog.verbose("constructStopServiceKeepRule: " + Arrays.toString(ruleKeep));
        }
        return !RepoProxy.getProxy().getLazy_rules().has(ruleKeep);
    }

    // TODO Consider make a cache.
    // KEEP A *
    // KEEP A xx/.yy
    // KEEP A xx/xx.yy
    private static String[] constructStopServiceKeepRule(String packageName,
                                                         ComponentName componentName) {
        if (packageName == null || componentName == null) return null;
        String shortName = componentName.flattenToShortString();
        String name = componentName.flattenToString();
        return new String[]{
                String.format("KEEP %s *", packageName),
                String.format("KEEP %s %s", packageName, shortName),
                String.format("KEEP %s %s", packageName, name),
        };
    }

    @Getter
    @AllArgsConstructor
    private class LazyServiceKiller implements Runnable {

        private String targetServicePkg;

        @Override
        public void run() {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("LAZY, checking if need clean up service:" + targetServicePkg);
            }

            // If current top package is that we want to kill, skip it.
            if (isPackageRunningOnTop(targetServicePkg)) {
                XposedLog.wtf("LAZY, package is still running on top, won't kill it's services: " + targetServicePkg);
                return;
            }

            // Check if has notification.
            if (isDoNotKillSBNEnabled(XAppBuildVar.APP_GREEN)
                    && hasNotificationForPackageInternal(targetServicePkg)) {
                XposedLog.verbose("LAZY, package has SBN, do not kill: " + targetServicePkg);
                return;
            }

            if (isHandlingPushMessageIntent(targetServicePkg)) {
                XposedLog.verbose("LAZY, package isHandlingPushMessageIntent, do not kill, post again? " + targetServicePkg);
                // postLazyServiceKillerIfNecessary(targetServicePkg, GCMFCMHelper.GCM_INTENT_HANDLE_INTERVAL_MILLS);
                // return;
            }

            boolean isFwSolution = isAppServiceLazyControlSolutionEnable(XAshmanManager.AppServiceControlSolutions.FLAG_FW);


            // Invoke ActiveServices.
            if (isFwSolution) {
                if (mActiveServicesProxy != null) {
                    XposedLog.verbose("LAZY, isFwSolution candidate package to kill: " + targetServicePkg);

                    toastLazyAppTipsIfNeeded(targetServicePkg, "B");

                    int uid = PkgUtil.uidForPkg(getContext(), targetServicePkg);
                    if (uid > 0) {
                        mActiveServicesProxy.stopServicesForPackageUid(uid, new String[]{targetServicePkg},
                                mActiveServicesServiceStopperProxy);
                    } else {
                        XposedLog.wtf("LAZY, package uid is NOT valid!!!");
                    }
                } else {
                    XposedLog.wtf("LAZY, mActiveServicesProxy is null !!!");
                }
            }

            // Invoke App service control.
            boolean isAppSolution = isAppServiceLazyControlSolutionEnable(XAshmanManager.AppServiceControlSolutions.FLAG_APP);
            if (isAppSolution) {
                XposedLog.verbose("LAZY, isAppSolution candidate package to kill: " + targetServicePkg);
                toastLazyAppTipsIfNeeded(targetServicePkg, "A");
                mAppServiceController.stopAppService(targetServicePkg, mAppServiceControllerServiceStopperProxy);
            }
        }
    }

    private void toastLazyAppTipsIfNeeded(String packageName, String solutionName) {
        // Check if tips enabled.
        ErrorCatchRunnable er = new ErrorCatchRunnable(() -> {
            boolean optLazyTipsEnabled = isOptFeatureEnabled("LAZY_APP_TIPS");
            if (optLazyTipsEnabled) {
                Toast.makeText(getContext(),
                        String.format("即将为 %s 停止服务。",
                                PkgUtil.loadNameByPkgName(getContext(), packageName)),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }, "toastLazyAppTipsIfNeeded");
        er.run();
        // Do not bother for error.
    }

    private ActiveServicesServiceStopper mActiveServicesServiceStopperProxy
            = new ActiveServicesServiceStopper() {

        @Override
        public boolean stopService(ServiceRecordProxy serviceRecordProxy) {

            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("LAZY mAppServiceControllerServiceStopperProxy now stop: " + serviceRecordProxy.getName());
            }

            boolean confirm = confirmToStopLazyService(serviceRecordProxy.getPackageName(), serviceRecordProxy.getName());
            if (!confirm) {
                if (XposedLog.isVerboseLoggable()) {
                    XposedLog.verbose("LAZY mActiveServicesServiceStopperProxy stopService, skip for no confirm: "
                            + serviceRecordProxy.getName());
                }
                return false;
            }

            // Let's do stop.
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("LAZY mActiveServicesServiceStopperProxy stopping service: " + serviceRecordProxy);
            }

            // First stop by ActiveServices.
            // Now always do it for user build.
            boolean stopped = mActiveServicesProxy.stopServiceLocked(serviceRecordProxy.getHost());
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("LAZY stopService, mActiveServicesServiceStopperProxy stop res: " + stopped);
            }
            return true;
        }
    };

    private final AppServiceControlServiceStopper mAppServiceControllerServiceStopperProxy
            = control -> {

        ComponentName name = null;
        try {
            name = control.getServiceComponent();
        } catch (RemoteException e) {
            XposedLog.wtf("LAZY Fail retrieve getServiceComponent: " + Log.getStackTraceString(e));
            return false;
        }

        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("LAZY mAppServiceControllerServiceStopperProxy now stop: " + name);
        }

        if (name == null) return false;

        boolean confirm = confirmToStopLazyService(name.getPackageName(), name);
        if (!confirm) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("LAZY mAppServiceControllerServiceStopperProxy stopService," +
                        " skip for no confirm: " + name);
            }
            return false;
        }

        // Let's do stop.
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("LAZY mAppServiceControllerServiceStopperProxy stopping service: " + name);
        }

        // First stop by ActiveServices.
        // Now always do it for user build.
        boolean stopped = true;
        try {
            control.stopService();
        } catch (RemoteException e) {
            XposedLog.wtf("LAZY Fail call control.stopService(): " + Log.getStackTraceString(e));
            stopped = false;
        }
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("LAZY stopService, mAppServiceControllerServiceStopperProxy stop res: " + stopped);
        }
        return stopped;
    };

    private void notifyTopPackageChanged(final String from, final String to) {
        try {
            int itemCount = mTopPackageListenerCallbacks.beginBroadcast();
            for (int i = 0; i < itemCount; i++) {
                ITopPackageChangeListener l = mTopPackageListenerCallbacks.getBroadcastItem(i);
                try {
                    l.onChange(from, to);
                } catch (Throwable ignored) {
                }
            }
        } catch (Exception e) {
            XposedLog.wtf("Fail broadcast top listener: " + e);
        } finally {
            mTopPackageListenerCallbacks.finishBroadcast();
        }

        // Also notifiy the pmh.
        try {
            for (PushNotificationHandler h : mPushNotificationHandlers) {
                h.onTopPackageChanged(to);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail broadcast onTopPackageChanged to PushNotificationHandler : " + e);
        }
    }

    @Override
    public int checkPermission(String perm, int pid, int uid) {
        return PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public int checkOperation(int code, int uid, String packageName, String reason) {
        int mode = checkOperationInternal(code, uid, packageName, reason);
        logOperationIfNecessary(code, uid, packageName, reason, mode, null);
        return mode;
    }

    private void logOperationIfNecessary(int code, int uid, String packageName, String reason, int mode, String[] payload) {
        // No log for power save.
        if (isPowerSaveModeEnabled()) return;

        if (code >= AppOpsManagerCompat._NUM_OP) {
            // Do not add invaild op.
            return;
        }

        if (packageName == null) return;

        if (BuildConfig.APPLICATION_ID.equals(packageName)) return;

        if (PkgUtil.isSystemOrPhoneOrShell(uid)) return;

        if (isInWhiteList(packageName)) return;

        if (isWhiteSysAppEnabled() && isInSystemAppList(packageName))
            return;

        if (AppOpsManagerCompat.isLoggableOp(code)) {
            logOpEventToMemory(packageName, code, mode, payload);
        }
    }

    @Override
    @InternalCall
    public boolean resident(String pkgName) {
        return !isInSystemAppList(pkgName)
                && RepoProxy.getProxy().getResident().has(pkgName);
    }

    @Override
    @InternalCall
    public boolean residentEnableInternal() {
        return !mIsSafeMode && mResidentEnabled.get();
    }

    @Override
    @BinderCall
    public boolean isResidentEnabled() {
        return mResidentEnabled.get();
    }

    @Override
    @BinderCall
    public boolean isResidentEnabledForPackage(String who) {
        return resident(who);
    }

    @Override
    @BinderCall
    public void setResidentEnabled(boolean enable) {
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETRESIDENTENABLED, enable).sendToTarget();
    }

    @Override
    @BinderCall
    public void addOrRemoveResidentApps(String app, boolean add) {
        if (XposedLog.isVerboseLoggable())
            XposedLog.verbose("addOrRemoveResidentApps: " + app);
        enforceCallingPermissions();
        if (app == null) return;
        if (isInSystemAppList(app)) return; // Not allowed for system app.
        addOrRemoveFromRepo(new String[]{app}, RepoProxy.getProxy().getResident(), add);
    }

    @Override
    @BinderCall
    public String[] getResidentApps(boolean resident) {
        if (XposedLog.isVerboseLoggable()) XposedLog.verbose("getResidentApps: " + resident);
        enforceCallingPermissions();
        if (!resident) {
            Collection<String> packages = mPackagesCache.keySet();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> outList = Lists.newArrayList();

            // Remove those not in blocked list.
            String[] allPackagesArr = convertObjectArrayToStringArray(packages.toArray());
            Collections.consumeRemaining(allPackagesArr, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    if (isInSystemAppList(s)) return; // No system app.
                    if (outList.contains(s)) return;// Kik dup package.
                    if (isPackageResidentByUser(s)) return;
                    if (isInWhiteList(s)) return;
                    outList.add(s);
                }
            });

            if (outList.size() == 0) {
                return new String[0];
            }
            Object[] objArr = outList.toArray();
            return convertObjectArrayToStringArray(objArr);
        } else {
            Set<String> packages = RepoProxy.getProxy().getResident().getAll();
            if (packages.size() == 0) {
                return new String[0];
            }

            final List<String> noSys = Lists.newArrayList();

            Collections.consumeRemaining(packages, new Consumer<String>() {
                @Override
                public void accept(String p) {
                    if (isInSystemAppList(p)) {
                        return;
                    }
                    noSys.add(p);
                }
            });
            return convertObjectArrayToStringArray(noSys.toArray());
        }
    }

    @Override
    @BinderCall
    public boolean isPanicHomeEnabled() {
        return mPanicHomeEnabled.get();
    }

    @Override
    @BinderCall
    public void setPanicHomeEnabled(boolean enable) {
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETPANICHOMEENABLED, enable).sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isPanicLockEnabled() {
        return mPanicLockEnabled.get();
    }

    @Override
    @BinderCall
    public void setPanicLockEnabled(boolean enable) {
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETPANICLOCKENABLED, enable).sendToTarget();
    }

    @Override
    @BinderCall
    public void lockNow() {
        if (mDevicePolicyManagerService != null) {
            wrapCallingIdetUnCaught(new Runnable() {
                @Override
                public void run() {
                    try {
                        mDevicePolicyManagerService.lockNow(getContext());
                    } catch (RemoteException e) {
                        XposedLog.wtf("lockNow: " + e);
                    }
                }
            });
        }
    }

    @Override
    public boolean isInRedemptionMode() {
        return RepoProxy.hasFileIndicator(SubModuleManager.REDEMPTION);
    }

    @Override
    public void leaveRedemptionMode() {
        RepoProxy.deleteFileIndicator(SubModuleManager.REDEMPTION);
    }

    @Override
    public void enterRedemptionMode() {
        RepoProxy.createFileIndicator(SubModuleManager.REDEMPTION);
    }

    @Override
    public boolean isSELinuxEnabled() {
        return SELinuxHelper.isSELinuxEnabled();
    }

    @Override
    public boolean isSELinuxEnforced() {
        return SELinuxHelper.isSELinuxEnforced();
    }

    @Override
    public void setSelinuxEnforce(boolean enforce) {
    }

    @Override
    public boolean isPowerSaveModeEnabled() {
        return mPowerSaveModeEnabled.get();
    }

    @Override
    public void setPowerSaveModeEnabled(boolean enable) {
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETPOWERSAVEMODEENABLED, enable).sendToTarget();
    }

    @Override
    public String[] getStartRules() {
        return convertObjectArrayToStringArray(RepoProxy.getProxy().getStart_rules().getAll().toArray());
    }

    @Override
    public boolean addOrRemoveStartRules(String rule,
                                         final boolean add) {
        XposedLog.verbose("addOrRemoveStartRules: " + rule + ", " + add);
        RuleParser p = RuleParser.Factory.newParser();
        Rule r = p.parse(rule);
        XposedLog.verbose("addOrRemoveStartRules: " + r);
        if (r == null) return false;
        String rulePattern = r.toInternalPattern();
        if (add) {
            RepoProxy.getProxy().getStart_rules().add(rulePattern);
            return true;
        } else {
            return RepoProxy.getProxy().getStart_rules().remove(rulePattern);
        }
    }

    @Override
    public boolean hasSystemError() {
        final Holder<Boolean> res = new Holder<>();
        wrapCallingIdetUnCaught(new ErrorCatchRunnable(new Runnable() {
            @Override
            public void run() {
                res.setData(!FileUtil.isEmptyDirOrNoExist(RepoProxy.getSystemErrorTraceDirByVersion()));
            }
        }, "hasSystemError"));
        return res.getData();
    }

    @Override
    public void cleanUpSystemErrorTraces() {
        wrapCallingIdetUnCaught(new ErrorCatchRunnable(new Runnable() {
            @Override
            public void run() {
                FileUtil.deleteDir(RepoProxy.getSystemErrorTraceDir());
            }
        }, "cleanUpSystemErrorTraces"));
    }

    @Override
    public void addAppLockWhiteListActivity(String[] activities) {
        XposedLog.verbose("addAppLockWhiteListActivity: " + Arrays.toString(activities));
        if (activities == null) return;
        for (String a : activities) {
            RepoProxy.getProxy().getLock_white_list_activity().add(a);
        }
    }

    private int checkOperationInternal(int code, int uid, String packageName, String reason) {
        if (packageName == null) return AppOpsManagerCompat.MODE_ALLOWED;

        if (BuildConfig.APPLICATION_ID.equals(packageName)) return AppOpsManagerCompat.MODE_ALLOWED;

        if (PkgUtil.isSystemOrPhoneOrShell(uid)) return AppOpsManagerCompat.MODE_ALLOWED;

        if (isInWhiteList(packageName)) return AppOpsManagerCompat.MODE_ALLOWED;

        if (isWhiteSysAppEnabled() && isInSystemAppList(packageName))
            return AppOpsManagerCompat.MODE_ALLOWED;

        if (DEBUG_OP && XposedLog.isVerboseLoggable()) {
            String permName = AppOpsManagerCompat.opToPermission(code);
            XposedLog.verbose("checkOperation: reason %s code %s perm %s uid %s pkg %s",
                    reason, code, permName, uid, packageName);
        }

        String pattern = constructPatternForPermission(code, packageName);

        long id = Binder.clearCallingIdentity();
        try {
            if (isInPermissionBlockList(pattern)) {
                if (DEBUG_OP) {
                    XposedLog.verbose("checkOperation: returning MODE_IGNORED");
                }
                return AppOpsManagerCompat.MODE_IGNORED;
            }
        } finally {
            Binder.restoreCallingIdentity(id);
        }

        return AppOpsManagerCompat.MODE_ALLOWED;
    }

    private boolean isInPermissionBlockList(String pattern) {
        return RepoProxy.getProxy().getPerms().has(pattern);
    }

    private static String constructPatternForPermission(int code, String pkg) {
        return pkg + "@" + code;
    }

    private void processPendingDataRestrictRequests() {
        initDataInterface();
        if (TextUtils.isEmpty(mDataInterfaceName)) {
            return;
        }
        if (mPendingDataRestrictReceiver != null) {
            // getContext().unregisterReceiver(mPendingDataRestrictReceiver);
            // mPendingDataRestrictReceiver = null;
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

        if (!XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_FIREWALL)) {
            throw new IllegalStateException("restrictAppOnData, " +
                    "What the fuck? the firewall is not enabled at this build, but we got it up?");
        }

        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_RESTRICTAPPONDATA, uid, -1, restrict)
                .sendToTarget();
    }

    private void restrictAppOnDataForce(int uid, boolean restrict) {
        XposedLog.debug("NMS restrictAppOnDataForce: " + uid + ", restrict: " + restrict);
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_RESTRICTAPPONDATA, uid, 1, restrict)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public void restrictAppOnWifi(int uid, boolean restrict) {
        if (!XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_FIREWALL)) {
            throw new IllegalStateException("restrictAppOnWifi, " +
                    "What the fuck? the firewall is not enabled at this build, but we got it up?");
        }

        XposedLog.debug("NMS restrictAppOnWifi: " + uid + ", restrict: " + restrict);
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_RESTRICTAPPONWIFI, uid, -1, restrict)
                .sendToTarget();
    }

    private void restrictAppOnWifiForce(int uid, boolean restrict) {
        XposedLog.debug("NMS restrictAppOnWifiForce: " + uid + ", restrict: " + restrict);
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_RESTRICTAPPONWIFI, uid, 1, restrict)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isRestrictOnData(int uid) {
        NetworkRestriction match = new NetworkRestriction(POLICY_REJECT_ON_DATA, uid);
        return mDataBlackList.has(match.toJson());
    }

    @Override
    @BinderCall
    public boolean isRestrictOnWifi(int uid) {
        NetworkRestriction match = new NetworkRestriction(POLICY_REJECT_ON_WIFI, uid);
        return mWifiBlackList.has(match.toJson());
    }

    // NMS API END.

    @Override
    @CommonBringUpApi
    public void retrieveSettings() {
        XposedLog.wtf("retrieveSettings@" + getClass().getSimpleName());
        loadConfigFromSettings();
        cachePackages();

        mAppGuardService.retrieveSettings();


        // Init push handlers.
        // Register here to make sure we can read settings correctly.
        registerPushNotificationHandler(new WeChatPushNotificationHandler(getContext(), this));
        registerPushNotificationHandler(new TGPushNotificationHandler(getContext(), this));
    }

    private void construct() {
        RepoProxy.getProxy();

        mainHandler = onCreateServiceHandler();

        mLazyHandler = onCreateLazyHandler();

        boolean hasDozeFeature = XAppBuildVar.BUILD_VARS.contains(XAppBuildVar.APP_DOZE);
        if (hasDozeFeature && isDozeSupported()) {
            mDozeHandler = onCreateDozeHandler();
        } else {
            XposedLog.wtf("Will not create doze handler when no doze feature");
        }

        if (XposedLog.isVerboseLoggable()) {
            XposedLog.debug(
                    "construct, mainHandler: " + mainHandler
                            + ", mLazyHandler: " + mLazyHandler
                            + ", mDozeHandler: " + mDozeHandler
                            + ", @serial: " + serial());
        }

        mTopPackageListenerCallbacks = new RemoteCallbackList<>();

        if (getContext() != null) {
            mKillIdler = new KillAppIdler(getContext(), mOnAppIdleListener);
        }

        // Under test.
        if (false)
            mSystemGesturesPointerEventListener = new SystemGesturesPointerEventListener(getContext(),
                    new SystemGesturesPointerEventListenerCallbackImpl(getContext()));
    }

    private AppIdler getAppIdler() {
        AppIdler idler = getAppIdlerInternal();
        if (BuildConfig.DEBUG) {
            XposedLog.verbose("getAppIdler: " + idler);
        }
        return idler;
    }

    // FIXME Temp disable this feature for user build.
    // There is some serious issue on MIUI that cause system dead.
    private static final boolean HAS_STATS_MANAGER =
            BuildConfig.DEBUG && Build.VERSION.SDK_INT
                    >= Build.VERSION_CODES.LOLLIPOP_MR1;

    private AppIdler getAppIdlerInternal() {
        if (mKillIdler == null && mInactiveIdler == null) {
            return mDummyIdler;
        }
        // Safe check before usage.
        return (HAS_STATS_MANAGER && mInactiveInsteadOfKillAppInstead.get() && (mInactiveIdler != null))
                ? mInactiveIdler : mKillIdler;
    }

    protected Handler onCreateServiceHandler() {
        return new HandlerImpl();
    }

    protected Handler onCreateLazyHandler() {
        return new LazyHandler();
    }

    protected Handler onCreateDozeHandler() {
        return new DozeHandlerImpl();
    }

    @Override
    @CommonBringUpApi
    public void shutdown() {
        mAppGuardService.shutdown();
    }

    private IActivityManager getActivityManager() {
        return ActivityManagerNative.getDefault();
    }

    private ComponentName mFocusedCompName;

    private ClickableToastManager.OnToastClickListener mOnToastClickListener
            = new ClickableToastManager.OnToastClickListener() {
        @Override
        public void onToastClick(String text) {
            // Do not crash anyway.
            try {
                ClipboardManager cmb = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (cmb != null) {
                    cmb.setPrimaryClip(ClipData.newPlainText("service_config", text));
                }
            } catch (Throwable ignored) {
            }
        }
    };

    private FloatView.Callback mFloatCallback = new FloatView.Callback() {
        @Override
        public void onSingleTap(String text) {
            XposedLog.verbose("onSingleTap:" + text);
            mOnToastClickListener.onToastClick(text);
        }

        @Override
        public void onDoubleTap() {
            XposedLog.verbose("onDoubleTap");
        }

        @Override
        public void onSwipeDirection(@NonNull FloatView.SwipeDirection direction) {
            XposedLog.verbose("onSwipeDirection");
        }

        @Override
        public void onSwipeDirectionLargeDistance(@NonNull FloatView.SwipeDirection direction) {
            XposedLog.verbose("onSwipeDirectionLargeDistance");
        }

        @Override
        public void onLongPress() {
            XposedLog.verbose("onLongPress");
        }
    };

    private FloatView mFloatView;

    private Runnable toastRunnable = new Runnable() {
        @Override
        public void run() {
            ComponentName c = mFocusedCompName;
            if (c != null) {
                try {
                    if (mFloatView == null) {
                        mFloatView = new FloatView(getContext(), mFloatCallback);
                        mFloatView.attach();
                        mFloatView.show();
                    }
                    String raw = c.flattenToString();
                    mFloatView.setText(raw);
                } catch (Throwable ignored) {
                    Log.e(XposedLog.TAG, "toastRunnable: " + Log.getStackTraceString(ignored));
                }
            }
        }
    };

    @Override
    @InternalCall
    @CommonBringUpApi
    public void onPackageMoveToFront(final Intent intent) {
        if (BuildConfig.DEBUG) {
            XposedLog.verbose("onPackageMoveToFront: " + intent);
        }
        if (intent == null) return;

        // Check if this is a verifier.
        // Ignore this event for verifier.
        ComponentName componentName = intent.getComponent();
        if (componentName != null && XAshmanManager.VERIFIER_CLASS_NAME.equals(componentName.getClassName())) {
            XposedLog.verbose("Ignore onPackageMoveToFront for verifier...");
            return;
        }

        // Notify app lock service.
        mAppGuardService.onPackageMoveToFront(intent);

        onPackageMoveToFrontInternal(intent);
    }

    private static final long PKG_MOVE_TO_FRONT_EVENT_DELAY = 256;

    private void onPackageMoveToFrontInternal(final Intent intent) {
        // Update top imd right now.
        String packageName = PkgUtil.packageNameOf(intent);

        if (packageName != null) {
            mLazyHandler.removeMessages(AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONT);
            mLazyHandler.removeMessages(AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONTDELAYUPDATE);

            // Post to callbacks imd.
            mLazyHandler.obtainMessage(
                    AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONT, packageName).sendToTarget();

            mLazyHandler
                    .sendMessageDelayed(
                            mLazyHandler.obtainMessage(AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONTDELAYUPDATE, packageName),
                            PKG_MOVE_TO_FRONT_EVENT_DELAY);
        }

        // For debug.
        if (mIsSystemReady && showFocusedActivityInfoEnabled()) {
            mLazyHandler.removeCallbacks(toastRunnable);
            mFocusedCompName = intent.getComponent();
            mLazyHandler.post(toastRunnable);
        }
    }

    @Override
    public String serial() {
        return mSerialUUID.toString();
    }

    @Override
    @BinderCall
    public void clearProcess(IProcessClearListener listener) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_CLEARPROCESS, listener)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public void setLockKillDelay(long delay) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLDELAY, delay).sendToTarget();
    }

    @Override
    @BinderCall
    public long getLockKillDelay() {
        enforceCallingPermissions();
        return mLockKillDelay;
    }

    @Override
    public void setWhiteSysAppEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETWHITESYSAPPENABLED, enabled)
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
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETBOOTBLOCKENABLED, enabled)
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
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETSTARTBLOCKENABLED, enabled)
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
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETLOCKKILLENABLED, enabled)
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
    public void setRFKillEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETRFKILLENABLED, enabled)
                .sendToTarget();
    }

    @Override
    @BinderCall
    public boolean isRFKillEnabled() {
        enforceCallingPermissions();
        return !mIsSafeMode && mRootActivityFinishKillEnabled.get();
    }

    @Override
    public void setGreeningEnabled(boolean enabled) {
        enforceCallingPermissions();
        mainHandler.obtainMessage(AshManHandlerMessages.MSG_SETGREENINGENABLED, enabled)
                .sendToTarget();
    }

    @Override
    public boolean isGreeningEnabled() {
        return false;//FIXME!!!!!!
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

        if (args == null || args.length == 0) {

            mAppGuardService.dump(fd, fout, args);

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

                // Dump while list.
                fout.println("White list hook: ");
                Collections.consumeRemaining(RepoProxy.getProxy()
                        .getWhite_list_hooks_dynamic()
                        .getAll(), new Consumer<String>() {
                    @Override
                    public void accept(String o) {
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
                Collections.consumeRemaining(RepoProxy.getProxy().getBoots().getAll(), new Consumer<String>() {
                    @Override
                    public void accept(String o) {
                        fout.println(o);
                    }
                });

                fout.println();
                fout.println("======================");
                fout.println();

                // Dump start list.
                fout.println("Start list: ");
                Collections.consumeRemaining(RepoProxy.getProxy().getStarts().getAll(), new Consumer<String>() {

                    @Override
                    public void accept(String s) {
                        fout.println(s);
                    }
                });

                fout.println();
                fout.println("======================");
                fout.println();

                // Dump lk list.
                fout.println("LK list: ");
                Collections.consumeRemaining(RepoProxy.getProxy().getLks().getAll(), new Consumer<String>() {

                    @Override
                    public void accept(String s) {
                        fout.println(s);
                    }
                });

                fout.println();
                fout.println("======================");
                fout.println();

                // Dump rf list.
                fout.println("RF list: ");
                Collections.consumeRemaining(RepoProxy.getProxy().getRfks().getAll(), new Consumer<String>() {

                    @Override
                    public void accept(String s) {
                        fout.println(s);
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

                // Dump webview.
                fout.println("Webview provider list: ");
                Object[] wwListObjects = mWebviewProviders.toArray();
                Collections.consumeRemaining(wwListObjects, new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        fout.println(o);
                    }
                });

                // Dump block list.
                fout.println("Block record list: ");
                Object[] blockRecordObjects = mBlockRecords.values().toArray();
                Collections.consumeRemaining(blockRecordObjects, new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        fout.println(o);
                    }
                });
            }
        } else {
            // Exe command.
            new AshShellCommand(this).exec(this, null, fd, null, args);
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

    protected static void enforceDebugBuild() {
        if (!BuildConfig.DEBUG) {
            throw new SecurityException("User build does not require permission to interact with X-APM-S");
        }
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

    private void onServiceStartBy(String serviceName, int starterUid) {
        if (isPowerSaveModeEnabled()) {
            return;
        }
        mServiceStartRecords.put(serviceName, starterUid);
    }

    @Override
    public String getServiceStarter(ComponentName service) {
        if (service == null) return null;
        String key = service.flattenToString();
        Integer uid = mServiceStartRecords.get(key);
        if (uid == null) return null;
        return PkgUtil.pkgForUid(getContext(), uid);
    }

    private AtomicBoolean mInactiveInsteadOfKillAppInstead = new AtomicBoolean(false);

    @Override
    public boolean isInactiveAppInsteadOfKillPreferred() {
        return mInactiveInsteadOfKillAppInstead.get();
    }

    @Override
    public void setInactiveAppInsteadOfKillPreferred(boolean prefer) {
        SettingsProvider.get().putBoolean("INACTIVE_INSTEAD_OF_KILL", prefer);
        mInactiveInsteadOfKillAppInstead.set(prefer);
    }

    @Override
    public void mockSystemDead(long delay) {
        enforceCallingPermissions();

        if (BuildConfig.DEBUG) {
            new TestXposedMethod().main();
        }

        mainHandler.postDelayed(() -> {
            throw new IllegalStateException("Mock system dead by user, bye!");
        }, delay);
    }

    @Override
    public void clearModuleSettings(String moduleVar) {
        enforceCallingPermissions();

        if (moduleVar.equals(XAppBuildVar.APP_BLUR)) {
            RepoProxy.getProxy().getBlurs().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_BOOT)) {
            RepoProxy.getProxy().getBoots().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_COMP_REPLACE)) {
            RepoProxy.getProxy().getComponentReplacement().clear();
        }
        if (moduleVar.equals(XAppBuildVar.APP_DATA_CLEAR)) {
            RepoProxy.getProxy().getUninstall().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_DOZE)) {
            RepoProxy.getProxy().getDoze_whitelist_adding().removeAll();
            RepoProxy.getProxy().getDoze_whitelist_removal().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_FIREWALL)) {
            RepoProxy.getProxy().getData_restrict().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_LAZY)) {
            RepoProxy.getProxy().getLazy().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_LK)) {
            RepoProxy.getProxy().getLks().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_LOCK)) {
            RepoProxy.getProxy().getLocks().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_OPS)) {
            RepoProxy.getProxy().getPerms().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_PRIVACY)) {
            RepoProxy.getProxy().getPrivacy().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_RESIDENT)) {
            RepoProxy.getProxy().getResident().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_RFK)) {
            RepoProxy.getProxy().getRfks().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_UNINSTALL)) {
            RepoProxy.getProxy().getUninstall().removeAll();
        }
        if (moduleVar.equals(XAppBuildVar.APP_START)) {
            RepoProxy.getProxy().getStarts().removeAll();
        }
    }

    @Override
    public boolean isDisableMotionEnabled() {
        return mDisableMotionEnabled.get();
    }

    @Override
    public void setDisableMotionEnabled(boolean enable) {
        enforceCallingPermissions();
        if (mDozeHandler != null) {
            mDozeHandler.obtainMessage(DozeHandlerMessages.MSG_SETDISABLEMOTIONENABLED, enable).sendToTarget();
        }
    }

    @BinderCall
    @Override
    public List<OpLog> getOpLogForPackage(String packageName) {
        return mOpsCache.getLogForPackage(packageName);
    }

    @BinderCall
    @Override
    public List<OpLog> getOpLogForOp(int code) {
        return mOpsCache.getLogForOp(code);
    }

    @Override
    public void clearOpLogForPackage(String packageName) throws RemoteException {
        XposedLog.verbose("clearOpLogForPackage: " + packageName);
        mOpsCache.clearOpLogForPackage(packageName);
    }

    @Override
    public void clearOpLogForOp(int cod) throws RemoteException {
        XposedLog.verbose("clearOpLogForOp: " + cod);
        mOpsCache.clearOpLogForOp(cod);
    }

    @Override
    public String getUserName() {
        long ident = Binder.clearCallingIdentity();
        try {
            UserManager um = (UserManager) getContext().getSystemService(Context.USER_SERVICE);
            if (um != null) {
                return um.getUserName();
            }
            return null;
        } catch (Throwable e) {
            XposedLog.wtf("getUserName: " + e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    @Override
    public Bitmap getUserIcon() {
        long ident = Binder.clearCallingIdentity();
        try {
            UserManager um = (UserManager) getContext().getSystemService(Context.USER_SERVICE);
            if (um != null) {
                return um.getUserIcon(UserHandle.USER_CURRENT);
            }
            return null;
        } catch (Throwable e) {
            XposedLog.wtf("getUserIcon: " + e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    @Override
    @BinderCall
    public void addPendingDisableApps(String pkg) {
        XposedLog.verbose("addPendingDisableApps: " + pkg);
        RepoProxy.getProxy().getPending_disable_apps().add(pkg);
    }

    @BinderCall
    @Override
    public void addPowerSaveWhitelistApp(String pkg) {
        mDeviceIdleController.addPowerSaveWhitelistAppInternal(pkg);
        RepoProxy.getProxy().getDoze_whitelist_adding().add(pkg);
        RepoProxy.getProxy().getDoze_whitelist_removal().remove(pkg);
    }

    @BinderCall
    @Override
    public void removePowerSaveWhitelistApp(String pkg) {
        mDeviceIdleController.removePowerSaveWhitelistAppInternal(pkg);
        RepoProxy.getProxy().getDoze_whitelist_removal().add(pkg);
        RepoProxy.getProxy().getDoze_whitelist_adding().remove(pkg);
    }

    @BinderCall
    @Override
    public String[] getFullPowerWhitelist() {
        return mDeviceIdleController.getFullPowerWhitelistInternal();
    }

    @BinderCall
    @Override
    public String[] getUserPowerWhitelist() {
        return mDeviceIdleController.getUserPowerWhitelistInternal();
    }

    @Override
    @BinderCall
    public ActivityManager.MemoryInfo getMemoryInfo() {
        long ident = Binder.clearCallingIdentity();
        try {
            ActivityManager.MemoryInfo m = new ActivityManager.MemoryInfo();
            ActivityManagerNative.getDefault().getMemoryInfo(m);
            return m;
        } catch (Throwable e) {
            XposedLog.wtf("getMemoryInfo: " + Log.getStackTraceString(e));
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
        return new ActivityManager.MemoryInfo();
    }

    private void wrapCallingIdetUnCaught(Runnable r) {
        long idet = Binder.clearCallingIdentity();
        try {
            r.run();
        } finally {
            Binder.restoreCallingIdentity(idet);
        }
    }

    @Override
    @BinderCall
    public void enableKeyguard(final boolean enabled) {
        if (mPhoneWindowManagerProxy != null) {
            wrapCallingIdetUnCaught(new ErrorCatchRunnable(new Runnable() {
                @Override
                public void run() {
                    mPhoneWindowManagerProxy.enableKeyguard(enabled);
                }
            }, "enableKeyguard"));
        }
    }

    @BinderCall
    @Override
    public void exitKeyguardSecurely(final IBooleanCallback1 result) {
        XposedLog.verbose("exitKeyguardSecurely: " + mPhoneWindowManagerProxy);
        if (mPhoneWindowManagerProxy != null) {
            wrapCallingIdetUnCaught(new ErrorCatchRunnable(new Runnable() {
                @Override
                public void run() {
                    mPhoneWindowManagerProxy.exitKeyguardSecurely(new WindowManagerPolicy
                            .OnKeyguardExitResult() {
                        @Override
                        public void onKeyguardExitResult(boolean success) {
                            if (result != null) {
                                try {
                                    result.onResult(success);
                                } catch (RemoteException e) {
                                    XposedLog.wtf("exitKeyguardSecurely,  result.onResult: " + e);
                                }
                            }
                        }
                    });
                }
            }, "exitKeyguardSecurely"));
        }
    }

    @BinderCall
    @Override
    public void dismissKeyguardLw() {
        if (mPhoneWindowManagerProxy != null) {
            wrapCallingIdetUnCaught(new ErrorCatchRunnable(new Runnable() {
                @Override
                public void run() {
                    mPhoneWindowManagerProxy.dismissKeyguardLw();
                }
            }, "dismissKeyguardLw"));
        }
    }

    @BinderCall
    @Override
    public boolean isKeyguardLocked() {
        if (mPhoneWindowManagerProxy != null) {
            return mPhoneWindowManagerProxy.isKeyguardLocked();
        }
        return false;
    }

    @Override
    @BinderCall
    public int getRunningProcessCount() {
        return PkgUtil.getRunningAppsCount(getContext());
    }

    @BinderCall
    @Override
    public String[] getSystemPowerWhitelist() {
        return mDeviceIdleController.getSystemPowerWhitelistInternal();
    }

    @SuppressLint("HandlerLeak")
    private class HandlerImpl extends Handler implements AshManHandler {

        HandlerImpl() {
        }

        public HandlerImpl(Looper looper) {
            super(looper);
        }

        private final Runnable clearProcessRunnable = () -> {
            try {
                clearProcess(null);
            } catch (Throwable e) {
                XposedLog.wtf("Error on clearProcessRunnable: " + Log.getStackTraceString(e));
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
                case AshManHandlerMessages.MSG_SETSTARTRULEENABLED:
                    HandlerImpl.this.setStartRuleEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETLOCKKILLENABLED:
                    HandlerImpl.this.setLockKillEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETSHOWAPPPROCESSUPDATENOTIFICATIONENABLED:
                    HandlerImpl.this.setShowAppProcessUpdateNotificationEnabled((Boolean) msg.obj);
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
                    @SuppressWarnings("unchecked") Pair<String, Boolean> extra = (Pair<String, Boolean>) msg.obj;
                    boolean tmp = extra.second;
                    String pkg = extra.first;
                    HandlerImpl.this.setApplicationEnabledSetting(pkg, msg.arg1, msg.arg2, tmp);
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
                case AshManHandlerMessages.MSG_SETAUTOADDBLACKNOTIFICATIONENABLED:
                    HandlerImpl.this.setAutoAddBlackNotificationEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_FORCERELOADPACKAGES:
                    HandlerImpl.this.forceReloadPackages();
                    break;
                case AshManHandlerMessages.MSG_SETPERMISSIONCONTROLENABLED:
                    HandlerImpl.this.setPermissionControlEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETUSERDEFINEDANDROIDID:
                    HandlerImpl.this.setUserDefinedAndroidId((String) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETUSERDEFINEDDEVICEID:
                    HandlerImpl.this.setUserDefinedDeviceId((String) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETUSERDEFINEDLINE1NUMBER:
                    HandlerImpl.this.setUserDefinedLine1Number((String) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETSHOWFOCUSEDACTIVITYINFOENABLED:
                    HandlerImpl.this.setShowFocusedActivityInfoEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETGREENINGENABLED:
                    HandlerImpl.this.setGreeningEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_RESTOREDEFAULTSETTINGS:
                    HandlerImpl.this.restoreDefaultSettings();
                    break;
                case AshManHandlerMessages.MSG_ONAPPLICATIONUNCAUGHTEXCEPTION:
                    UncaughtException ue = (UncaughtException) msg.obj;
                    HandlerImpl.this.onApplicationUncaughtException(ue.packageName, ue.thread, ue.exception, ue.trace);
                    break;
                case AshManHandlerMessages.MSG_SETAPPCRASHDUMPENABLED:
                    HandlerImpl.this.setAppCrashDumpEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETLAZYMODEENABLED:
                    HandlerImpl.this.setLazyModeEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETLAZYRULEENABLED:
                    HandlerImpl.this.setLazyRuleEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETLPBKENABLED:
                    HandlerImpl.this.setLPBKEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETDONOTKILLSBNENABLED:
                    @SuppressWarnings("unchecked") Pair<Boolean, String> data = (Pair<Boolean, String>) msg.obj;
                    HandlerImpl.this.setDoNotKillSBNEnabled(data.first, data.second);
                    break;
                case AshManHandlerMessages.MSG_SETTASKREMOVEKILLENABLED:
                    HandlerImpl.this.setTaskRemoveKillEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETPRIVACYENABLED:
                    HandlerImpl.this.setPrivacyEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETRESIDENTENABLED:
                    HandlerImpl.this.setResidentEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETPANICHOMEENABLED:
                    HandlerImpl.this.setPanicHomeEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETPANICLOCKENABLED:
                    HandlerImpl.this.setPanicLockEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETPOWERSAVEMODEENABLED:
                    HandlerImpl.this.setPowerSaveModeEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETWAKEUPONNOTIFICATIONENABLED:
                    HandlerImpl.this.setWakeupOnNotificationEnabled((Boolean) msg.obj);
                    break;
                case AshManHandlerMessages.MSG_SETAPPSERVICELAZYCONTROLSOLUTION:
                    HandlerImpl.this.setAppServiceLazyControlSolution(msg.arg1, (Boolean) msg.obj);
                    break;
            }
        }

        @Override
        public void setPrivacyEnabled(boolean enabled) {
            if (mPrivacyEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.PRIVACY_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setWhiteSysAppEnabled(boolean enabled) {
            if (mWhiteSysAppEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.ASH_WHITE_SYS_APP_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setDoNotKillSBNEnabled(boolean enabled, String module) {
            XposedLog.verbose("setDoNotKillSBNEnabled %s %s", enabled, module);
            if (module.equals(XAppBuildVar.APP_LK)) {
                if (mDoNotKillSBNEnabled.compareAndSet(!enabled, enabled)) {
                    SystemSettings.ASH_WONT_KILL_SBN_APP_B.writeToSystemSettings(getContext(), enabled);
                }
            } else {
                if (mDoNotKillSBNGreenEnabled.compareAndSet(!enabled, enabled)) {
                    SystemSettings.ASH_WONT_KILL_SBN_APP_GREEN_B.writeToSystemSettings(getContext(), enabled);
                }
            }
        }

        @Override
        public void setTaskRemoveKillEnabled(boolean enabled) {
            if (mTaskRemovedKillEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.REMOVE_TASK_KILL_ENABLED_B.writeToSystemSettings(getContext(), enabled);
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
        public void setStartRuleEnabled(boolean enabled) {
            if (mStartRuleEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_START_RULE_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setLazyRuleEnabled(boolean enabled) {
            if (mLazyRuleEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_LAZY_RULE_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setAppServiceLazyControlSolution(int solutionFlags, boolean enabled) {
            if (solutionFlags == XAshmanManager.AppServiceControlSolutions.FLAG_FW) {
                mLazySolutionFW.set(enabled);
                SystemSettings.APM_LAZY_SOLUTION_FW_B.writeToSystemSettings(getContext(), enabled);
            }

            if (solutionFlags == XAshmanManager.AppServiceControlSolutions.FLAG_APP) {
                mLazySolutionApp.set(enabled);
                SystemSettings.APM_LAZY_SOLUTION_APP_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setLockKillEnabled(boolean enabled) {
            if (mLockKillEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.LOCK_KILL_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setShowAppProcessUpdateNotificationEnabled(boolean enabled) {
            if (mShowAppProcessUpdateNotification.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_SHOW_APP_PROCESS_UPDATE_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setResidentEnabled(boolean enabled) {
            if (mResidentEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_RESIDENT_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setPowerSaveModeEnabled(boolean enabled) {
            if (mPowerSaveModeEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_POWER_SAVE_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setPanicHomeEnabled(boolean enabled) {
            if (mPanicHomeEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_PANIC_HOME_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setPanicLockEnabled(boolean enabled) {
            if (mPanicLockEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.APM_PANIC_LOCK_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setAutoAddBlackEnable(boolean enabled) {
            if (mAutoAddToBlackListForNewApp.compareAndSet(!enabled, enabled)) {
                SystemSettings.AUTO_BLACK_FOR_NEW_INSTALLED_APP_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setAutoAddBlackNotificationEnabled(boolean value) {
            if (mAutoAddNotificationToBlackListForNewApp.compareAndSet(!value, value)) {
                SystemSettings.AUTO_BLACK_NOTIFICATION_FOR_NEW_INSTALLED_APP_B.writeToSystemSettings(getContext(), value);
            }
        }

        @Override
        public void setWakeupOnNotificationEnabled(boolean enable) {
            if (mWakeupOnNotificationPosted.compareAndSet(!enable, enable)) {
                SystemSettings.WAKE_UP_ON_NOTIFICATION_POSTED_ENABLED_B.writeToSystemSettings(getContext(), enable);
            }
        }

        @Override
        public void forceReloadPackages() {
            XposedLog.verbose("forceReloadPackages");
            mWorkingService.execute(() -> {
                cachePackages();

                cacheGCMPackages();

                // Remove onwer package to fix previous bugs.
                try {
                    RepoProxy.getProxy().getBoots().remove(BuildConfig.APPLICATION_ID);
                    RepoProxy.getProxy().getStarts().remove(BuildConfig.APPLICATION_ID);
                    RepoProxy.getProxy().getRfks().remove(BuildConfig.APPLICATION_ID);
                    RepoProxy.getProxy().getGreens().remove(BuildConfig.APPLICATION_ID);
                    RepoProxy.getProxy().getLks().remove(BuildConfig.APPLICATION_ID);
                    RepoProxy.getProxy().getPrivacy().remove(BuildConfig.APPLICATION_ID);
                    RepoProxy.getProxy().getWhite_list_hooks_dynamic().reloadAsync();
                } catch (Throwable e) {
                    XposedLog.wtf("Fail remove owner package from repo: " + Log.getStackTraceString(e));
                }
            });
        }

        @Override
        public void setPermissionControlEnabled(boolean enabled) {
            if (mPermissionControlEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.PERMISSION_CONTROL_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setUserDefinedAndroidId(String id) {
            mUserDefinedAndroidId.setData(id);
            SystemSettings.USER_DEFINED_ANDROID_ID_T_S
                    .writeToSystemSettings(getContext(), id);
        }

        @Override
        public void setUserDefinedDeviceId(String id) {
            mUserDefinedDeviceId.setData(id);
            SystemSettings.USER_DEFINED_DEVICE_ID_T_S
                    .writeToSystemSettings(getContext(), id);
        }

        @Override
        public void setUserDefinedLine1Number(String id) {
            mUserDefinedLine1Number.setData(id);
            SystemSettings.USER_DEFINED_LINE1_NUM_T_S
                    .writeToSystemSettings(getContext(), id);
        }

        @Override
        public void setShowFocusedActivityInfoEnabled(boolean enabled) {
            if (mShowFocusedActivityInfoEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.SHOW_FOCUSED_ACTIVITY_INFO_B.writeToSystemSettings(getContext(), enabled);
            }

            // Hide float view in lazy handler.
            if (!enabled) {
                mLazyHandler.post(new ErrorCatchRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (mFloatView != null) {
                            try {
                                mFloatView.hideAndDetach();
                                mFloatView = null;
                            } catch (Throwable e) {
                                XposedLog.wtf("Fail detach float view: " + Log.getStackTraceString(e));
                            }
                        }
                    }
                }, "hideAndDetach"));
            }
        }

        @Override
        public void setGreeningEnabled(boolean enabled) {
            if (mGreeningEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.GREENING_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void restoreDefaultSettings() {
            SystemSettings.restoreDefault(getContext());
            RepoProxy.getProxy().deleteAll();
            loadConfigFromSettings();
            SettingsProvider.get().putString("AppInstalledAutoApplyTemplate", "NULL");
        }

        // Only show one dialog at one time.
        private boolean mCrashDialogShowing;

        @SuppressWarnings("ConstantConditions")
        @Override
        public void onApplicationUncaughtException(String packageName, String thread, String exception, final String trace) {

            // This package is going to die.
            PkgUtil.onAppBringDown(packageName, "onApplicationUncaughtException");

            if (mCrashDialogShowing) return;

            if (!mShowAppCrashDumpEnabled.get()) {
                mCrashDialogShowing = false;
                return;
            }

            try {
                AlertDialog d = new AlertDialog.Builder(getContext())
                        .setTitle("调试模式")
                        .setMessage(String.format(
                                "应用管理检测到 %s 发生了异常，已经为你取得了错误信息与堆栈，截图反馈给开发者或许可以帮助解决该问题。\n\n" +
                                        "错误线程：%s\n" +
                                        "堆栈：%s\n\n" +
                                        "你也可以在Xposed日志中查看该错误信息。",
                                PkgUtil.loadNameByPkgName(getContext(), packageName),
                                thread,
                                trace
                        ))
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.copy,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            ClipboardManager cmb = (ClipboardManager) getContext()
                                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                                            if (cmb != null) {
                                                cmb.setPrimaryClip(ClipData.newPlainText("service_config", trace));
                                            }
                                        } catch (Throwable ignored) {
                                        }
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mCrashDialogShowing = false;
                            }
                        })
                        .create();
                d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
                d.show();
                mCrashDialogShowing = true;

            } catch (Exception e) {
                XposedLog.wtf("Fail show system dialog: " + Log.getStackTraceString(e));
            }
        }

        @Override
        public void setAppCrashDumpEnabled(boolean enabled) {
            if (mShowAppCrashDumpEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.SHOW_CRASH_DUMP_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setLockKillDoNotKillAudioEnabled(boolean enabled) {
            if (mLockKillDoNotKillAudioEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.LOCK_KILL_DONT_KILL_AUDIO_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setLazyModeEnabled(boolean enabled) {
            if (mLazyEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.LAZY_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setRFKillEnabled(boolean enabled) {
            if (mRootActivityFinishKillEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.ROOT_ACTIVITY_KILL_ENABLED_B.writeToSystemSettings(getContext(), enabled);
            }
        }

        @Override
        public void setLPBKEnabled(boolean enabled) {
            if (mLongPressBackKillEnabled.compareAndSet(!enabled, enabled)) {
                SystemSettings.LONG_PRESS_BACK_KILL_ENABLED_B.writeToSystemSettings(getContext(), enabled);
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
            boolean doNotKillAppWithSBNEnabled = isDoNotKillSBNEnabled(XAppBuildVar.APP_LK);
            XposedLog.verbose("clearProcess, doNotKillAppWithSBNEnabled: " + doNotKillAppWithSBNEnabled);

            if (XposedLog.isVerboseLoggable()) {
                dumpNotifications();
            }

            boolean doNotCleatWhenInter = true;
            if (listener != null) try {
                doNotCleatWhenInter = listener.doNotClearWhenIntervative();
            } catch (RemoteException ignored) {

            }

            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose(TAG_LK + "clearProcess!!! doNotCleatWhenInter: " + doNotCleatWhenInter);
            }

            if (listener != null) try {
                listener.onPrepareClearing();
            } catch (RemoteException ignored) {

            }

            final boolean finalDoNotClearWhenInter = doNotCleatWhenInter;

            FutureTask<String[]> futureTask = new FutureTask<>(new SignalCallable<String[]>() {

                @Override
                public String[] call() {

                    PowerManager power = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
                    ActivityManager am = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
                    if (am == null) return null;

                    Set<String> runningPackages = PkgUtil.getRunningProcessPackages(getContext());
                    if (BuildConfig.DEBUG) {
                        XposedLog.verbose(TAG_LK + "Running packages: " + runningPackages.toString());
                    }

                    String[] packagesToClear = getLKApps(true);
                    int count = packagesToClear.length;

                    if (listener != null) try {
                        listener.onStartClearing(count);
                    } catch (RemoteException ignored) {

                    }

                    String[] cleared = new String[count];

                    for (int i = 0; i < count; i++) {
                        // Check if canceled.
                        if (power != null && (finalDoNotClearWhenInter && power.isInteractive())) {
                            XposedLog.wtf(TAG_LK + "isInteractive, skip clearing");
                            return cleared;
                        }

                        String runningPackageName = packagesToClear[i];

                        if (!runningPackages.contains(runningPackageName)) {
                            if (XposedLog.isVerboseLoggable()) {
                                XposedLog.verbose(TAG_LK + "Won't kill app which not running: " + runningPackageName);
                            }
                            if (listener != null) try {
                                listener.onIgnoredPkg(null, "Not running");
                            } catch (RemoteException ignored) {

                            }
                            continue;
                        }

                        if (isLockKillDoNotKillAudioEnabled()
                                && runningPackageName.equals(mAudioFocusedPackage.getData())) {
                            if (XposedLog.isVerboseLoggable()) {
                                XposedLog.verbose(TAG_LK + "Won't kill app with audio focus: " + runningPackageName);
                            }
                            if (listener != null) try {
                                listener.onIgnoredPkg(null, "Audio focused");
                            } catch (RemoteException ignored) {

                            }
                            continue;
                        }

                        if (PkgUtil.isAppRunningForeground(getContext(), runningPackageName)) {

                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "foreground-app");
                            } catch (RemoteException ignored) {

                            }

                            if (XposedLog.isVerboseLoggable())
                                XposedLog.verbose(TAG_LK + "App is in foreground, wont kill: " + runningPackageName);
                            continue;
                        }

//                        if (PkgUtil.isDefaultSmsApp(getContext(), runningPackageName)) {
//
//                            addToWhiteList(runningPackageName);
//
//                            if (listener != null) try {
//                                listener.onIgnoredPkg(runningPackageName, "sms-app");
//                            } catch (RemoteException ignored) {
//
//                            }
//
//                            if (XposedLog.isVerboseLoggable()) {
//                                XposedLog.verbose(TAG_LK + "App is in isDefaultSmsApp, wont kill: " + runningPackageName);
//                            }
//                            continue;
//                        }

                        if (isDoNotKillSBNEnabled(XAppBuildVar.APP_LK)
                                && hasNotificationForPackageInternal(runningPackageName)) {

                            if (listener != null) try {
                                listener.onIgnoredPkg(runningPackageName, "sbn-app");
                            } catch (RemoteException ignored) {

                            }

                            if (XposedLog.isVerboseLoggable()) {
                                XposedLog.verbose(TAG_LK + "SBN app, wont kill: " + runningPackageName);
                            }
                            continue;
                        }

                        if (listener != null) try {
                            listener.onClearingPkg(runningPackageName);
                        } catch (RemoteException ignored) {

                        }

                        // Clearing using kill command.
                        if (power != null && (finalDoNotClearWhenInter && power.isInteractive())) {
                            XposedLog.wtf(TAG_LK + "isInteractive, skip clearing");
                            return cleared;
                        }

                        getAppIdler().setAppIdle(runningPackageName);

                        cleared[i] = runningPackageName;

                        XposedLog.verbose(TAG_LK + "Force stopped: " + runningPackageName);

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
            Runnable clear = () -> {
                synchronized (mBlockRecords) {
                    mBlockRecords.clear();
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

            // Re-disable apps.
            try {
                if (RepoProxy.getProxy().getPending_disable_apps().size() == 0) return;
                // Disable pending apps.
                for (String p : RepoProxy.getProxy().getPending_disable_apps().getAll()) {
                    if (!isPackageRunningOnTop(p)) {
                        // Do not remove from pending disable.
                        setApplicationEnabledSetting(p, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0, true);
                        XposedLog.verbose("Disable pending apps: " + p);
                        // RepoProxy.getProxy().getPending_disable_apps().remove(p);
                    }
                }
            } catch (Throwable e) {
                XposedLog.wtf("Fail handle disable_app: " + e);
            }
        }

        @Override
        public void onScreenOn() {
            cancelProcessClearing("SCREEN ON");
        }

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
                boolean oldValue = isRestrictOnData(uid);
                if (oldValue == restrict) {
                    XposedLog.debug("restrictAppOnData oldValue == restrict: " + uid);
                    return;
                }
            }

            try {
                boolean success = BandwidthCommandCompat.restrictAppOnData(
                        mNativeDaemonConnector,
                        uid, restrict, mDataInterfaceName);
                XposedLog.debug("NativeDaemonConnector execute success: " + success);

                synchronized (mQuotaLock) {
                    if (success) {
                        NetworkRestriction clean = new NetworkRestriction(POLICY_REJECT_ON_DATA, uid);
                        NetworkRestriction clean2 = new NetworkRestriction(POLICY_REJECT_NONE, uid);
                        int policy = restrict ? POLICY_REJECT_ON_DATA : POLICY_REJECT_NONE;
                        NetworkRestriction match = new NetworkRestriction(policy, uid);
                        mDataBlackList.remove(clean.toJson());
                        mDataBlackList.remove(clean2.toJson());
                        mDataBlackList.add(match.toJson());
                    }
                }
            } catch (Exception e) {
                XposedLog.wtf("Fail restrictAppOnData: " + Log.getStackTraceString(e));
            }
        }

        @Override
        public void restrictAppOnWifi(int uid, boolean restrict, boolean force) {

            if (!force) synchronized (mQuotaLock) {
                boolean oldValue = isRestrictOnWifi(uid);
                if (oldValue == restrict) {
                    XposedLog.debug("restrictAppOnWifi oldValue == restrict: " + uid);
                    return;
                }
            }

            try {
                boolean success = BandwidthCommandCompat.restrictAppOnWifi(
                        mNativeDaemonConnector, uid,
                        restrict, mWifiInterfaceName);
                XposedLog.debug("NativeDaemonConnector execute success: " + success);

                synchronized (mQuotaLock) {
                    if (success) {
                        NetworkRestriction clean = new NetworkRestriction(POLICY_REJECT_ON_WIFI, uid);
                        NetworkRestriction clean2 = new NetworkRestriction(POLICY_REJECT_NONE, uid);
                        int policy = restrict ? POLICY_REJECT_ON_WIFI : POLICY_REJECT_NONE;
                        NetworkRestriction match = new NetworkRestriction(policy, uid);
                        mWifiBlackList.remove(clean.toJson());
                        mWifiBlackList.remove(clean2.toJson());
                        mWifiBlackList.add(match.toJson());
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

            // Add to repo.
            if (newState == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                RepoProxy.getProxy().getComps().add(componentName.flattenToString());
            } else {
                RepoProxy.getProxy().getComps().remove(componentName.flattenToString());
            }
        }

        @Override
        public int getComponentEnabledSetting(ComponentName componentName) {
            PackageManager pm = getContext().getPackageManager();
            return pm.getComponentEnabledSetting(componentName);
        }

        @Override
        public void setApplicationEnabledSetting(String packageName, int newState, int flags, boolean tmp) {
            XposedLog.verbose("setApplicationEnabledSetting %s %s %s %s", packageName, newState, flags, tmp);
            PackageManager pm = getContext().getPackageManager();
            pm.setApplicationEnabledSetting(packageName, newState, flags);

            if (!tmp) {
                // Remove this pkg from pending disable apps.
                XposedLog.verbose("Remove pending disables for " + packageName);
                RepoProxy.getProxy().getPending_disable_apps_tr().remove(packageName);
                RepoProxy.getProxy().getPending_disable_apps().remove(packageName);
            }
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
            // FIXME Impl is needed.
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

    // This is updated with a short delay to give a short time for us to handle back event.
    private final Holder<String> mTopPackageDelay = new Holder<>();
    // This is updated no delay.
    private final Holder<String> mTopPackageImd = new Holder<>();

    private class LazyHandler extends Handler implements AshManLZHandler {

        public LazyHandler(Looper looper) {
            super(looper);
        }

        LazyHandler() {
        }

        @Override
        public void onStartProcessLocked(ApplicationInfo applicationInfo) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("onStartProcessLocked: " + applicationInfo);
            }
            if (applicationInfo != null && applicationInfo.packageName != null) {
                addToRunningProcessPackages(applicationInfo.packageName);

                // Check lazy settings.
                // Process add happen before activity report.
                // So delay check.
                boolean shouldCheck = isLazyModeEnabled()
                        && isPackageLazyByUser(applicationInfo.packageName)
                        && !isPackageRunningOnTop(applicationInfo.packageName);

                if (shouldCheck) {
                    ErrorCatchRunnable processLazyChecker = new ErrorCatchRunnable(() -> {
                        if (!isPackageRunningOnTop(applicationInfo.packageName)) {
                            postLazyServiceKillerIfNecessary(applicationInfo.packageName,
                                    LAZY_KILL_SERVICE_PROCESS_INTERVAL,
                                    "Process start background");
                        } else {
                            XposedLog.verbose("App is now on top, skip lazy checker.");
                        }
                    }, "processLazyChecker");
                    postDelayed(processLazyChecker, LAZY_CHECK_PACKAGE_PROCESS_DELAY);
                }
            }
        }

        @Override
        public void onRemoveProcessLocked(ApplicationInfo applicationInfo, boolean callerWillRestart, boolean allowRestart, String reason) {
            if (XposedLog.isVerboseLoggable()) {
                XposedLog.verbose("onRemoveProcessLocked: %s %s %s %s ", applicationInfo, callerWillRestart, allowRestart, reason);
            }
            if (applicationInfo != null && applicationInfo.packageName != null) {
                removeFromRunningProcessPackages(applicationInfo.packageName);

                // Notify package manager that this app is dead.
                PkgUtil.onAppBringDown(applicationInfo.packageName, "onRemoveProcessLocked");
            }
        }

        @Override
        @Deprecated
        public void onActivityDestroy(Intent intent) {

        }

        @Override
        public void onPackageMoveToFront(String who) {
            String from = mTopPackageImd.getData();
            if (who != null && !who.equals(from)) {
                mTopPackageImd.setData(who);
                postNotifyTopPackageChanged(from, who);

                // Check if we need to add app process for this host.
                ErrorCatchRunnable er = new ErrorCatchRunnable(() -> addAppForLazyIfNeeded(who), "LAZY addAppForLazyIfNeeded");
                er.run();
            }
        }

        private void addAppForLazyIfNeeded(String who) {
            if (isLazyModeEnabledForPackage(who)) {
                if (isLazyRuleEnabled()) {
                    @LazyRuleCheck
                    boolean hasAddAppRule = RepoProxy.getProxy().getLazy_rules().has(constructAddAppRuleForLazy(who));
                    if (hasAddAppRule) {
                        XposedLog.verbose("LAZY, addAppForLazyIfNeeded: " + who);
                        addApp(who);
                    }
                }
            }
        }

        // ADDAPP ONLAUNCH com.tencent.mm
        private String[] constructAddAppRuleForLazy(String who) {
            String rule = String.format("ADDAPP ONLAUNCH %s", who);
            return new String[]{rule};
        }

        @Override
        public void onPackageMoveToFrontDelayUpdate(String who) {
            mTopPackageDelay.setData(who);
        }

        @Override
        public void onCompSetting(String pkg, boolean enable) {

        }

        @Override
        public void onBroadcastAction(Intent intent) {
            String action = intent.getAction();
            XposedLog.debug("mPackageReceiver action: " + action);
            if (action == null || intent.getData() == null) return;

            switch (action) {
                case Intent.ACTION_PACKAGE_ADDED:
                    String packageName = intent.getData().getSchemeSpecificPart();
                    if (packageName == null) return;

                    boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                    int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                    XposedLog.verbose("ACTION_PACKAGE_ADDED replacing:%s pkg:%s uid:",
                            replacing, packageName, uid);

                    // Cache this package sync.
                    cachePackages(packageName);

                    // We only add to black list when this is a new installed app.
                    if (!replacing) try {

                        XAshmanManager x = XAshmanManager.get();

                        boolean autoAdd = x.isServiceAvailable() && x.isAutoAddBlackEnabled();

                        XposedLog.verbose("ACTION_PACKAGE_ADDED autoAdd:%s", autoAdd);

                        if (autoAdd) {
                            if (!isInWhiteList(packageName)) {

                                // Do not apply for google vending.
                                if ("com.android.vending".equals(packageName)) {
                                    return;
                                }

                                // Apply template.
                                AppSettings template = getAppInstalledAutoApplyTemplate();
                                XposedLog.verbose("ACTION_PACKAGE_ADDED: " + template);

                                applyAppSettingsForPackage(packageName, template);

                                XposedLog.verbose("Apply app settings template for new app!!!!!!!!!!!");

                                // Ops.
                                // Note. Please see detailed comment @applyOpsSettingsForPackage.
                                final String finalPkgName = packageName;
                                postDelayed(new ErrorCatchRunnable(() -> {
                                    try {
                                        applyOpsSettingsForPackage(finalPkgName);

                                        boolean showNotification = isAutoAddBlackNotificationEnabled();
                                        if (showNotification) {
                                            showNewAppRestrictedNotification(getContext(),
                                                    finalPkgName,
                                                    String.valueOf(PkgUtil.loadNameByPkgName(getContext(), finalPkgName)));
                                        }

                                    } catch (Exception e) {
                                        XposedLog.verbose("Fail applyOpsSettingsForPackage/showNewAppRestrictedNotification: "
                                                + Log.getStackTraceString(e));
                                    }
                                }, "applyOpsSettingsForPackage"), 8000);
                            }
                        }


                        // Cache GCM packages async.
                        mWorkingService.execute(new ErrorCatchRunnable(XAshmanServiceImpl.this::cacheGCMPackages, "cacheGCMPackages"));

                    } catch (Throwable e) {
                        XposedLog.wtf(Log.getStackTraceString(e));
                    }
                    break;
                case Intent.ACTION_PACKAGE_REPLACED:
                    packageName = intent.getData().getSchemeSpecificPart();
                    if (packageName == null) return;
                    parsePackageAsync(packageName);
                    break;

                case Intent.ACTION_PACKAGE_REMOVED:
                    packageName = intent.getData().getSchemeSpecificPart();
                    if (packageName == null) return;

                    replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

                    // We will remove from cache and black list when this app is uninstall.
                    if (!replacing) try {
                        uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                        if (uid > 0) {
                            // FIXME Too slow.
                            String needRem = PkgUtil.pkgForUid(getContext(), uid);
                            if (needRem != null) {
                                int removed = mPackagesCache.remove(needRem);
                            }
                            XposedLog.debug("Package uninstalled, remove from cache: " + needRem);
                        }

                        XAshmanManager x = XAshmanManager.get();
                        x.addOrRemoveBootBlockApps(new String[]{packageName}, XAshmanManager.Op.REMOVE);
                        x.addOrRemoveRFKApps(new String[]{packageName}, XAshmanManager.Op.REMOVE);
                        x.addOrRemoveLKApps(new String[]{packageName}, XAshmanManager.Op.REMOVE);
                        x.addOrRemoveStartBlockApps(new String[]{packageName}, XAshmanManager.Op.REMOVE);

                        if (BuildConfig.APPLICATION_ID.equals(packageName)) {
                            mLazyHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    onAppGuardClientUninstalled();
                                }
                            }, 2000);
                        }
                    } catch (Throwable e) {
                        XposedLog.wtf(Log.getStackTraceString(e));
                    }
                    break;
            }


        }

        @Override
        public void notifyTopPackageChanged(String from, String to) {
            XAshmanServiceImpl.this.notifyTopPackageChanged(from, to);
        }

        private static final long LONG_PRESS_DETECTION_TIME_MILLS = 1500;
        private static final long BACK_PRESS_DETECTION_TIME_MILLS = 666;

        @Override
        public void onKeyEvent(KeyEvent keyEvent) {

            int keyCode = keyEvent.getKeyCode();
            int action = keyEvent.getAction();

            String currentPkg = getTopPackageDelay();

            if (BuildConfig.DEBUG) {
                XposedLog.verbose(XposedLog.PREFIX_KEY + "onKeyEvent: %s %s, current package: %s",
                        keyCode,
                        action,
                        currentPkg);
            }

            // Check for panic.
            boolean panicHandled = checkPanicEvent(keyCode, action, currentPkg);
            // This is a painc event, will not check more if it handle ok.
            if (panicHandled) return;

            boolean inKeyguard = isKeyguard();
            if (inKeyguard) {
                XposedLog.verbose("Ignore key event in keyguard for back key");
                return;
            }

            if (currentPkg == null) return;

            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (action == KeyEvent.ACTION_DOWN) {

                        if (hasMessages(AshManLZHandlerMessages.MSG_MAYBEBACKLONGPRESSED)
                                || hasMessages(AshManLZHandlerMessages.MSG_MAYBEBACKPRESSED)) {
                            XposedLog.verbose("Ignore back down event when we already has message in queue.");
                            return;
                        }

                        sendMessageDelayed(
                                obtainMessage(AshManLZHandlerMessages.MSG_MAYBEBACKLONGPRESSED, currentPkg),
                                LONG_PRESS_DETECTION_TIME_MILLS);

                    } else if (action == KeyEvent.ACTION_UP) {

                        // Key is up, remove long press detection.
                        boolean hasLongInQueue = hasMessages(AshManLZHandlerMessages.MSG_MAYBEBACKLONGPRESSED);

                        if (hasLongInQueue) {
                            removeMessages(AshManLZHandlerMessages.MSG_MAYBEBACKLONGPRESSED);

                            if (!hasMessages(AshManLZHandlerMessages.MSG_MAYBEBACKPRESSED)) {
                                sendMessageDelayed(
                                        obtainMessage(AshManLZHandlerMessages.MSG_MAYBEBACKPRESSED, currentPkg),
                                        BACK_PRESS_DETECTION_TIME_MILLS);
                            }
                        }
                    }

                    break;
            }
        }

        private static final int POWER_KEY_TIMES_PANIC = 5;
        private static final int POWER_KEY_PANIC_INTERVAL = 800;

        private AtomicInteger mPowerKeyPressTimes = new AtomicInteger(0);

        private Runnable mClearPowerkeyRunnable = this::resetPowerKeyTimes;

        private void resetPowerKeyTimes() {
            XposedLog.verbose("resetPowerKeyTimes");
            mPowerKeyPressTimes.set(0);
        }

        private int increasePowerKeyTimes() {
            try {
                return mPowerKeyPressTimes.incrementAndGet();
            } finally {
                removeCallbacks(mClearPowerkeyRunnable);
                postDelayed(mClearPowerkeyRunnable, POWER_KEY_PANIC_INTERVAL);
            }
        }

        private boolean checkPanicEvent(int keyCode, int action, String currentPkg) {
            XposedLog.verbose("checkPanicEvent");
            if (action != KeyEvent.ACTION_UP) {
                return false;
            }
            if (keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_POWER) {
                return false;
            }
            if (!isPanicHomeEnabled() && !isPanicLockEnabled()) {
                return false;
            }
            if (isPanicLockEnabled() && keyCode == KeyEvent.KEYCODE_POWER) {
                int powerTimes = mPowerKeyPressTimes.incrementAndGet();
                XposedLog.verbose("checkPanicEvent, powerTimes: " + powerTimes);
                if (powerTimes >= POWER_KEY_TIMES_PANIC) {
                    onPanicLock();
                    resetPowerKeyTimes();
                    removeCallbacks(mClearPowerkeyRunnable);
                    return true;
                } else {
                    int times = increasePowerKeyTimes();
                    XposedLog.verbose("checkPanicEvent, increase to: " + times);
                }
            }
            return false;
        }

        private void onPanicLock() {
            XposedLog.verbose("onPanicLock");
            DevicePolicyManager dpm = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm != null) {
                dpm.lockNow();
                vibrate();
            }
        }

        @SuppressLint("MissingPermission")
        private void vibrate() {
            Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(new long[]{10, 20, 20}, -1);
            }
        }

        @Override
        public void maybeBackLongPressed(String targetPackage) {
            XposedLog.verbose(XposedLog.PREFIX_KEY + "maybeBackLongPressed: " + targetPackage);

            if (isInWhiteList(targetPackage)) {
                return;
            }

            // Check if long press kill is enabled.
            boolean enabled = isLPBKEnabled();
            if (!enabled) {
                XposedLog.verbose(XposedLog.PREFIX_KEY + "maybeBackLongPressed not enabled");
                return;
            }

            boolean mayBeKillThisPackage = getTopPackageDelay() != null && getTopPackageDelay().equals(targetPackage);
            if (mayBeKillThisPackage) {
                XposedLog.verbose(XposedLog.PREFIX_KEY + "mayBeKillThisPackage after long back: " + targetPackage);
                getAppIdler().setAppIdle(targetPackage);
            }
        }

        @Override
        public void maybeBackPressed(String targetPackage) {
            String current = getTopPackageDelay();
            XposedLog.verbose("maybeBackPressed target: %s, current: %s", targetPackage, current);
            if (targetPackage != null && !targetPackage.equals(current)) {
                onBackPressed(targetPackage);
            }
        }

        private void onBackPressed(final String packageName) {
            XposedLog.verbose(XposedLog.PREFIX_KEY + "onBackPressed: " + packageName);

            if (packageName == null) return;

            if (!isRFKillEnabled()) {
                XposedLog.verbose(XposedLog.PREFIX_KEY + "PackageRFKill not enabled for all package");
                return;
            }

            if (!shouldRFKPackage(packageName)) {
                XposedLog.verbose(XposedLog.PREFIX_KEY + "PackageRFKill not enabled for this package");
                return;
            }

            boolean killPackageWhenBackPressed = !packageName.equals(getTopPackageDelay());

            if (killPackageWhenBackPressed) {
                postDelayed(new ErrorCatchRunnable(() -> {
                    try {
                        XposedLog.verbose(XposedLog.PREFIX_KEY + "Killing killPackageWhenBackPressed: " + packageName);

                        if (packageName.equals(getTopPackageDelay())) {
                            XposedLog.verbose(XposedLog.PREFIX_KEY + "Top package is now him, let it go~");
                            return;
                        }

                        getAppIdler().setAppIdle(packageName);
                    } catch (Throwable e) {
                        XposedLog.wtf(XposedLog.PREFIX_KEY + "Fail rf kill in runnable: " + Log.getStackTraceString(e));
                    }
                }, "killPackageWhenBackPressed"), 666);
            }
        }

        private String getTopPackageDelay() {
            return mTopPackageDelay.getData();
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
                case AshManLZHandlerMessages.MSG_ONPACKAGEMOVETOFRONTDELAYUPDATE:
                    LazyHandler.this.onPackageMoveToFrontDelayUpdate((String) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_ONCOMPSETTING:
                    LazyHandler.this.onCompSetting((String) msg.obj, msg.arg1 == 1);
                    break;
                case AshManLZHandlerMessages.MSG_ONBROADCASTACTION:
                    LazyHandler.this.onBroadcastAction((Intent) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_NOTIFYTOPPACKAGECHANGED:
                    @SuppressWarnings("unchecked")
                    Pair<String, String> p = (Pair<String, String>) msg.obj;
                    LazyHandler.this.notifyTopPackageChanged(p.first, p.second);
                    break;
                case AshManLZHandlerMessages.MSG_ONKEYEVENT:
                    LazyHandler.this.onKeyEvent((KeyEvent) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_MAYBEBACKLONGPRESSED:
                    LazyHandler.this.maybeBackLongPressed((String) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_MAYBEBACKPRESSED:
                    LazyHandler.this.maybeBackPressed((String) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_ONSTARTPROCESSLOCKED:
                    LazyHandler.this.onStartProcessLocked((ApplicationInfo) msg.obj);
                    break;
                case AshManLZHandlerMessages.MSG_ONREMOVEPROCESSLOCKED:
                    TypePack pack = (TypePack) msg.obj;
                    LazyHandler.this.onRemoveProcessLocked((ApplicationInfo) pack.getO1(),
                            pack.isBoolean1(),
                            pack.isBoolean2(),
                            pack.getS1());
                    break;
            }
        }
    }


    @Builder
    @Getter
    @ToString
    private static class StartProcessEvent {
        private int caller;
        private String hostType;
        private String hostName;
        private String packageName;
        private boolean allowed;
        private String why;
        private long when;
    }

    @Builder
    @Getter
    @ToString
    private static class ServiceEvent {
        private String pkg;
        private int callerUid;
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
        public static final CheckResult SERVICE_CHECK_DISABLED = new CheckResult(true, "SERVICE_CHECK_DISABLED", true);
        public static final CheckResult BOOT_CHECK_DISABLED = new CheckResult(true, "BOOT_CHECK_DISABLED", true);
        public static final CheckResult BROADCAST_CHECK_DISABLED = new CheckResult(true, "BROADCAST_CHECK_DISABLED", true);
        public static final CheckResult SYSTEM_NOT_READY = new CheckResult(true, "SYSTEM_NOT_READY", true);

        public static final CheckResult WHITE_LISTED = new CheckResult(true, "WHITE_LISTED", true);
        public static final CheckResult SYSTEM_APP = new CheckResult(true, "SYSTEM_APP", true);
        public static final CheckResult CALLED_BY_SYSTEM = new CheckResult(true, "CALLED_BY_SYSTEM", true);
        public static final CheckResult HAS_GCM_INTENT = new CheckResult(true, "HAS_GCM_INTENT", true);

        public static final CheckResult HOME_APP = new CheckResult(true, "HOME_APP", true);
        public static final CheckResult LAUNCHER_APP = new CheckResult(true, "LAUNCHER_APP", true);
        public static final CheckResult SMS_APP = new CheckResult(true, "SMS_APP", true);

        public static final CheckResult APP_RUNNING = new CheckResult(true, "APP_RUNNING", true);
        public static final CheckResult APP_RUNNING_TOP = new CheckResult(true, "APP_RUNNING_TOP", true);
        public static final CheckResult SAME_CALLER = new CheckResult(true, "SAME_CALLER", true);
        public static final CheckResult SAME_CALLER_CORE = new CheckResult(true, "SAME_CALLER_CORE", true);
        public static final CheckResult SAME_CALLER_RULE = new CheckResult(true, "SAME_CALLER_RULE", true);

        public static final CheckResult BAD_ARGS = new CheckResult(true, "BAD_ARGS", true);
        public static final CheckResult USER_ALLOWED = new CheckResult(true, "USER_ALLOWED", true);
        public static final CheckResult USER_DENIED = new CheckResult(false, "USER_DENIED", true);

        // Denied cases.
        public static final CheckResult DENIED_GENERAL = new CheckResult(false, "DENIED_GENERAL", true);
        public static final CheckResult DENIED_OP_DENIED = new CheckResult(false, "DENIED_OP_DENIED", true);
        public static final CheckResult DENIED_IFW = new CheckResult(false, "DENIED_IFW", true);
        public static final CheckResult JUST_BRING_DOWN = new CheckResult(false, "JUST_BRING_DOWN", true);
        public static final CheckResult DENIED_LAZY = new CheckResult(false, "DENIED_LAZY", true);
        public static final CheckResult ALLOWED_LAZY_KEEPED = new CheckResult(true, "ALLOWED_LAZY_KEEPED", true);
        public static final CheckResult DENIED_GREEN_APP = new CheckResult(false, "DENIED_GREEN_APP", true);
        public static final CheckResult DENIED_IN_RULE = new CheckResult(false, "DENIED_IN_RULE", true);
        public static final CheckResult ALLOWED_IN_RULE = new CheckResult(true, "ALLOWED_IN_RULE", true);
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
