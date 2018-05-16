package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.List;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// Hook hookStartShortcut settings.
class LauncherAppSubModule extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_LOCK;
    }

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.N;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookGetShortcuts();
    }

    private void hookGetShortcuts() {
        XposedLog.verbose("LauncherAppService hookGetShortcuts...");
        try {
            Class clz = XposedHelpers.findClass("android.content.pm.LauncherApps", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "getShortcuts", new XC_MethodHook() {
                        @SuppressWarnings("unchecked")
                        @RequiresApi(api = Build.VERSION_CODES.N_MR1)
                        @Override
                        protected void afterHookedMethod(final MethodHookParam param)
                                throws Throwable {
                            super.afterHookedMethod(param);

                            Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                            if (context != null && !param.hasThrowable()) {
                                String packageName = context.getPackageName();
                                Log.d(XposedLog.TAG, "LauncherAppService getShortcuts: " + packageName);

                                ShortcutInfo.Builder sb = new ShortcutInfo.Builder(context, "x-apm-per-app-settings");
                                Intent intent = new Intent();
                                ComponentName componentName = new ComponentName("github.tornaco.xposedmoduletest",
                                        "github.tornaco.xposedmoduletest.ui.activity.app.PerAppSettingsDashboardActivity");
                                intent.setComponent(componentName);
                                intent.putExtra("pkg_name", packageName);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.setAction("github.tornaco.xposedmoduletest.ui.action.PER-APP-SETTINGS");
                                sb.setIntent(intent);
                                sb.setIcon(Icon.createWithResource(context, android.R.drawable.ic_btn_speak_now));
                                sb.setLongLabel("PerAppSetting");
                                sb.setShortLabel("PerAppSetting");
                                ShortcutInfo shortcutInfo = sb.build();

                                List<ShortcutInfo> modified = (List<ShortcutInfo>) param.getResult();
                                if (modified != null && modified.size() > 0) {
                                    shortcutInfo.setActivity(modified.get(0).getActivity());
                                    XposedHelpers.setObjectField(shortcutInfo, "mPackageName", shortcutInfo.getActivity().getPackageName());
                                    shortcutInfo.addFlags(modified.get(0).getFlags());
                                    shortcutInfo.setRank(modified.size() + 1);
                                    modified.add(shortcutInfo);
                                    Log.d(XposedLog.TAG, "LauncherAppService add shortcut: " + shortcutInfo);
                                    Log.d(XposedLog.TAG, "LauncherAppService first shortcut: " + modified.get(0));
                                    param.setResult(modified);
                                }
                            }
                        }
                    });
            XposedLog.verbose("LauncherAppService hookGetShortcuts OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("LauncherAppService Fail hookGetShortcuts:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

}
