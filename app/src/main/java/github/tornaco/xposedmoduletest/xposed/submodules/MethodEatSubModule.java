package github.tornaco.xposedmoduletest.xposed.submodules;

import android.util.Log;

import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.bean.MethodFood;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */
public class MethodEatSubModule extends AndroidSubModule {

    private static final Map<String, MethodFood> sFoods = new HashMap<>();

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        super.handleLoadingPackage(pkg, lpparam);
        if (sFoods.containsKey(lpparam.packageName)) {
            eatMethod(sFoods.get(lpparam.packageName), lpparam);
        }
    }

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("*");
    }

    @SuppressWarnings("unchecked")
    private void eatMethod(MethodFood food, XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("eatMethod: " + food);
        try {
            Class clz = XposedHelpers.findClass(food.getClassName(), lpparam.classLoader);
            String[] paramClasses = food.getParamClasses();
            Class[] paramClz = paramClasses == null ? null : new Class[paramClasses.length];
            boolean paramSuccess = true;
            if (paramClz != null) {
                for (int i = 0; i < paramClasses.length; i++) {
                    paramClz[i] = XposedHelpers.findClass(paramClasses[i], lpparam.classLoader);
                    if (paramClz[i] == null) {
                        paramSuccess = false;
                        break;
                    }
                }
            }
            if (!paramSuccess) {
                Log.d(XposedLog.TAG_ME, "Param fail");
                return;
            }
            Method m;
            if (paramClz == null || paramClz.length == 0) {
                m = clz.getDeclaredMethod(food.getMethodName());
            } else {
                m = clz.getDeclaredMethod(food.getMethodName(), paramClz);
            }
            if (m == null) {
                Log.d(XposedLog.TAG_ME, "Method get fail");
                return;
            }
            final Method finalM = m;
            XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(m, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Log.d(XposedLog.TAG_ME, "beforeHookedMethod: " + finalM.getName());
                }
            });
            XposedLog.verbose("eatMethod OK:" + unhook);
            setStatus(unhookToStatus(unhook));
        } catch (Exception e) {
            XposedLog.verbose("Fail eatMethod:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
