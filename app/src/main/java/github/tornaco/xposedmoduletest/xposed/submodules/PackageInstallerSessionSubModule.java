package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Binder;
import android.util.Log;

import com.google.common.io.Files;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class PackageInstallerSessionSubModule extends AndroidSubModule {
    @Override
    public String needBuildVar() {
        return XAppBuildVar.APP_OPS;
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        hookCommit(lpparam);
    }

    private void hookCommit(XC_LoadPackage.LoadPackageParam lpparam) {
        logOnBootStage("PackageInstallerSessionSubModule hookCommit...");
        try {
            final Class ams = XposedHelpers.findClass("com.android.server.pm.PackageInstallerSession",
                    lpparam.classLoader);
            Set unHooks = XposedBridge.hookAllMethods(ams, "commit", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    int callingUid = Binder.getCallingUid();
                    XposedLog.verbose("commit @PackageInstallerSession, caller: " + callingUid
                            + ", args: " + Arrays.toString(param.args));
                    Object session = param.thisObject;
                    File stageDir = (File) XposedHelpers.getObjectField(session, "stageDir");
                    XposedLog.verbose("commit @PackageInstallerSession, stageDir: " + stageDir + ", isFile?" + stageDir.isFile());
                    Iterable<File> subFiles = Files.fileTreeTraverser().postOrderTraversal(stageDir);
                    for (File f : subFiles) {
                        XposedLog.verbose("commit @PackageInstallerSession, subFiles: " + f + ", isFile?" + f.isFile());
                    }
                }
            });
            logOnBootStage("PackageInstallerSessionSubModule hookCommit OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            logOnBootStage("PackageInstallerSessionSubModule Fail hookCommit: " + Log.getStackTraceString(e));
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
