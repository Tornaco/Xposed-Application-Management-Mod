package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Build;

import java.util.HashSet;
import java.util.Set;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class SubModuleManager {

    public static final String REDEMPTION = "redemption@" + BuildConfig.VERSION_CODE;

    private static Singleton<SubModuleManager> sMe
            = new Singleton<SubModuleManager>() {
        @Override
        protected SubModuleManager create() {
            return new SubModuleManager();
        }
    };

    private final Set<SubModule> SUBS = new HashSet<>();

    private void addToSubsChecked(SubModule subModule) {

        boolean isRedemptionMode = RepoProxy.hasFileIndicator(REDEMPTION);
        if (isRedemptionMode) {
            boolean isCoreModule = subModule.isCoreModule();
            if (isCoreModule) {
                XposedLog.wtf("Still add core module even we are in redemption mode: " + subModule.name());
            } else {
                XposedLog.wtf("Won't add module because we are in redemption mode: " + subModule.name());
                return;
            }
        }

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
        addToSubsChecked(new ActiveServiceForegroundNotificationCancellationSubModule());
        addToSubsChecked(new RuntimeInitSubModule());

        addToSubsChecked(new AMSBroadcastIntentSubModule());
        addToSubsChecked(new AMSRemoveTaskSubModule());
        addToSubsChecked(new AMSGetRunningAppsSubModule());
        addToSubsChecked(new AMSCheckPermissionSubModule());
        addToSubsChecked(new AMSMoveTaskToBackSubModule());

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
        addToSubsChecked(new ToastSubModule());

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

        if (BuildConfig.DEBUG) {
            // addToSubsChecked(new DebugOnlyTestModuleErrorSubModule());
        }

        addToSubsChecked(new ActivityStackRealStartActivitySubModule());
        addToSubsChecked(new DevicePolicyManagerServiceSubModule());

        addToSubsChecked(new PackageInstallerServiceSubModule());
        addToSubsChecked(new RuntimeSubModule());
        // addToSubsChecked(new UNIXProcessSubModule());
        addToSubsChecked(new AndroidProcessSubModule());

        addToSubsChecked(new PackageParserSubModule());
        addToSubsChecked(new AMSCreateRecentTaskInfoFromTaskRecordSubModule());

        if (BuildConfig.DEBUG) {
            addToSubsChecked(new ResourceManagerApplyConfigSubModule());
            addToSubsChecked(new ResourceSubModule());
        }


        if (BuildConfig.DEBUG) {
            addToSubsChecked(new BroadcastQueueSubModule());
        }

        if (BuildConfig.DEBUG){
            addToSubsChecked(new ViewTouchEventSubModule());
            addToSubsChecked(new ViewGroupDebugDrawSubModule());
        }
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
