package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ToastSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookMakeToast();
        if (OSUtil.isOOrAbove()) {
            hookMakeToastOreoAddon();
        }
        hookShowToast(startupParam);
    }

    private void hookShowToast(IXposedHookZygoteInit.StartupParam startupParam) {
        XposedLog.verbose("hookShowToast...");
        try {
            Class clz = XposedHelpers.findClass("android.widget.Toast", null);
            Set<XC_MethodHook.Unhook> unHooks = XposedBridge.hookAllMethods(clz, "show",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);

                            if (BuildConfig.DEBUG) {
                                Log.d(XposedLog.TAG, "Toast show: " + AndroidAppHelper.currentPackageName());
                            }

                            String currentPackageName = AndroidAppHelper.currentPackageName();
                            if (currentPackageName == null) return;

                            String text = "";
                            try {
                                Toast toast = (Toast) param.thisObject;
                                View v = toast.getView();
                                TextView tv = v.findViewById(com.android.internal.R.id.message);
                                text = String.valueOf(tv.getText());
                            } catch (Throwable e) {
                                Log.e(XposedLog.TAG, "Fail retrieve text from toast: " + e);
                            }

                            boolean permControlEnabled = XAPMManager.get().isServiceAvailable() && XAPMManager.get().isPermissionControlEnabled();
                            if (permControlEnabled) {
                                int mode = XAPMManager.get().isServiceAvailable() ?
                                        XAPMManager.get().getPermissionControlBlockModeForPkg(
                                                XAppOpsManager.OP_TOAST_WINDOW,
                                                currentPackageName, true, new String[]{text})
                                        : XAppOpsManager.MODE_ALLOWED;

                                if (mode == XAppOpsManager.MODE_IGNORED) {
                                    Log.d(XposedLog.TAG, "Toast show denied");
                                    param.setResult(null);
                                }
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            handleMakeToastIcon(param);
                        }
                    });
            XposedLog.verbose("hookShowToast OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookShowToast:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookMakeToastOreoAddon() {
        XposedLog.verbose("hookMakeToastOreoAddon...");
        try {
            Class clz = XposedHelpers.findClass("android.widget.Toast", null);
            @SuppressWarnings("unchecked") Method m
                    = clz.getDeclaredMethod("makeText", Context.class, Looper.class, CharSequence.class, int.class);
            XC_MethodHook.Unhook unHooks = XposedBridge.hookMethod(m,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (isToastCallerEnabled()) {
                                XposedLog.verbose("makeTextOreoAddon: " + Arrays.toString(param.args));
                                String appLabel = String.valueOf(PkgUtil.loadNameByPkgName((Context) param.args[0],
                                        AndroidAppHelper.currentPackageName()));
                                String atAppLebal = "@" + appLabel;
                                if (!param.args[2].toString().contains(atAppLebal)) {
                                    String newText = atAppLebal + "\t" + param.args[2];
                                    param.args[2] = newText;
                                }
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            handleMakeToastIcon(param);
                        }
                    });
            XposedLog.verbose("hookMakeToastOreoAddon OK:" + unHooks);
            setStatus(unhookToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookMakeToastOreoAddon:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private boolean isToastCallerEnabled() {
        return XAPMManager.get().isServiceAvailable() && XAPMManager.get()
                .isOptFeatureEnabled(XAPMManager.OPT.TOAST.name());
    }

    private boolean isToastCallerIconEnabled() {
        return XAPMManager.get().isServiceAvailable() && XAPMManager.get()
                .isOptFeatureEnabled(XAPMManager.OPT.TOAST_ICON.name());
    }

    private void hookMakeToast() {
        XposedLog.verbose("hookMakeToast...");
        try {
            Class clz = XposedHelpers.findClass("android.widget.Toast", null);
            @SuppressWarnings("unchecked") Method m = clz.getDeclaredMethod("makeText",
                    Context.class, CharSequence.class, int.class);
            XC_MethodHook.Unhook unHooks = XposedBridge.hookMethod(m,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            if (isToastCallerEnabled()) {
                                XposedLog.verbose("makeText: " + Arrays.toString(param.args));
                                String appLabel = String.valueOf(PkgUtil.loadNameByPkgName((Context) param.args[0],
                                        AndroidAppHelper.currentPackageName()));
                                String newText = "@" + appLabel + "\t" + param.args[1];
                                param.args[1] = newText;
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            handleMakeToastIcon(param);
                        }
                    });
            XposedLog.verbose("hookMakeToast OK:" + unHooks);
            setStatus(unhookToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookMakeToast:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void handleMakeToastIcon(XC_MethodHook.MethodHookParam param) {
        if (isToastCallerIconEnabled()) {
            try {
                Toast t = (Toast) param.getResult();
                if (t == null) return;
                ViewGroup v = (ViewGroup) t.getView();
                if (v != null && !hasToastIconImageView(v)) {
                    ImageView iconView = new ImageView(v.getContext());
                    iconView.setTag(ICON_VIEW_TAG);
                    TextView tv = v.findViewById(com.android.internal.R.id.message);
                    Drawable d = ApkUtil.loadIconByPkgName(v.getContext(), AndroidAppHelper.currentPackageName());
                    iconView.setImageDrawable(d);
                    int textSize = (int) (tv.getTextSize() * 1.5);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(textSize, textSize);
                    v.addView(iconView, params);
                    XposedLog.verbose("handleMakeToastIcon add icon: " + Arrays.toString(param.args));
                }
            } catch (Throwable e) {
                XposedLog.wtf("Fail handleMakeToastIcon add icon: " + Log.getStackTraceString(e));
            }
        }
    }

    private static final String ICON_VIEW_TAG = "APM-TOAST-ICON";

    private static boolean hasToastIconImageView(ViewGroup viewGroup) {
        if (viewGroup != null) for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View c = viewGroup.getChildAt(i);
            if (c instanceof ImageView && ICON_VIEW_TAG.equals(c.getTag())) {
                return true;
            }
        }
        return false;
    }
}
