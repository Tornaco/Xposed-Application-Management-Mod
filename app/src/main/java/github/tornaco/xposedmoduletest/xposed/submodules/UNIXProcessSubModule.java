package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class UNIXProcessSubModule extends AndroidSubModule {

    private static final String TAG = "UNIXProcess-";

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("*");
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookWrite();
    }

    private void hookWrite() {
        XposedLog.verbose(TAG + "hookWrite...");
        try {
            Class clz = XposedHelpers.findClass("java.lang.UNIXProcess$ProcessPipeOutputStream", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "write", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Object in = param.args[0];
                            if (in instanceof byte[]) {
                                byte[] data = (byte[]) in;
                                String cmd = new String(data);
                                String caller = AndroidAppHelper.currentPackageName();
                                Log.d(XposedLog.TAG, TAG + "write: " + cmd + ", caller: " + caller);
                            }
                        }
                    });
            XposedLog.verbose(TAG + "hookWrite OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose(TAG + "Fail hookWrite:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
