package github.tornaco.xposedmoduletest.xposed.submodules;

import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/3/27 12:09.
 * God bless no bug!
 */

public class ThumbnalDataSubModule extends AbsSubModule {

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet("com.android.systemui");
    }

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.O;
    }

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_BLUR;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        super.handleLoadingPackage(pkg, lpparam);
        hookCreateFromTaskSnapshot(lpparam);
    }

    private void hookCreateFromTaskSnapshot(XC_LoadPackage.LoadPackageParam lpparam) {
        XposedLog.boot("hookCreateFromTaskSnapshot...");

        try {
            Class clz = XposedHelpers.findClass("com.android.systemui.recents.model.ThumbnailData ",
                    lpparam.classLoader);

            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "createFromTaskSnapshot", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Object res = param.getResult();
                            Log.d(XposedLog.TAG, "BLUR createFromTaskSnapshot, res: " + res);
//                            ThumbnailDataProxy proxy = new ThumbnailDataProxy(res);
//                            Bitmap in = proxy.getThumbnail();
//                            proxy.setThumbnail(null);
//                            Log.d(XposedLog.TAG, "createFromTaskSnapshot, replaced!");
                        }
                    });
            XposedLog.boot("hookCreateFromTaskSnapshot OK: " + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.boot("Fail hookCreateFromTaskSnapshot: " + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }

    class ThumbnailDataProxy extends InvokeTargetProxy<Object> {

        public ThumbnailDataProxy(Object host) {
            super(host);
        }

        Bitmap getThumbnail() {
            return (Bitmap) XposedHelpers.getObjectField(getHost(), "thumbnail");
        }

        void setThumbnail(Bitmap in) {
            XposedHelpers.setObjectField(getHost(), "thumbnail", in);
        }
    }
}
