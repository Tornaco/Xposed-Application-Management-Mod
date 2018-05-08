package github.tornaco.xposedmoduletest.xposed.service.pm;

import android.util.Log;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/5/8 12:52.
 * God bless no bug!
 */
public class InstallArgsProxy extends InvokeTargetProxy<Object> {

    public InstallArgsProxy(Object host) {
        super(host);
    }

    public Object getMoveInfoObject() {
        try {
            return XposedHelpers.getObjectField(getHost(), "move");
        } catch (Throwable e) {
            XposedLog.wtf("InstallArgsProxy fail getMoveInfoObject: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public Object getOriginInfoObject() {
        try {
            return XposedHelpers.getObjectField(getHost(), "origin");
        } catch (Throwable e) {
            XposedLog.wtf("InstallArgsProxy fail getOriginInfoObject: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public String getInstallerPackageName() {
        try {
            return (String) XposedHelpers.getObjectField(getHost(), "installerPackageName");
        } catch (Throwable e) {
            XposedLog.wtf("InstallArgsProxy fail getInstallerPackageName: " + Log.getStackTraceString(e));
            return null;
        }
    }
}
