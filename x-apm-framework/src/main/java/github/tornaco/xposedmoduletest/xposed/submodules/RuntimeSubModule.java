package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class RuntimeSubModule extends AndroidSubModule {

    private static final String TAG = "Runtime-";

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("*");
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookExec();
    }

    private void hookExec() {
        XposedLog.verbose(TAG + "hookExec...");
        try {
            Class clz = XposedHelpers.findClass("java.lang.Runtime", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "exec", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);

                            boolean permControlEnabled = XAPMManager.get().isServiceAvailable() && XAPMManager.get().isPermissionControlEnabled();
                            if (!permControlEnabled) {
                                return;
                            }

                            Object in = param.args[0];
                            if (in instanceof String) {
                                if (XAPMManager.get().isServiceAvailable()) {
                                    String command = (String) in;
                                    String caller = AndroidAppHelper.currentPackageName();
                                    // Log.d(XposedLog.TAG, String.format("Runtime exec: %s %s", command, caller));
                                    int mode = XAPMManager.get().getPermissionControlBlockModeForPkg(XAppOpsManager
                                            .OP_EXECUTE_SHELL_COMMAND, caller, true, new String[]{command});
                                    if (mode == XAppOpsManager.MODE_IGNORED) {
                                        param.setResult(null);
                                        Log.d(XposedLog.TAG, "COMMAND BLOCKED");
                                    }
                                }
                            } else if (in instanceof String[]) {
                                String[] cmdArr = (String[]) in;
                                String caller = AndroidAppHelper.currentPackageName();
                                // Log.d(XposedLog.TAG, String.format("Runtime exec arr: %s %s", Arrays.toString(cmdArr), caller));
                                if (XAPMManager.get().isServiceAvailable()) {
                                    int mode = XAPMManager.get().getPermissionControlBlockModeForPkg(XAppOpsManager
                                            .OP_EXECUTE_SHELL_COMMAND, caller, true, cmdArr);
                                    if (mode == XAppOpsManager.MODE_IGNORED) {
                                        param.setResult(null);
                                        Log.d(XposedLog.TAG, "COMMAND BLOCKED");
                                    }
                                }
                            }

                        }
                    });
            XposedLog.verbose(TAG + "hookExec OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose(TAG + "Fail hookExec:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
