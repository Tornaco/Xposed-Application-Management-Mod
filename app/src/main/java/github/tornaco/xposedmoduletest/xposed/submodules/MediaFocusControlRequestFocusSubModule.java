package github.tornaco.xposedmoduletest.xposed.submodules;

import android.media.AudioAttributes;
import android.os.Binder;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class MediaFocusControlRequestFocusSubModule extends AndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);

        // L or below, locate MC in frameworks/base/media.
        if (!OSUtil.isMOrAbove()) {
            hookMediaFocusCtrlForL();
        }
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        if (OSUtil.isMOrAbove()) hookMediaFocusCtrlForMOrAbove(lpparam);
    }

    private void hookMediaFocusCtrlForMOrAbove(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookMediaFocusCtrlForMOrAbove...");
        try {
            final Class ams = XposedHelpers.findClass("com.android.server.audio.MediaFocusControl",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "requestAudioFocus", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    AudioAttributes at = null;
                    try {
                        at = (AudioAttributes) param.args[0];
                    } catch (Throwable e) {
                        XposedLog.wtf("AudioAttributes cast fail: " + Log.getStackTraceString(e));
                    }
                    int contentType = at == null ? AudioAttributes.CONTENT_TYPE_MUSIC : at.getContentType();
                    int res = (int) param.getResult();
                    int callingUid = Binder.getCallingUid();
                    getBridge().onRequestAudioFocus(contentType, res, callingUid, null);
                }
            });
            XposedLog.verbose("hookMediaFocusCtrlForMOrAbove OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hook hookMediaFocusCtrlForMOrAbove");
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void hookMediaFocusCtrlForL() {
        XposedLog.verbose("hookMediaFocusCtrlForL...");
        try {
            final Class ams = XposedHelpers.findClass("android.media.MediaFocusControl", null);
            Set unHooks = XposedBridge.hookAllMethods(ams, "requestAudioFocus", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    if (Binder.getCallingUid() > 2000) {
                        Log.e(XposedLog.TAG_DANGER, "hookMediaFocusCtrlForL this is a err caller, please file a bug!");
                        return;
                    }

                    AudioAttributes at = null;
                    try {
                        at = (AudioAttributes) param.args[0];
                    } catch (Throwable e) {
                        XposedLog.wtf("AudioAttributes cast fail: " + Log.getStackTraceString(e));
                    }
                    int contentType = at == null ? AudioAttributes.CONTENT_TYPE_MUSIC : at.getContentType();
                    int res = (int) param.getResult();
                    int callingUid = Binder.getCallingUid();
                    getBridge().onRequestAudioFocus(contentType, res, callingUid, null);
                }
            });
            XposedLog.verbose("hookMediaFocusCtrlForL OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hook hookMediaFocusCtrlForL");
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
