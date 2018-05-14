package github.tornaco.xposedmoduletest.xposed.service.am;

import android.content.ComponentName;
import android.util.Log;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/25 10:57.
 * God bless no bug!
 */
public class ServiceRecordProxy extends InvokeTargetProxy<Object> {

    private String mPackageName;
    private ComponentName mName;

    ServiceRecordProxy(Object host) {
        super(host);
    }

    public String getPackageName() {
        if (mPackageName != null) {
            return mPackageName;
        }
        try {
            mPackageName = (String) XposedHelpers.getObjectField(getHost(), "packageName");
            return mPackageName;
        } catch (Exception e) {
            XposedLog.wtf("ServiceRecordProxy Fail getPackageName: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public ComponentName getName() {
        if (mName != null) {
            return mName;
        }
        try {
            mName = (ComponentName) XposedHelpers.getObjectField(getHost(), "name");
            return mName;
        } catch (Exception e) {
            XposedLog.wtf("ServiceRecordProxy Fail getName: " + Log.getStackTraceString(e));
            return null;
        }
    }

    public void setDelayed(boolean delayed) {
        setBooleanField("delayed", delayed);
    }

    public boolean isStartRequested() {
        try {
            return XposedHelpers.getBooleanField(getHost(), "startRequested");
        } catch (Exception e) {
            XposedLog.wtf("ServiceRecordProxy Fail get isStartRequested: " + Log.getStackTraceString(e));
            return true; // Default???
        }
    }

    @Override
    public String toString() {
        return getHost() == null ? "NULL" : getHost().toString();
    }
}
