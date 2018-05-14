package github.tornaco.xposedmoduletest.xposed.app;

import android.app.Service;
import android.content.ComponentName;
import android.util.Log;

import github.tornaco.xposedmoduletest.IServiceControl;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Tornaco on 2018/4/28 9:40.
 * God bless no bug!
 */

@AllArgsConstructor
@ToString
public class IServiceControlAdapter extends IServiceControl.Stub {
    @Getter
    private Service service;
    @Getter
    private ComponentName serviceName;

    @Override
    public void stopService() {
        if (getService() != null) {
            getService().stopSelf();
            Log.d(XposedLog.TAG_LAZY, "Stop self: " + getService() + ", serviceName: " + serviceName);
        }
    }

    @Override
    public ComponentName getServiceComponent() {
        return serviceName;
    }
}
