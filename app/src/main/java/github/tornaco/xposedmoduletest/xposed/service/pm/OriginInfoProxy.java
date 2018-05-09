package github.tornaco.xposedmoduletest.xposed.service.pm;

import android.util.Log;

import java.io.File;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/5/8 13:25.
 * God bless no bug!
 */
public class OriginInfoProxy extends InvokeTargetProxy<Object> {

    public OriginInfoProxy(Object host) {
        super(host);
    }

    public File getFile() {
        try {
            File file = (File) XposedHelpers.getObjectField(getHost(), "file");
            XposedLog.verbose(XposedLog.PREFIX_PM + "OriginInfoProxy file: " + file);
            return InstallerUtil.getMonolithicPackageFile(file);
        } catch (Throwable e) {
            XposedLog.wtf("OriginInfoProxy fail getFile: " + Log.getStackTraceString(e));
            return null;
        }
    }
}
