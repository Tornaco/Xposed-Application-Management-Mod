package github.tornaco.xposedmoduletest.xposed.submodules.debug;

import android.content.Context;
import android.util.Log;

import com.android.internal.app.AlertController;
import com.android.internal.policy.PhoneWindow;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.submodules.AndroidSubModule;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class WindowSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookSetContentView();
        hookAlertController();
    }

    private void hookSetContentView() {
        XposedLog.verbose("PhoneWindow hookSetContentView...");
        try {
            Class clz = XposedHelpers.findClass("com.android.internal.policy.PhoneWindow", null);
            @SuppressWarnings("unchecked") Method setContentViewInt = clz.getDeclaredMethod("setContentView", int.class);
            XC_MethodHook.Unhook unhook = XposedBridge.hookMethod(setContentViewInt, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    super.afterHookedMethod(param);
                    Log.d(XposedLog.TAG, String.format("PhoneWindow setContentView: %s %s", param.thisObject, Arrays.toString(param.args)));
                    PhoneWindow phoneWindow = (PhoneWindow) param.thisObject;
                    Context context = phoneWindow.getContext();
                    int id = (int) param.args[0];
                    if (id != 0) {
                        String entry = context.getResources().getResourceEntryName(id);
                        String name = context.getResources().getResourceName(id);
                        String pkg = context.getResources().getResourcePackageName(id);
                        String type = context.getResources().getResourceTypeName(id);
                        Log.d(XposedLog.TAG, String.format("PhoneWindow, setContentView: %s-%s-%s-%s", pkg, type, entry, name));
                    }
                }
            });
            XposedLog.verbose("PhoneWindow hookSetContentView OK:" + unhook);
            setStatus(unhookToStatus(unhook));
        } catch (Exception e) {
            XposedLog.verbose("PhoneWindow Fail hookSetContentView:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookAlertController() {
        XposedLog.verbose("AlertController hookAlertController...");
        try {
            Class stackClass = XposedHelpers.findClass("com.android.internal.app.AlertController", null);

            Set unHooks = XposedBridge.hookAllConstructors(stackClass,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param)
                                throws Throwable {
                            super.afterHookedMethod(param);
                            AlertController ac = (AlertController) param.thisObject;
                            int mAlertDialogLayout = XposedHelpers.getIntField(ac, "mAlertDialogLayout");
                            int mSingleChoiceItemLayout = XposedHelpers.getIntField(ac, "mSingleChoiceItemLayout");
                            int mListLayout = XposedHelpers.getIntField(ac, "mListLayout");

                            Context context = (Context) XposedHelpers.getObjectField(ac, "mContext");

                            printId("mAlertDialogLayout", mAlertDialogLayout, context);
                            printId("mSingleChoiceItemLayout", mSingleChoiceItemLayout, context);
                            printId("mListLayout", mListLayout, context);
                        }
                    });
            XposedLog.verbose("AlertController hookAlertController OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("AlertController Fail hook hookAlertController" + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private static void printId(String idName, int id, Context context) {
        Log.d(XposedLog.TAG, String.format("AlertController printId: %s-%s", idName, id));
        if (id != 0) {
            String entry = context.getResources().getResourceEntryName(id);
            String name = context.getResources().getResourceName(id);
            String pkg = context.getResources().getResourcePackageName(id);
            String type = context.getResources().getResourceTypeName(id);
            Log.d(XposedLog.TAG, String.format("AlertController, print id for:%s ---- %s-%s-%s-%s", idName, pkg, type, entry, name));
        }
    }
}
