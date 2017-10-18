package github.tornaco.xposedmoduletest.x;

import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
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

public class XAIOModule implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = "XAppGuard-";

    private static final String SELF_PKG = "github.tornaco.xposedmoduletest";
    public static final String SELF_PREF_NAME = "github_tornaco_xposedmoduletest_pref";

    private final Set<String> PASSED_PACKAGES = new HashSet<>();

    private AppServiceClient mAppService;
    private Context mAppOpsContext;
    private Handler mAppOpsHandler;

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;

        if (packageName.equals("android")) {
            handleLoadAndroid(lpparam);
            hookAppOps(lpparam);
        }

        if (SELF_PKG.equals(packageName)) {
            handlerLoadSelf(lpparam);
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

    private void handleLoadAndroid(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log(TAG + "Android loading...");

        try {
            Method startActivity =
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


            XposedBridge.hookMethod(startActivity, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(final MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

                    try {
                        if (!waitForAppService()) return;

                        Intent intent = (Intent) param.args[2];
                        if (intent == null) return;

                        ComponentName componentName = intent.getComponent();
                        if (componentName == null) return;
                        final String pkgName = componentName.getPackageName();

                        XposedBridge.log(TAG + "HOOKING startActivity:" + pkgName);

                        // Bypass.
                        if (bypass(pkgName)) {
                            XposedBridge.log(TAG + "BYPASS PKG");
                            return;
                        }

                        int callingUserId = UserHandle.getCallingUserId();
                        final Object[] args = new Object[param.args.length + 1];
                        System.arraycopy(param.args, 0, args, 0, param.args.length);
                        args[args.length - 1] = callingUserId;

                        int callingUID = Binder.getCallingUid();
                        int callingPID = Binder.getCallingPid();

                        mAppService.service.noteAppStart(new ICallback.Stub() {
                            @Override
                            public void onRes(boolean res) throws RemoteException {
                                if (res) try {
                                    onPackagePass(pkgName);
                                    XposedBridge.invokeOriginalMethod(startActivityAsUser, param.thisObject, args);
                                } catch (Exception e) {
                                    // replacing did not work.. but no reason to crash the VM! Log the error and go on.
                                    XposedBridge.log(TAG + Log.getStackTraceString(e));
                                }
                            }
                        }, pkgName, callingUID, callingPID);

                        if (!pkgName.contains("launcher")) {
                            param.setResult(0);
                        }
                    } catch (Exception e) {
                        // replacing did not work.. but no reason to crash the VM! Log the error and go on.
                        XposedBridge.log(TAG + Log.getStackTraceString(e));
                    }
                }
            });

        } catch (Exception e) {
            XposedBridge.log(TAG + Log.getStackTraceString(e));
        }

        final Method startActivityFromRecents;
        try {
            startActivityFromRecents = Class.forName("com.android.server.am.ActivityManagerService", false, lpparam.classLoader)
                    .getDeclaredMethod("startActivityFromRecents", int.class, Bundle.class);

            XposedBridge.hookMethod(startActivityFromRecents, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    param.setThrowable(new SecurityException("DUMMY"));
                    final long origID = Binder.clearCallingIdentity();
                    XposedBridge.log(TAG + "startActivityFromRecents:" + param.args[0]);
                    XposedBridge.log(TAG + "startActivityFromRecents:" + origID);
                    Binder.restoreCallingIdentity(origID);
                }
            });
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            XposedBridge.log(TAG + Log.getStackTraceString(e));
        }
    }

    private boolean bypass(String pkg) {
        return PASSED_PACKAGES.contains(pkg) || pkg.equals(SELF_PKG);
    }

    private void onPackagePass(String pkg) {
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

        public AppServiceClient(IAppService service) {
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
