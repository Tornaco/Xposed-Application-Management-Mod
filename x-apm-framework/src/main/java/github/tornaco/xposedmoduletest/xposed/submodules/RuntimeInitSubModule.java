package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.os.Binder;
import android.util.Log;

import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.util.DateUtils;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookUnCaughtErr settings.
class RuntimeInitSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        if (!OSUtil.isMIUI()) {
            hookUnCaughtErr();
        } else {
            XposedLog.boot("Skip hookUnCaughtErr for MIUI ");
        }
    }

    private void hookUnCaughtErr() {
        XposedLog.verbose("hookUnCaughtErr...");
        try {
            final Class c = XposedHelpers.findClass(
                    OSUtil.isOOrAbove() ?
                            "com.android.internal.os.RuntimeInit$KillApplicationHandler"
                            : "com.android.internal.os.RuntimeInit$UncaughtHandler",
                    null);
            Set unHooks = XposedBridge.hookAllMethods(c, "uncaughtException",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            // Check if it is an System process dying.
                            boolean isAndroidDying = "android".equals(AndroidAppHelper
                                    .currentPackageName());
                            if (isAndroidDying) {
                                XposedLog.wtf("==================FATAL================");
                                XposedLog.wtf("Android is dying, something serious bad going");
                                // Save the trace to storage.
                                Thread t = (Thread) param.args[0];
                                XposedLog.wtf("Android is dying on thread: " + t);
                                Throwable e = (Throwable) param.args[1];
                                String trace = t + "\n" + Log.getStackTraceString(e);
                                XposedLog.wtf(trace);
                                File traceDir = RepoProxy.getSystemErrorTraceDirByVersion();
                                String fileName = "SYSTEM_ERROR_TRACE-" + DateUtils.formatForFileName(System.currentTimeMillis());
                                File traceFile = new File(traceDir, fileName);
                                XposedLog.wtf("Writing error trace to: " + traceFile);
                                try {
                                    Files.createParentDirs(traceFile);
                                    Files.asByteSink(traceFile).asCharSink(Charset.defaultCharset())
                                            .write(trace);
                                    XposedLog.wtf("System error trace has been write to: " + traceFile);
                                } catch (Throwable e2) {
                                    XposedLog.wtf("Fail write system err trace: " + Log.getStackTraceString(e2));
                                }

                                // Check if it we cause this err.
                                boolean maybeUs = trace.contains(BuildConfig.APPLICATION_ID);
                                boolean isRedemptionModeEnabled = XAPMManager.get().isServiceAvailable()
                                        && XAPMManager.get().isRedemptionModeEnabled();
                                if (maybeUs && isRedemptionModeEnabled) {
                                    XposedLog.wtf("Maybe our APM module cause this err, disable our module anyway.");
                                    // Fake disable by create a file indicator.
                                    RepoProxy.createFileIndicator(SubModuleManager.REDEMPTION);

                                    // Disable debug mode, because I wonder it is debug mode
                                    // who made this err.
                                    if (XAppLockManager.get().isServiceAvailable()) {
                                        XposedLog.wtf("Disable debug mode first:(");
                                        XAppLockManager.get().setDebug(false);
                                    }
                                }
                                XposedLog.wtf("==================FATAL HANDLE END================");
                            }

                            if (PkgUtil.isSystemOrPhoneOrShell(Binder.getCallingUid())) {
                                return;
                            }

                            Log.d(XposedLog.TAG, "uncaughtException, reporting to X-APM-S: " + Binder.getCallingUid());
                            // Now report to ash man.
                            XAPMManager xAshmanManager = XAPMManager.get();
                            if (xAshmanManager.isServiceAvailable()) {
                                // Wrap err log to xp log.
                                Thread t = (Thread) param.args[0];
                                Throwable e = (Throwable) param.args[1];
                                String currentPackage = AndroidAppHelper.currentPackageName();
                                String trace = Log.getStackTraceString(e);

                                boolean shouldInterruptCrash =
                                        xAshmanManager.onApplicationUncaughtException(currentPackage,
                                                t.getName(), e.getClass().getName(), trace);
                                if (shouldInterruptCrash) {
                                    Log.d(XposedLog.TAG, "uncaughtException, result interrupted!!!");
                                    param.setResult(null);
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookUnCaughtErr OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookUnCaughtErr: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
