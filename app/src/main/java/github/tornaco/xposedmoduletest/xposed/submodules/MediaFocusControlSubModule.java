package github.tornaco.xposedmoduletest.xposed.submodules;

import android.media.AudioAttributes;
import android.os.Binder;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class MediaFocusControlSubModule extends IntentFirewallAndroidSubModule {

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookMediaFocusCtrl(lpparam);
    }

    private void hookMediaFocusCtrl(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("hookMediaFocusCtrl...");
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

//                    /**
//                     * Content type value to use when the content type is unknown, or other than the ones defined.
//                     */
//                    public final static int CONTENT_TYPE_UNKNOWN = 0;
//                    /**
//                     * Content type value to use when the content type is speech.
//                     */
//                    public final static int CONTENT_TYPE_SPEECH = 1;
//                    /**
//                     * Content type value to use when the content type is music.
//                     */
//                    public final static int CONTENT_TYPE_MUSIC = 2;
//                    /**
//                     * Content type value to use when the content type is a soundtrack, typically accompanying
//                     * a movie or TV program.
//                     */
//                    public final static int CONTENT_TYPE_MOVIE = 3;
//                    /**
//                     * Content type value to use when the content type is a sound used to accompany a user
//                     * action, such as a beep or sound effect expressing a key click, or event, such as the
//                     * type of a sound for a bonus being received in a game. These sounds are mostly synthesized
//                     * or short Foley sounds.
//                     */
//                    public final static int CONTENT_TYPE_SONIFICATION = 4;


                    int contentType = at == null ? AudioAttributes.CONTENT_TYPE_MUSIC : at.getContentType();
                    int res = (int) param.getResult();
                    int callingUid = Binder.getCallingUid();
                    getIntentFirewallBridge().onRequestAudioFocus(contentType, res, callingUid, null);
                }
            });
            XposedLog.verbose("hookMediaFocusCtrl OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hook hookMediaFocusCtrl");
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
