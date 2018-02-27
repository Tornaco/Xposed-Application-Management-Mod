package github.tornaco.xposedmoduletest.xposed.service;

import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.KeyEvent;

import github.tornaco.xposedmoduletest.xposed.service.doze.DeviceIdleControllerProxy;
import github.tornaco.xposedmoduletest.xposed.service.dpm.DevicePolicyManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.notification.NotificationManagerServiceProxy;
import github.tornaco.xposedmoduletest.xposed.service.policy.PhoneWindowManagerProxy;
import github.tornaco.xposedmoduletest.xposed.submodules.SubModule;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public interface IModuleBridge {

    void onModuleInitError(SubModule module);

    @CoreApi
    @CommonBringUpApi
    void attachContext(Context context);

    @CoreApi
    @CommonBringUpApi
    void publish();

    @CoreApi
    @CommonBringUpApi
    void systemReady();

    /**
     * System is ready, we should retrieve settings now before other service start.
     */
    @CommonBringUpApi
    void retrieveSettings();

    @CommonBringUpApi
    void shutdown();

    @CommonBringUpApi
    void onPackageMoveToFront(Intent who);

    @CommonBringUpApi
    String serial();

    @CommonBringUpApi
    boolean onKeyEvent(KeyEvent keyEvent, String source);

    @CommonBringUpApi
    boolean checkBroadcastIntentSending(IApplicationThread caller,
                                        Intent intent
//            , String resolvedType, IIntentReceiver resultTo,
//                                 int resultCode, String resultData, Bundle map,
//                                 String requiredPermission, int appOp, boolean serialized, boolean sticky, int userId
    );

    @CommonBringUpApi
    void notifyTaskCreated(int taskId, ComponentName componentName);

    @CommonBringUpApi
    ComponentName componentNameForTaskId(int taskId);

    // API for AppGuard.
    boolean interruptPackageRemoval(String pkg);

    boolean interruptPackageDataClear(String pkg);

    void notifyPackageRemovalInterrupt(String pkg);

    void notifyPackageDataClearInterrupt(String pkg);

    boolean onEarlyVerifyConfirm(String pkg, String reason);

    void verify(Bundle options, String pkg, ComponentName componentName,
                int uid, int pid, VerifyListener listener);

    Intent checkIntent(Intent from);

    long wrapCallingUidForIntent(long from, Intent intent);

    boolean isBlurForPkg(String pkg);

    int getBlurRadius() throws RemoteException;

    float getBlurScale();

    boolean interruptFPSuccessVibrate();

    boolean interruptFPErrorVibrate();

    boolean isActivityStartShouldBeInterrupted(ComponentName componentName);

    // API for AppGuard end.


    // API For ASH.
    void attachDeviceIdleController(DeviceIdleControllerProxy proxy);

    void attachNotificationService(NotificationManagerServiceProxy proxy);

    void attachDevicePolicyManagerService(DevicePolicyManagerServiceProxy proxy);

    void attachPhoneWindowManager(PhoneWindowManagerProxy proxy);

    boolean checkService(ComponentName service, int callerUid) throws RemoteException;

    @Deprecated
    boolean checkService(Intent service, String callingPackage, int callingPid, int callingUid, boolean callingFromFg)
            throws RemoteException;

    boolean checkRestartService(String packageName, ComponentName componentName) throws RemoteException;

    boolean checkBroadcast(Intent intent, int receiverUid, int callerUid) throws RemoteException;

    // FIXME  We are not ready to use this one.
    boolean checkBroadcastDeliver(Intent intent, String callerPackage, int callingPid, int callingUid) throws RemoteException;

    void onActivityDestroy(Intent intent, String reason);

    boolean checkComponentSetting(ComponentName componentName,
                                  int newState,
                                  int flags,
                                  int callingUid);


    // Network manager api.
    void onNetWorkManagementServiceReady(NativeDaemonConnector connector);

    void onRequestAudioFocus(int type, int res, int callingUid, String callingPkg);

    void onAbandonAudioFocus(int res, int callingUid, String callingPkg);

    int checkPermission(String perm, int pid, int uid);

    int checkOperation(int code, int uid, String packageName, String reason);

    boolean resident(String pkgName);

    boolean residentEnableInternal();

    boolean isPanicLockEnabled() throws RemoteException;

    int getRecentTaskExcludeSetting(ComponentName c) throws RemoteException;
    // API For ASH END.
}
