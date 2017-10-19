package github.tornaco.xposedmoduletest.x;

import android.app.ActivityManager;
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
import android.util.Log;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.IAppService;
import github.tornaco.xposedmoduletest.ICallback;

/**
 * Created by guohao4 on 2017/10/13.
 * Email: Tornaco@163.com
 */

class XModuleImpl24 extends XModule {

    private static final String TAG = "XAppGuard-";

    private static final String SELF_PKG = "github.tornaco.xposedmoduletest";
    public static final String SELF_PREF_NAME = "github_tornaco_xposedmoduletest_pref";

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
        String packageName = lpparam.packageName;

        if (packageName.equals("android")) {
            hookActivityStarter(lpparam);
            hookTaskMover(lpparam);
            hookAppOps(lpparam);
        }

        if (SELF_PKG.equals(packageName)) {
            handlerLoadSelf(lpparam);
        }
    }

    private void hookTaskMover(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Method moveToFront = Class.forName("com.android.server.am.ActivityManagerService",
                    false, lpparam.classLoader)
                    .getDeclaredMethod("moveTaskToFrontLocked",
                            int.class, int.class, Bundle.class);
            XposedBridge.hookMethod(moveToFront, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    XposedBridge.log(TAG + "moveTaskToFrontLocked:" + param.args[0]);

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
                                if (!waitForAppService()) return;

                                Intent intent = (Intent) param.args[1];
                                if (intent == null) return;

                                ComponentName componentName = intent.getComponent();
                                if (componentName == null) return;
                                final String pkgName = componentName.getPackageName();

                                XposedBridge.log(TAG + "HOOKING startActivityLocked:" + pkgName);

                                // Package has been passed.
                                if (pkgPassed(pkgName)) {
                                    XposedBridge.log(TAG + "PASSED PKG");
                                    return;
                                }

                                int callingUID = Binder.getCallingUid();
                                int callingPID = Binder.getCallingPid();

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

    private void handlerLoadSelf(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log(TAG + "Loading self...");
        XposedBridge.log(TAG + mAppOpsContext);
        XposedBridge.log(TAG + mAppOpsHandler);
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
        return PASSED_PACKAGES.contains(pkg) || pkg.equals(SELF_PKG);
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
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log(TAG + "initZygote...");
    }

    private class AppServiceClient implements IBinder.DeathRecipient {
        boolean ok;
        IAppService service;

        AppServiceClient(IAppService service) {
            ok = service != null;
            if (!ok) return;
            this.service = service;
            try {
                this.service.asBinder().linkToDeath(this, 0);
            } catch (RemoteException ignored) {

            }
        }

        void unLinkToDeath() {
            if (ok && service != null) {
                service.asBinder().unlinkToDeath(this, 0);
            }
        }

        @Override
        public void binderDied() {
            XposedBridge.log(TAG + "AppServiceClient binder died!!!");
            ok = false;
            unLinkToDeath();
        }
    }
}
