package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Build;

import java.util.HashSet;
import java.util.Set;

import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class IntentFirewallSubModuleManager {

    private static IntentFirewallSubModuleManager sMe;

    private final Set<SubModule> SUBS = new HashSet<>();

    private void addToSubsChecked(SubModule subModule) {
        String var = subModule.needBuildVar();

        if (var == null || XAppBuildVar.BUILD_VARS.contains(var)) {
            int minSDK = subModule.needMinSdk();
            if (Build.VERSION.SDK_INT >= minSDK) {
                SUBS.add(subModule);
            } else {
                XposedLog.boot("Skip submodule for min sdk not match: " + subModule.name());
            }
        } else {
            XposedLog.boot("Skip submodule for var not match: " + subModule.name());
        }
    }

    private IntentFirewallSubModuleManager() {
        addToSubsChecked(new ServiceSubModule());

        addToSubsChecked(new AlarmManagerSubModule());
        addToSubsChecked(new WakelockSubModule());
        addToSubsChecked(new SecureSettingsSubModule());

        addToSubsChecked(new TelephonyManagerSubModule());

        addToSubsChecked(new NetworkManagementModule());

        addToSubsChecked(new MediaFocusControlSubModule2());
        addToSubsChecked(new MediaFocusControlSubModule());

        // RFK.
        addToSubsChecked(new PWMSubModule());
        addToSubsChecked(new InputManagerSubModule());

        addToSubsChecked(new PMSSubModule4());
        addToSubsChecked(new PMSSubModule2());

        addToSubsChecked(new ASDSubModule());
        addToSubsChecked(new IFWSubModule());

        addToSubsChecked(new AppOpsSubModule4());
        addToSubsChecked(new AppOpsSubModule3());
        addToSubsChecked(new AppOpsSubModule2());
        addToSubsChecked(new AppOpsSubModule());

        addToSubsChecked(new ActiveServiceSubModule());
        addToSubsChecked(new RuntimeInitSubModule());

        addToSubsChecked(new AMSSubModule10());
        addToSubsChecked(new AMSSubModule9());
        addToSubsChecked(new AMSSubModule8());
        addToSubsChecked(new AMSSubModule6());
        addToSubsChecked(new AMSSubModule5());
        addToSubsChecked(new AMSSubModule4());
        addToSubsChecked(new AMSSubModule3());
        addToSubsChecked(new AMSSubModule2());
        addToSubsChecked(new AMSSubModule());

        addToSubsChecked(new DeviceIdleControllerSubModule());
        addToSubsChecked(new NotificationManagerServiceSubModule());
    }

    @Synchronized
    public
    static IntentFirewallSubModuleManager getInstance() {
        if (sMe == null) sMe = new IntentFirewallSubModuleManager();
        return sMe;
    }

    public Set<SubModule> getAllSubModules() {
        return SUBS;
    }
}
