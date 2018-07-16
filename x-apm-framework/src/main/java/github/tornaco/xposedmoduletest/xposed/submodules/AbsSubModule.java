package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.io.Files;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.service.IModuleBridge;
import github.tornaco.xposedmoduletest.xposed.util.DateUtils;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

abstract class AbsSubModule implements SubModule {
    @Getter
    private IModuleBridge bridge;

    @Getter
    private SubModuleStatus status = SubModuleStatus.UNKNOWN;

    @Getter
    private String errorMessage;

    @Override
    public String needBuildVar() {
        return null;
    }

    @Override
    public int needMinSdk() {
        return Build.VERSION_CODES.LOLLIPOP;
    }

    @Override
    public Priority priority() {
        return Priority.Normal;
    }

    @Override
    public int compareTo(@NonNull SubModule subModule) {
        if (isCoreModule()) {
            return -1;
        }

        if (!isCoreModule() && subModule.isCoreModule()) {
            return 1;
        }

        if (priority().ordinal() > subModule.priority().ordinal()) {
            return -1;
        }

        if (priority() == subModule.priority()) {
            return 0;
        }

        return 1;
    }

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        // Empty.
    }

    @Override
    public void handleLoadingPackage(String pkg, XC_LoadPackage.LoadPackageParam lpparam) {
        // Empty.
    }

    @Override
    public void onBridgeCreate(IModuleBridge bridge) {
        this.bridge = bridge;
        XposedLog.boot("onBridgeCreate@" + bridge.serial() + ", assign to: " + getClass().getName());
    }

    @Override
    public void onBridgeChange(IModuleBridge bridge) {
        this.bridge = bridge;
        XposedLog.boot("onBridgeChange@" + bridge.serial() + ", assign to: " + getClass().getName());
    }

    public void setStatus(SubModuleStatus status) {
        this.status = status;
        if (this.status == SubModuleStatus.ERROR) {
            getBridge().onModuleInitError(this);
        }
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        // Dump err.
        try {
            File errDir = RepoProxy.getSystemErrorTraceDirByVersion();
            String moduleTag = getClass().getSimpleName();
            String errFileName = errDir.getPath() + File.separator + moduleTag + "-" + DateUtils.formatForFileName(System.currentTimeMillis()) + ".err";
            File errFile = new File(errFileName);
            XposedLog.wtf("Dump module err message to: " + errFileName);
            Files.createParentDirs(errFile);
            Files.asByteSink(errFile).asCharSink(Charset.defaultCharset())
                    .write(errorMessage);
            XposedLog.wtf("Module error trace has been write to: " + errFile);

        } catch (Throwable e) {
            XposedLog.wtf("Module error trace dump fail: " + Log.getStackTraceString(e));
        }
    }

    @Override
    public boolean isCoreModule() {
        return false;
    }

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    public void logOnBootStage(Object log) {
        XposedLog.boot(getClass().getName() + "- " + log);
    }

    public void logOnBootStage(String format, Object... args) {
        XposedLog.boot(String.format(format, args));
    }

    public static SubModuleStatus unhooksToStatus(Set unHooks) {
        if (unHooks == null || unHooks.size() == 0) return SubModuleStatus.ERROR;
        return SubModuleStatus.READY;
    }

    public static SubModuleStatus unhookToStatus(XC_MethodHook.Unhook unHooks) {
        if (unHooks == null) return SubModuleStatus.ERROR;
        return SubModuleStatus.READY;
    }
}
