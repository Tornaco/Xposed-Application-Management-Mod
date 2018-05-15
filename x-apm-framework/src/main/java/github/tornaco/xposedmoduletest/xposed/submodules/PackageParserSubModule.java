package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.pm.PackageParser;
import android.os.Binder;
import android.util.Log;

import java.io.File;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.pm.InstallerUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

class PackageParserSubModule extends AndroidSubModule {

    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_PACKAGE_INSTALL_VERIFY;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookParseBaseApk();
    }

    private void hookParseBaseApk() {
        XposedLog.verbose("hookParseBaseApk...");
        try {
            Class clz = XposedHelpers.findClass("android.content.pm.PackageParser", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "parseBaseApk", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            XposedLog.verbose(XposedLog.PREFIX_PM
                                    + "parseBaseApk: %s %s %s", Binder.getCallingUid(), param.args[0], param.getResult());
                            Object arg0 = param.args[0];
                            if (arg0 instanceof File) {
                                File file = (File) arg0;
                                boolean isSourceApkFile = InstallerUtil.isSourcePackageFilePath(file.getAbsolutePath());
                                if (isSourceApkFile) {
                                    Object result = param.getResult();
                                    if (result != null) {
                                        PackageParser.Package pkg = (PackageParser.Package) result;
                                        String apkPackageName = pkg.packageName;
                                        // Try setup with aidl.
                                        XAPMManager.get().onSourceApkFileDetected(file.getAbsolutePath(), apkPackageName);
                                    }
                                }
                            }
                        }
                    });
            XposedLog.verbose("hookParseBaseApk OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Throwable e) {
            XposedLog.verbose("Fail hookParseBaseApk:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
