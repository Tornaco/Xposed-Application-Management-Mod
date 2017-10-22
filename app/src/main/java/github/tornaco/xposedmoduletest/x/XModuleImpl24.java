package github.tornaco.xposedmoduletest.x;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.IAppService;
import github.tornaco.xposedmoduletest.ICallback;

/**
 * Created by guohao4 on 2017/10/13.
 * Email: Tornaco@163.com
 */

class XModuleImpl24 extends XModule {

    private static final String SELF_PKG = BuildConfig.APPLICATION_ID;

    private final Set<String> PASSED_PACKAGES = new HashSet<>();

    private AppServiceClient mAppService;
    private Context mAppOpsContext;
    private Handler mAppOpsHandler;

    private AtomicInteger mSeriousErrorOccurredTimes = new AtomicInteger(0);

    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PASSED_PACKAGES.clear();
        }
    };

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        super.handleLoadPackage(lpparam);

        xStatus = XStatus.RUNNING;

        String packageName = lpparam.packageName;

        if (packageName.equals("android")) {
            hookActivityStarter(lpparam);
            hookTaskMover(lpparam);
            hookAppOps(lpparam);
        }
    }

    private void hookTaskMover(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class taskRecordClass = Class.forName("com.android.server.am.TaskRecord", false, lpparam.classLoader);
            final Method moveToFront = Class.forName("com.android.server.am.ActivityStackSupervisor",
                    false, lpparam.classLoader)
                    .getDeclaredMethod("findTaskToMoveToFrontLocked",
                            taskRecordClass, int.class, ActivityOptions.class,
                            String.class, boolean.class);
            XposedBridge.hookMethod(moveToFront, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param)
                        throws Throwable {
                    super.beforeHookedMethod(param);

                    if (earlyPassed()) return;

                    if (DEBUG_V)
                        XposedBridge.log(TAG + "findTaskToMoveToFrontLocked:" + param.args[0]);
                    // FIXME Using aff instead of PKG.
                    try {
                        final String affinity = (String) XposedHelpers.getObjectField(param.args[0], "affinity");

                        // Package has been passed.
                        if (pkgPassed(affinity)) {
                            if (DEBUG_V)
                                XposedBridge.log(TAG + "PASSED PKG");
                            return;
                        }

                        int callingUID = Binder.getCallingUid();
                        int callingPID = Binder.getCallingPid();

                        if (!ensureAppService(true)) {
                            mSeriousErrorOccurredTimes.incrementAndGet();
                            toast("连接应用超时，请检查AppGuard是否被限制");
                            return;
                        }

                        mAppService.service.noteAppStart(new ICallback.Stub() {
                            @Override
                            public void onRes(int res, int flags) throws RemoteException {

                                if (DEBUG_V)
                                    XposedBridge.log(TAG + "noteAppStart, onRes:" + res
                                            + ", flags:" + flags);

                                if (res != XMode.MODE_DENIED) try {
                                    if (flags != XFlags.FLAG_ALWAYS_VERIFY && res == XMode.MODE_ALLOWED) {
                                        addPackagePass(affinity);
                                    }
                                    mAppOpsHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                XposedBridge.invokeOriginalMethod(moveToFront,
                                                        param.thisObject, param.args);
                                            } catch (Exception e) {
                                                XposedBridge.log(TAG
                                                        + "Error@" + mSeriousErrorOccurredTimes.incrementAndGet()
                                                        + Log.getStackTraceString(e));
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    // replacing did not work.. but no reason to crash the VM! Log the error and go on.
                                    XposedBridge.log(TAG
                                            + "Error@" + mSeriousErrorOccurredTimes.incrementAndGet()
                                            + Log.getStackTraceString(e));
                                }
                            }
                        }, affinity, callingUID, callingPID);

                        param.setResult(null);

                    } catch (Exception e) {
                        XposedBridge.log(TAG + "findTaskToMoveToFrontLocked" + Log.getStackTraceString(e));
                    }
                }
            });
        } catch (Exception e) {
            XposedBridge.log(TAG + "hookTaskMover" + Log.getStackTraceString(e));
        }
    }

    private void hookActivityStarter(XC_LoadPackage.LoadPackageParam lpparam) {

        try {

            Method startActivityLockedExact = null;
            int matchCount = 0;
            for (Method method : Class.forName("com.android.server.am.ActivityStarter",
                    false, lpparam.classLoader).getDeclaredMethods()) {
                if (method.getName().equals("startActivityLocked")) {
                    startActivityLockedExact = method;
                    startActivityLockedExact.setAccessible(true);
                    matchCount++;
                }
            }

            if (startActivityLockedExact == null) {
                XposedBridge.log(TAG + "*** FATAL can not find starter method ***");
                return;
            }

            if (matchCount > 1) {
                XposedBridge.log(TAG + "*** FATAL more than 1 starter method ***");
                return;
            }

            XposedBridge.log(TAG + "startActivityLocked method:" + startActivityLockedExact);
            final Method finalStartActivityLockedExact = startActivityLockedExact;
            XposedBridge.hookMethod(startActivityLockedExact,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            if (earlyPassed()) return;

                            try {
                                Intent intent = (Intent) param.args[1];
                                if (intent == null) return;

                                if (DEBUG_V) {
                                    XposedBridge.log(TAG + "HOOKING intent: " + intent);
                                    XposedBridge.log(TAG + "HOOKING is home: " + isLauncherIntent(intent));
                                }

                                ComponentName componentName = intent.getComponent();
                                if (componentName == null) return;
                                final String pkgName = componentName.getPackageName();

                                if (DEBUG_V)
                                    XposedBridge.log(TAG + "HOOKING startActivityLocked:" + pkgName);

                                // Package has been passed.
                                if (pkgPassed(pkgName)) {
                                    if (DEBUG_V)
                                        XposedBridge.log(TAG + "PASSED PKG");
                                    return;
                                }

                                boolean isHomeIntent = isLauncherIntent(intent);
                                if (isHomeIntent) {
                                    PREBUILT_WHITE_LIST.add(pkgName);
                                    onLauncherPackageLaunch();
                                    return;
                                }

                                if (!ensureAppService(true)) {
                                    toast("连接应用超时，请检查AppGuard是否被限制");
                                    return;
                                }

                                int callingUID = Binder.getCallingUid();
                                int callingPID = Binder.getCallingPid();

                                mAppService.service.noteAppStart(new ICallback.Stub() {
                                    @Override
                                    public void onRes(int res, int flags) throws RemoteException {

                                        if (DEBUG_V)
                                            XposedBridge.log(TAG + "noteAppStart, onRes:" + res
                                                    + ", flags:" + flags);

                                        if (res != XMode.MODE_DENIED) try {
                                            if (flags != XFlags.FLAG_ALWAYS_VERIFY && res == XMode.MODE_ALLOWED) {
                                                addPackagePass(pkgName);
                                            }
                                            mAppOpsHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        XposedBridge.invokeOriginalMethod(finalStartActivityLockedExact,
                                                                param.thisObject, param.args);
                                                    } catch (Exception e) {
                                                        XposedBridge.log(TAG
                                                                + "Error@" + mSeriousErrorOccurredTimes.incrementAndGet()
                                                                + Log.getStackTraceString(e));
                                                    }
                                                }
                                            });
                                        } catch (Exception e) {
                                            // replacing did not work.. but no reason to crash the VM! Log the error and go on.
                                            XposedBridge.log(TAG
                                                    + "Error@" + mSeriousErrorOccurredTimes.incrementAndGet()
                                                    + Log.getStackTraceString(e));
                                        }
                                    }
                                }, pkgName, callingUID, callingPID);

                                if (!pkgName.contains("launcher")) {
                                    param.setResult(ActivityManager.START_SUCCESS);
                                }
                            } catch (Exception e) {
                                // replacing did not work.. but no reason to crash the VM! Log the error and go on.
                                XposedBridge.log(TAG + Log.getStackTraceString(e));
                            }
                        }
                    });
        } catch (Exception e) {
            XposedBridge.log(TAG + "hookActivityStarter" + Log.getStackTraceString(e));
        }
    }

    private void onLauncherPackageLaunch() {
        if (DEBUG_V) XposedBridge.log(TAG + "Launcher ready");
    }

    private boolean ensureAppService(boolean block) {
        int MAX_RETRY = 20;
        int times = 0;
        if (mAppService == null || !mAppService.ok) {
            startAppService();
            if (block) {
                toast("正在连接App Guard客户端...");
                while (mAppService == null || !mAppService.ok) {
                    try {
                        Thread.sleep(50);
                        times++;
                    } catch (InterruptedException ignored) {

                    }
                    if (times > MAX_RETRY) return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private void startAppService() {
        final Context context = mAppOpsContext;
        try {
            if (context == null) {
                XposedBridge.log(TAG + "Bad context");
                return;
            }
            if (mAppOpsHandler == null) {
                XposedBridge.log(TAG + "Bad handler");
                return;
            }

            Runnable serviceRunnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent intent = new Intent("tornaco.action.app.service");
                        intent.setPackage(SELF_PKG);
                        context.startService(intent);

                        // Bind App service.
                        context.bindService(intent, new ServiceConnection() {
                            @Override
                            public void onServiceConnected(ComponentName name, IBinder service) {
                                IAppService appService = IAppService.Stub.asInterface(service);
                                XposedBridge.log(TAG + "*** Started app service:" + appService + " ***");
                                mAppService = new AppServiceClient(appService);
                            }

                            @Override
                            public void onServiceDisconnected(ComponentName name) {

                            }
                        }, Context.BIND_AUTO_CREATE);
                    } catch (Throwable e) {
                        XposedBridge.log(TAG + Log.getStackTraceString(e));
                    }
                }
            };
            mAppOpsHandler.post(serviceRunnable);
        } catch (Exception e) {
            XposedBridge.log(TAG + Log.getStackTraceString(e));
        }
    }

    // Setup a flag to disable module when error occurred too many times.
    private boolean earlyPassed() {
        return mAppOpsHandler == null
                || mSeriousErrorOccurredTimes.get() > 10;
    }

    private boolean pkgPassed(String pkg) {
        return PREBUILT_WHITE_LIST.contains(pkg) || PASSED_PACKAGES.contains(pkg);
    }

    private void addPackagePass(String pkg) {
        PASSED_PACKAGES.add(pkg);
    }

    private void hookAppOps(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Method systemReady =
                    Class.forName("com.android.server.AppOpsService", false, lpparam.classLoader)
                            .getDeclaredMethod("systemReady");
            XposedBridge.hookMethod(systemReady, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    // Retrieve System context.
                    mAppOpsContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                    mAppOpsHandler = (Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler");
                    XposedBridge.log(TAG + mAppOpsContext);
                    XposedBridge.log(TAG + mAppOpsHandler);

                    // Register screen receiver now.
                    mAppOpsContext.registerReceiver(mScreenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
                }
            });
        } catch (Exception e) {
            XposedBridge.log(TAG + Log.getStackTraceString(e));
        }
    }

    @Override
    void onBootComplete() {
        super.onBootComplete();
        ensureAppService(true);
    }

    @Override
    public String codename() throws RemoteException {
        return "XModule-v24-AOSP-" + XModuleGithubCommitSha.LATEST_SHA;
    }

    private void toast(String what) {
        Toast.makeText(mAppOpsContext, what, Toast.LENGTH_SHORT).show();
    }
}
