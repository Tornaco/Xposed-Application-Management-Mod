package github.tornaco.xposedmoduletest;

import android.app.AndroidAppHelper;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by guohao4 on 2017/10/13.
 * Email: Tornaco@163.com
 */

public class AMSModule implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String TAG = "AMSModule-";

    public static final String SELF_PKG = "github.tornaco.xposedmoduletest";
    public static final String SELF_PREF_NAME = "github_tornaco_xposedmoduletest_pref";

    private IAppService mAppService;

    private XSharedPreferences xSharedPreferences;


    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;

        if (packageName.equals("android")) {
            handleLoadAndroid(lpparam);
        }

        if (SELF_PKG.equals(packageName)) {
            handlerLoadSelf(lpparam);
        }
    }

    private void handlerLoadSelf(final XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log(TAG + "Loading self...");
        xSharedPreferences = new XSharedPreferences(SELF_PKG, SELF_PREF_NAME);
        xSharedPreferences.makeWorldReadable();
        startAppService();
    }

    private void startAppService() {
        try {
            // Start App service.
            final Context context = AndroidAppHelper.currentApplication();
            if (context == null) {
                XposedBridge.log(TAG + "Bad context");
                return;
            }
            XposedBridge.log(TAG + "current app:" + context.getPackageName());
            Intent intent = new Intent("tornaco.action.app.service");
            intent.setPackage(SELF_PKG);
            context.startService(intent);

            // Bind App service.
            context.bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IAppService appService = IAppService.Stub.asInterface(service);
                    XposedBridge.log(TAG + "app service:" + appService);
                    mAppService = appService;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            }, Context.BIND_AUTO_CREATE);
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

                    // Check if hook enabled.
                    try {
                        boolean enabled = xSharedPreferences.getBoolean(XKey.ENABLED, false);
                        XposedBridge.log(TAG + "xSharedPreferences, enabled:" + enabled);
                    } catch (Exception e) {
                        XposedBridge.log(TAG + Log.getStackTraceString(e));
                    }

                    try {
                        if (mAppService == null)
                            startAppService();
                        while (mAppService == null) {
                            Thread.sleep(1000);
                        }

                        String intentStr = param.args[2].toString();
                        XposedBridge.log(TAG + "beforeHookedMethod:" + intentStr);

                        // Bypass.
                        if (intentStr.contains(SELF_PKG)) {
                            return;
                        }

                        int callingUserId = UserHandle.getCallingUserId();
                        final Object[] args = new Object[param.args.length + 1];
                        System.arraycopy(param.args, 0, args, 0, param.args.length);
                        args[args.length - 1] = callingUserId;

                        mAppService.noteAppStart(new ICallback.Stub() {
                            @Override
                            public void onRes(boolean res) throws RemoteException {
                                if (res) try {
                                    XposedBridge.invokeOriginalMethod(startActivityAsUser, param.thisObject, args);
                                } catch (Exception e) {
                                    // replacing did not work.. but no reason to crash the VM! Log the error and go on.
                                    XposedBridge.log(TAG + Log.getStackTraceString(e));
                                }
                            }
                        }, intentStr);

                        if (!intentStr.contains("launcher")) {
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

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        XposedBridge.log(TAG + "initZygote...");
    }
}
