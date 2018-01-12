package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.AndroidAppHelper;
import android.graphics.GraphicBuffer;
import android.os.RemoteException;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class BitmapCreateFromHWBufferSubModule extends AndroidSubModule {

    private static final String PACKAGE_SYSTEM_UI = "com.android.systemui";

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        if (OSUtil.isOOrAbove()) {
            hookCreateFromHWBuffer();
        }
    }

    /**
     * @see #onCreateHWBitmap(XC_MethodHook.MethodHookParam)
     */
    private void hookCreateFromHWBuffer() {
        XposedLog.boot("hookCreateFromHWBuffer...");

        try {
            Class clz = XposedHelpers.findClass("android.graphics.Bitmap", null);

            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "createHardwareBitmap", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            try {
                                onCreateHWBitmap(param);
                            } catch (Exception e) {
                                XposedLog.boot("Fail onCreateHWBitmap: " + e);
                            }
                        }
                    });
            XposedLog.boot("hookCreateFromHWBuffer OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("Fail hookCreateFromHWBuffer: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    private void onCreateHWBitmap(XC_MethodHook.MethodHookParam param) throws RemoteException {

        String callingPkg = AndroidAppHelper.currentPackageName();
        XposedLog.verbose("onCreateHWBitmap: " + callingPkg);

        if (!PACKAGE_SYSTEM_UI.equals(callingPkg)) {
            XposedLog.verbose("onCreateHWBitmap: not system ui, skip");
            return;
        }

        GraphicBuffer buffer = (GraphicBuffer) param.args[0];
        if (buffer == null) {
            param.setResult(null);
            XposedLog.verbose("onCreateHWBitmap, hack to null while buffer is null");
        }
    }
}
