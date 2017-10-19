package github.tornaco.xposedmoduletest.x;

import android.app.ActivityManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

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
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

class XModuleImpl23 extends XModule {
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

        xStatus = XStatus.RUNNING;

        String packageName = lpparam.packageName;

        if (packageName.equals("android")) {
            hookActivityManagerService(lpparam);
            hookTaskMover(lpparam);
            hookAppOps(lpparam);
            initDefaultXPreference();
        }
    }

    private void hookTaskMover(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class taskRecordClass = Class.forName("com.android.server.am.TaskRecord", false, lpparam.classLoader);
            final Method moveToFront = Class.forName("com.android.server.am.ActivityStackSupervisor",
                    false, lpparam.classLoader)
                    .getDeclaredMethod("findTaskToMoveToFrontLocked",
                            taskRecordClass, int.class, Bundle.class, String.class);
            XposedBridge.hookMethod(moveToFront, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    if (earlyPassed()) return;

                    XposedBridge.log(TAG + "findTaskToMoveToFrontLocked:" + param.args[0]);
                    // FIXME Using aff instead of PKG.
                    try {
                        final String affinity = (String) XposedHelpers.getObjectField(param.args[0], "affinity");

                        // Package has been passed.
                        if (pkgPassed(affinity)) {
                            XposedBridge.log(TAG + "PASSED PKG");
                            return;
                        }

                        int callingUID = Binder.getCallingUid();
                        int callingPID = Binder.getCallingPid();

                        if (!waitForAppService()) {
                            mSeriousErrorOccurredTimes.incrementAndGet();
                            return;
                        }

                        mAppService.service.noteAppStart(new ICallback.Stub() {
                            @Override
                            public void onRes(int res) throws RemoteException {

                                XposedBridge.log(TAG + "noteAppStart, onRes:" + res);

                                if (res != XMode.MODE_DENIED) try {
                                    if (res == XMode.MODE_ALLOWED) addPackagePass(affinity);
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

    private void hookActivityManagerService(XC_LoadPackage.LoadPackageParam lpparam) {

        try {

            final Method startActivity =
                    Class.forName("com.android.server.am.ActivityManagerService", false, lpparam.classLoader)
                            .getDeclaredMethod("startActivity", IApplicationThread.class, String.class,
                                    Intent.class, String.class, IBinder.class, String.class,
                                    int.class, int.class, ProfilerInfo.class, Bundle.class);

            final Method startActivityAsUser =
                    Class.forName("com.android.server.am.ActivityManagerService", false, lpparam.classLoader)
                            .getDeclaredMethod("startActivityAsUser", IApplicationThread.class, String.class,
                                    Intent.class, String.class, IBinder.class, String.class,
                                    int.class, int.class, ProfilerInfo.class, Bundle.class,
                                    int.class);

            XposedBridge.hookMethod(startActivity,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            if (earlyPassed()) return;

                            try {
                                Intent intent = (Intent) param.args[2];
                                if (intent == null) return;

                                ComponentName componentName = intent.getComponent();
                                if (componentName == null) return;
                                final String pkgName = componentName.getPackageName();

                                XposedBridge.log(TAG + "HOOKING startActivity:" + pkgName);

                                // Package has been passed.
                                if (pkgPassed(pkgName)) {
                                    XposedBridge.log(TAG + "PASSED PKG");
                                    return;
                                }

                                if (!waitForAppService()) return;

                                int callingUID = Binder.getCallingUid();
                                int callingPID = Binder.getCallingPid();

                                int callingUserId = UserHandle.getCallingUserId();
                                final Object[] args = new Object[param.args.length + 1];
                                System.arraycopy(param.args, 0, args, 0, param.args.length);
                                args[args.length - 1] = callingUserId;

                                mAppService.service.noteAppStart(new ICallback.Stub() {
                                    @Override
                                    public void onRes(int res) throws RemoteException {

                                        XposedBridge.log(TAG + "noteAppStart, onRes:" + res);

                                        if (res != XMode.MODE_DENIED) try {
                                            if (res == XMode.MODE_ALLOWED) addPackagePass(pkgName);
                                            mAppOpsHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        XposedBridge.invokeOriginalMethod(startActivityAsUser, param.thisObject, args);
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
            XposedBridge.log(TAG + "hookActivityManagerService" + Log.getStackTraceString(e));
        }
    }

    private boolean waitForAppService() {
        int MAX_RETRY = 50;
        int times = 0;
        if (mAppService == null || !mAppService.ok) {
            startAppService();
            while (mAppService == null || !mAppService.ok) {
                try {
                    Thread.sleep(50);
                    times++;
                } catch (InterruptedException ignored) {

                }
                if (times > MAX_RETRY) return false;
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
                                XposedBridge.log(TAG + "app service:" + appService);
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
}
