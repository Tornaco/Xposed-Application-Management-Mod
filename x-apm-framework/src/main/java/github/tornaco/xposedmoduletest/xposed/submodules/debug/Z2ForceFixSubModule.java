package github.tornaco.xposedmoduletest.xposed.submodules.debug;

import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.submodules.AndroidSubModule;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

// To fix:
// DIALER FC IN LOOP.
// 04-20 14:42:09.532  6058  6058 E AndroidRuntime: java.lang.NullPointerException: Attempt to invoke virtual method 'com.google.protobuf.MessageLite com.android.dialer.protos.ProtoParsers$InternalDontUse.getMessageUnsafe(com.google.protobuf.MessageLite)' on a null object reference
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at com.android.dialer.protos.ProtoParsers.get(ProtoParsers.java:42)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at com.android.dialer.protos.ProtoParsers.getTrusted(ProtoParsers.java:52)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at com.android.dialer.callintent.CallIntentParser.getCallSpecificAppData(CallIntentParser.java:38)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at com.android.incallui.call.DialerCall.parseCallSpecificAppData(DialerCall.java:807)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at com.android.incallui.call.DialerCall.<init>(DialerCall.java:281)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at com.android.incallui.call.CallList.onCallAdded(CallList.java:117)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at com.android.incallui.InCallPresenter.onCallAdded(InCallPresenter.java:536)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at com.android.incallui.InCallServiceImpl.onCallAdded(InCallServiceImpl.java:50)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at android.telecom.InCallService$2.onCallAdded(InCallService.java:257)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at android.telecom.Phone.fireCallAdded(Phone.java:353)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at android.telecom.Phone.internalAddCall(Phone.java:148)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at android.telecom.InCallService$1.handleMessage(InCallService.java:100)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at android.os.Handler.dispatchMessage(Handler.java:105)
//04-20 14:42:09.532  6058  6058 E AndroidRuntime:        at android.os.Looper.loop(Looper.j

// Source code.
// http://androidxref.com/8.0.0_r4/xref/packages/apps/Dialer/java/com/android/dialer/protos/ProtoParsers.java
public class Z2ForceFixSubModule extends AndroidSubModule {

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("com.android.dialer");
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        super.handleLoadingPackage(pkg, lpparam);
        hookGet(lpparam);
    }

    private void hookGet(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.verbose("Z2ForceFixSubModule hookGet...");
        try {
            Class clz = XposedHelpers.findClass("com.android.dialer.protos.ProtoParsers",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(clz, "get", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    Log.d(XposedLog.TAG, "Z2ForceFixSubModule, ProtoParsers#get");
                    Throwable e = param.getThrowable();
                    if (e != null) {
                        Log.d(XposedLog.TAG, "Z2ForceFixSubModule, ProtoParsers#get err: " + Log.getStackTraceString(e));
                        param.setThrowable(null);
                        param.setResult(null);
                        if (XAPMManager.get().isServiceAvailable()) {
                            XAPMManager.get().showToast("Z2ForceFixSubModule ERROR FIXED:" + e);
                        }
                    }
                }
            });
            XposedLog.verbose("Z2ForceFixSubModule hookGet OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Z2ForceFixSubModule Fail hookGet: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
