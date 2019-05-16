package github.tornaco.xposedmoduletest.xposed.submodules;

import android.os.Build;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.submodules.debug.DisplayListCanvasSubModule;
import github.tornaco.xposedmoduletest.xposed.submodules.debug.PointerEventDispatcherSubModule;
import github.tornaco.xposedmoduletest.xposed.submodules.debug.UserManagerServiceSubModule;
import github.tornaco.xposedmoduletest.xposed.submodules.debug.WindowSubModule;
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

    private final List<SubModule> SUBS = new ArrayList<>();

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

        addToSubsChecked(new AlarmManagerSubModule());
        addToSubsChecked(new WakelockSubModule());
        addToSubsChecked(new SecureSettingsSubModule());
        addToSubsChecked(new SystemSettingsSubModule());
        addToSubsChecked(new PowerManagerServiceLocalSubModule());

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

        // For some permissions.
        addToSubsChecked(new ContextImplSubModule());
        addToSubsChecked(new ServiceSubModule());
        addToSubsChecked(new ActiveServiceSubModule());
        addToSubsChecked(new RuntimeInitSubModule());

        addToSubsChecked(new AMSBroadcastIntentSubModule());
        addToSubsChecked(new AMSRemoveTaskSubModule());
        addToSubsChecked(new AMSGetRunningAppsSubModule());
        addToSubsChecked(new AMSCheckPermissionSubModule());
        addToSubsChecked(new AMSMoveTaskToBackSubModule());
        addToSubsChecked(new AMSStartProcessLockedSubModule());
        addToSubsChecked(new AMSRemoveProcessLockedSubModule());

        addToSubsChecked(new AMSRetrieveSettingsSubModule());
        addToSubsChecked(new AMSShutdownSubModule());
        addToSubsChecked(new AMSSystemReadySubModule());
        addToSubsChecked(new AMSStartSubModule());

        // Activity launching.
        addToSubsChecked(new ActivityRecordSubModule());
        addToSubsChecked(new ActivityStackSupervisorReportActivityVisibleSubModule());

        addToSubsChecked(new DeviceIdleControllerSubModule());
        addToSubsChecked(new NotificationManagerServiceSubModule());

        addToSubsChecked(new ToastSubModule());

        addToSubsChecked(new ServiceManagerSubModule());
        addToSubsChecked(new AppOpsInitSubModule());

        // To inactive an app.
        addToSubsChecked(new UsageStatsSubModule());

        // Task id manage.
        addToSubsChecked(new TaskChangeNotificationControllerSubModule());

        // AppGuard MODULES.
        addToSubsChecked(new FPSubModule());

        // Blur
        addToSubsChecked(new RecentBlurSubModule());

        addToSubsChecked(new PackageInstallerSubModule());
        addToSubsChecked(new PMSSubModule());
        addToSubsChecked(new TaskMoverSubModuleDelegate());
        addToSubsChecked(new ActivityStartSubModuleDelegate());
        addToSubsChecked(new LauncherAppServiceSubModule());

        // APPGUARD MODULES END.

        addToSubsChecked(new ActivityStackRealStartActivitySubModule());
        addToSubsChecked(new DevicePolicyManagerServiceSubModule());

        addToSubsChecked(new RuntimeSubModule());
        addToSubsChecked(new AndroidProcessSubModule());

        addToSubsChecked(new CreateRecentTaskInfoFromTaskRecordSubModule());


        // Modules to hook intent result code for GCM.
        // Enabled for PMH.
        addToSubsChecked(new AMSActivityIntentResolverSubModule());

        // System props.
        addToSubsChecked(new SystemPropSubModule());

        // Packages.
        // addToSubsChecked(new PMSInstallArgsSubModule());
        addToSubsChecked(new PMSInstallPackageLISubModule());
        addToSubsChecked(new PackageParserSubModule());

        // Submodules for debug purpose.
        // These module is under test, maybe publish for user
        // if we got a nice result:)
        if (BuildConfig.DEBUG) {
            addDebugModules();
        }

        // Sort.
        Collections.sort(SUBS, Comparable::compareTo);
    }

    private void addDebugModules() {
        addToSubsChecked(new ActivitySubModule());

        addToSubsChecked(new ResourceManagerApplyConfigSubModule());
        addToSubsChecked(new ResourceSubModule());

        // Dump broadcast details.
        addToSubsChecked(new BroadcastQueueSubModule());

        // View and event.
        addToSubsChecked(new ViewTouchEventSubModule());
        addToSubsChecked(new ViewGroupDebugDrawSubModule());

        // Window.
        addToSubsChecked(new WindowSubModule());

        // Multiple apps.
        addToSubsChecked(new UserManagerServiceSubModule());

        // Hot fix.
        addToSubsChecked(new DisplayListCanvasSubModule());
        // addToSubsChecked(new Z2ForceFixSubModule());

        // Input.
        addToSubsChecked(new PointerEventDispatcherSubModule());

        // Installer
        addToSubsChecked(new PackageInstallerServiceSubModule());
        addToSubsChecked(new PackageInstallerSessionSubModule());

        // Wakelock blocker.
        addToSubsChecked(new PowerManagerServiceSubModule());

        addToSubsChecked(new ActiveServiceForegroundNotificationCancellationSubModule());
    }

    @Synchronized
    public
    static SubModuleManager getInstance() {
        return sMe.get();
    }

    public List<SubModule> getAllSubModules() {
        return SUBS;
    }
}
