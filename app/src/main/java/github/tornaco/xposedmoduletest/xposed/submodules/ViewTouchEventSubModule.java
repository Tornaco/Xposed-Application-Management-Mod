package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;
import android.view.View;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */
public class ViewTouchEventSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookOnTouchEvent();
    }

    private void hookOnTouchEvent() {
        XposedLog.verbose("hookOnTouchEvent...");
        try {
            Class clz = XposedHelpers.findClass("android.view.View", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "onTouchEvent", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param)
                                throws Throwable {
                            super.beforeHookedMethod(param);
                            View thisView = (View) param.thisObject;
                            Log.d(XposedLog.TAG, "onTouchEvent: " + thisView + ", id: " + thisView.getId());
                        }
                    });
            XposedLog.verbose("hookOnTouchEvent OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookOnTouchEvent:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
