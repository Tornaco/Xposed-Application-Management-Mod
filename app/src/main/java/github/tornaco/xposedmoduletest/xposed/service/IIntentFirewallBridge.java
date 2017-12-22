package github.tornaco.xposedmoduletest.xposed.service;

import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;

/**
 * Created by guohao4 on 2017/11/1.
 * Email: Tornaco@163.com
 */

public interface IIntentFirewallBridge extends IModuleBridge {
    // API For Firewall.
    @Deprecated
    boolean checkService(ComponentName service, int callerUid) throws RemoteException;

    boolean checkService(Intent service, String callingPackage, int callingPid, int callingUid, boolean callingFromFg)
            throws RemoteException;

    boolean checkRestartService(String packageName, ComponentName componentName) throws RemoteException;

    boolean checkBroadcast(String action, int receiverUid, int callerUid) throws RemoteException;

    // FIXME  We are not ready to use this one.
    boolean checkBroadcast(Intent intent, String callerPackage, int callingPid, int callingUid) throws RemoteException;

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
}
