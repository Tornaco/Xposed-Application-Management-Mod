package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Build;

import java.util.HashSet;
import java.util.Set;

import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class SubModuleManager {

    private static Singleton<SubModuleManager> sMe = new Singleton<SubModuleManager>() {
        @Override
        protected SubModuleManager create() {
            return new SubModuleManager();
        }
    };

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

    private SubModuleManager() {
        addToSubsChecked(new PMSClearDataSubModule());

        addToSubsChecked(new ServiceSubModule());

        addToSubsChecked(new AlarmManagerSubModule());
        addToSubsChecked(new WakelockSubModule());
        addToSubsChecked(new SecureSettingsSubModule());

        addToSubsChecked(new TelephonyManagerSubModule());

        addToSubsChecked(new NetworkManagementModule());

        addToSubsChecked(new MediaFocusControlAbandonFocusSubModule());
        addToSubsChecked(new MediaFocusControlRequestFocusSubModule());

        // RFK.
        addToSubsChecked(new PWMInterceptKeySubModule());
        addToSubsChecked(new InputManagerInjectInputSubModule());

        addToSubsChecked(new PMSGetInstalledPackagesSubModule());
        addToSubsChecked(new PMSSetComponentEnabledSettingSubModule());

        addToSubsChecked(new ASDSubModule());
        addToSubsChecked(new IFWSubModule());

        addToSubsChecked(new AppOpsSubModule4());
        addToSubsChecked(new AppOpsSubModule3());
        addToSubsChecked(new AppOpsSubModule2());
        addToSubsChecked(new AppOpsSubModule());

        addToSubsChecked(new ActiveServiceSubModule());
        addToSubsChecked(new RuntimeInitSubModule());

        addToSubsChecked(new AMSBroadcastIntentSubModule());
        addToSubsChecked(new AMSSubModule10());
        addToSubsChecked(new AMSSubModule9());
        addToSubsChecked(new AMSSubModule8());
        addToSubsChecked(new AMSSubModule6());

        addToSubsChecked(new AMSSetFocusedActivitySubModule());
        addToSubsChecked(new ActivityStackSupervisorSetFocusedStackSubModule());

        addToSubsChecked(new AMSRetrieveSettingsSubModule());
        addToSubsChecked(new AMSShutdownSubModule());
        addToSubsChecked(new AMSSystemReadySubModule());
        addToSubsChecked(new AMSStartSubModule());

        addToSubsChecked(new ActivityRecordSubModule());

        addToSubsChecked(new DeviceIdleControllerSubModule());
        addToSubsChecked(new NotificationManagerServiceSubModule());

        addToSubsChecked(new ActivitySubModule());

        addToSubsChecked(new ServiceManagerSubModule());
        addToSubsChecked(new AppOpsInitSubModule());

        // Task id manage.
        addToSubsChecked(new TaskChangeNotificationControllerSubModule());

        // APPGUARD MODULES.
        addToSubsChecked(new FPSubModule());

        // Blur
        addToSubsChecked(new ScreenshotApplicationsSubModule());

        addToSubsChecked(new PackageInstallerSubModule());
        addToSubsChecked(new PMSSubModule());
        addToSubsChecked(new TaskMoverSubModuleDelegate());
        addToSubsChecked(new ActivityStartSubModuleDelegate());
        addToSubsChecked(new LauncherAppServiceSubModule());

        addToSubsChecked(new AMSSetFocusedActivitySubModule());
        addToSubsChecked(new ActivityStackSupervisorSetFocusedStackSubModule());

        // APPGUARD MODULES END.
    }

    @Synchronized
    public
    static SubModuleManager getInstance() {
        return sMe.get();
    }

    public Set<SubModule> getAllSubModules() {
        return SUBS;
    }
}
