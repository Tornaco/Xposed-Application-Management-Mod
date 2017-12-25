package github.tornaco.xposedmoduletest.xposed.submodules;

import android.app.Service;
import android.os.Binder;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import java.util.Set;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.ITopPackageChangeListener;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

class ServiceSubModule extends IntentFirewallAndroidSubModule {

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) {
        super.initZygote(startupParam);
        hookTheFuckingService();
    }

    private void hookTheFuckingService() {
        XposedLog.verbose("hookTheFuckingService...");
        try {
            Class clz = XposedHelpers.findClass("android.app.Service", null);
            Set unHooks = XposedBridge.hookAllMethods(clz,
                    "onCreate", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(final MethodHookParam param)
                                throws Throwable {
                            super.afterHookedMethod(param);

                            // Do not hook any system service.
                            int callingUid = Binder.getCallingUid();
                            if (PkgUtil.isSystemOrPhoneOrShell(callingUid)) return;

                            final Service service = (Service) param.thisObject;
                            final String hostPackage = service.getPackageName();

                            if (XAshmanManager.get().isServiceAvailable()
                                    && XAshmanManager.get().isLazyModeEnabledForPackage(hostPackage)) {

                                Log.d(XposedLog.TAG_LAZY, "Service onCreate: " + service);

                                final Handler h = new Handler(service.getMainLooper()) {
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        try {
                                            Log.d(XposedLog.TAG_LAZY, "Service stopSelf: " + service);
                                            service.stopSelf();

                                            // UnRegister.
                                            ITopPackageChangeListener.Stub l = (ITopPackageChangeListener.Stub) msg.obj;
                                            XAshmanManager.get().unRegisterOnTopPackageChangeListener(l);
                                        } catch (Exception e) {
                                            Log.e(XposedLog.TAG_LAZY, "Error handle message:" + e);
                                        }
                                    }
                                };

                                ITopPackageChangeListener.Stub l = new ITopPackageChangeListener.Stub() {
                                    @Override
                                    public void onChange(String from, String to) throws RemoteException {
                                        if (!hostPackage.equals(to)) {
                                            h.obtainMessage(0, this).sendToTarget();
                                        }
                                    }
                                };

                                Log.d(XposedLog.TAG_LAZY, "Registering listener: " + l);
                                XAshmanManager.get().registerOnTopPackageChangeListener(l);
                            }
                        }
                    });
            XposedLog.verbose("hookTheFuckingService OK:" + unHooks);
            setStatus(unhooksToStatus(unHooks));
        } catch (Exception e) {
            XposedLog.verbose("Fail hookTheFuckingService:" + e);
            setStatus(SubModuleStatus.ERROR);
            setErrorMessage(Log.getStackTraceString(e));
        }
    }
}
