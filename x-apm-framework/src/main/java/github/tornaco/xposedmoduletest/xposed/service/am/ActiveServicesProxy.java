package github.tornaco.xposedmoduletest.xposed.service.am;

import android.content.Intent;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseArray;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XposedHelpers;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.util.ReflectionUtils;
import github.tornaco.xposedmoduletest.xposed.service.ErrorCatchRunnable;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.ClazzDumper;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/25 10:09.
 * God bless no bug!
 */
public class ActiveServicesProxy extends InvokeTargetProxy<Object> {

    // FIELD OF CLASS class com.android.server.am.ActiveServicesManager: final com.android.server.am.ActivityManagerService com.android.server.am.ActiveServicesManager.mAm
    //FIELD OF CLASS class com.android.server.am.ActiveServicesManager: private com.android.server.am.ActiveServices com.android.server.am.ActiveServicesManager.mMainServices
    //FIELD OF CLASS class com.android.server.am.ActiveServicesManager: private android.util.SparseArray com.android.server.am.ActiveServicesManager.mServices
    //FIELD OF CLASS class com.android.server.am.ActiveServicesManager: private static final boolean com.android.server.am.ActiveServicesManager.DEBUG_ALL
    //FIELD OF CLASS class com.android.server.am.ActiveServicesManager: private static final int com.android.server.am.ActiveServicesManager.MULTI_INSTANCE_ID
    //FIELD OF CLASS class com.android.server.am.ActiveServicesManager: private static final java.lang.String com.android.server.am.ActiveServicesManager.TAG
    public ActiveServicesProxy(Object host) {
        super(host);
        // ClazzDumper.dump(getHost().getClass(), new ClazzDumper.FilePrinter("ActiveServicesProxy"));

        // Check host.
        try {
            if (host.getClass().getName().contains("com.android.server.am.ActiveServicesManager")) {
                XposedLog.wtf("ActiveServicesProxy fix reference for 360OS!!!");
                Object realHost = XposedHelpers.getObjectField(host, "mMainServices");
                if (realHost != null) {
                    setHost(realHost);
                    XposedLog.wtf("ActiveServicesProxy fix reference for 360OS by: " + realHost);
                }
            }
        } catch (Throwable e) {
            XposedLog.wtf("ActiveServicesProxy fail fix for 360OS!!: " + Log.getStackTraceString(e));
        }
    }

    private SparseArray getMServiceMapField() {
        try {
            return (SparseArray) XposedHelpers.getObjectField(getHost(), "mServiceMap");
        } catch (Exception e) {
            XposedLog.wtf("ActiveServicesProxy Fail getMServiceMapField: " + Log.getStackTraceString(e));
            new ErrorCatchRunnable(() -> ClazzDumper.dump(getHost().getClass(), ClazzDumper.ANDROID_UTIL_LOG_PRINTER),
                    "getMServiceMapField dump class").run();
            return null;
        }
    }

    // Checked N M
    private ServiceMapProxy getServiceMapProxy(int uid) {
        SparseArray mServiceMap = getMServiceMapField();
        if (mServiceMap == null) {
            XposedLog.wtf("ActiveServicesProxy getServiceMapProxy, mServiceMap is null");
            return null;
        }
        Object servicesObject = mServiceMap.get(UserHandle.getUserId(uid));
        if (servicesObject == null) {
            XposedLog.wtf("ActiveServicesProxy getServiceMapProxy, servicesObject is null");
            return null;
        }
        return new ServiceMapProxy(servicesObject);
    }

    @SuppressWarnings("unchecked")
    // Checked N M
    public List<Object> getServiceRecords(int uid, String[] packageNames) {
        if (packageNames == null || packageNames.length == 0) return new ArrayList<>(0);

        ServiceMapProxy serviceMapProxy = getServiceMapProxy(uid);
        if (serviceMapProxy == null) {
            XposedLog.wtf("ActiveServicesProxy getServiceRecords, serviceMapProxy is null");
            return new ArrayList<>(0);
        }

        ArrayMap mServicesByName = serviceMapProxy.getMServicesByNameField();
        if (mServicesByName == null) {
            XposedLog.wtf("ActiveServicesProxy getServiceRecords, servicesObject is null");
            return new ArrayList<>(0);
        }
        // Copy to make thread safe.
        ArrayMap mServicesByNameSafe = new ArrayMap(mServicesByName);
        List<Object> res = new ArrayList<>();
        for (int i = mServicesByNameSafe.size() - 1; i >= 0; i--) {
            if (BuildConfig.DEBUG) {
                XposedLog.verbose("ActiveServicesProxy getServiceRecords: " + mServicesByNameSafe.valueAt(i));
            }
            Object serviceRecordObject = mServicesByNameSafe.valueAt(i);
            ServiceRecordProxy serviceRecordProxy = new ServiceRecordProxy(serviceRecordObject);
            String pkg = serviceRecordProxy.getPackageName();
            if (BuildConfig.DEBUG) {
                XposedLog.verbose("ActiveServicesProxy getServiceRecords, pkg: " + pkg);
            }
            // Dose someone explicitly called start?
            List<String> candidates = Arrays.asList(packageNames);
            if (pkg != null && candidates.contains(pkg) && serviceRecordProxy.isStartRequested()) {
                res.add(serviceRecordObject);
                if (BuildConfig.DEBUG) {
                    XposedLog.verbose("ActiveServicesProxy getServiceRecords, adding: " + serviceRecordObject);
                }
            }
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<Object> getServiceRecords(int uid, Intent intent) {
        if (intent == null || intent.getComponent() == null) return new ArrayList<>(0);

        ServiceMapProxy serviceMapProxy = getServiceMapProxy(uid);
        if (serviceMapProxy == null) {
            XposedLog.wtf("ActiveServicesProxy getServiceRecords, serviceMapProxy is null");
            return new ArrayList<>(0);
        }

        ArrayMap mServicesByName = serviceMapProxy.getMServicesByNameField();
        if (mServicesByName == null) {
            XposedLog.wtf("ActiveServicesProxy getServiceRecords, servicesObject is null");
            return new ArrayList<>(0);
        }
        // Copy to make thread safe.
        ArrayMap mServicesByNameSafe = new ArrayMap(mServicesByName);
        List<Object> res = new ArrayList<>();

        for (int i = mServicesByNameSafe.size() - 1; i >= 0; i--) {
            if (BuildConfig.DEBUG) {
                XposedLog.verbose("ActiveServicesProxy getServiceRecords: " + mServicesByNameSafe.valueAt(i));
            }
            Object serviceRecordObject = mServicesByNameSafe.valueAt(i);
            ServiceRecordProxy serviceRecordProxy = new ServiceRecordProxy(serviceRecordObject);
            if (intent.getComponent().equals(serviceRecordProxy.getName())) {
                XposedLog.verbose("ActiveServicesProxy add serviceRecordObject: " + serviceRecordObject);
                res.add(serviceRecordObject);
            }
        }
        return res;
    }

    public void stopServicesForPackageUid(int uid, String packageName, ActiveServicesServiceStopper serviceStopper) {
        stopServicesForPackageUid(uid, new String[]{packageName}, serviceStopper);
    }

    // Checked N M
    public void stopServicesForPackageUid(int uid, String[] packageNames, ActiveServicesServiceStopper serviceStopper) {
        XposedLog.verbose("ActiveServicesProxy stopServicesForPackageUid: " + Arrays.toString(packageNames));
        new ErrorCatchRunnable(() -> ClazzDumper.dump(getHost().getClass(), ClazzDumper.ANDROID_UTIL_LOG_PRINTER),
                "stopServicesForPackageUid dump class").run();

        List<Object> serviceRecords = getServiceRecords(uid, packageNames);

        if (serviceRecords != null) {

            ServiceMapProxy serviceMapProxy = getServiceMapProxy(uid);
            if (serviceMapProxy == null) {
                XposedLog.wtf("ActiveServicesProxy stopServicesForPackageUid, serviceMapProxy is null");
                return;
            }

            for (int i = serviceRecords.size() - 1; i >= 0; i--) {

                Object serviceRecordObject = serviceRecords.get(i);

                ServiceRecordProxy serviceRecordProxy = new ServiceRecordProxy(serviceRecordObject);

                // This error can be ignored.
                new ErrorCatchRunnable(() -> {
                    serviceRecordProxy.setDelayed(false);
                    serviceMapProxy.ensureNotStartingBackground(serviceRecordObject);
                }, "LAZY setDelayed and ensureNotStartingBackground").run();

                boolean stopped = serviceStopper.stopService(serviceRecordProxy);

                if (XposedLog.isVerboseLoggable()) {
                    XposedLog.verbose("ActiveServicesProxy stopServicesForPackageUid, stopped: " + serviceRecordObject + ", res: " + stopped);
                }
            }
        }
    }

    public boolean stopServiceLocked(Object serviceRecordObj) {
        return callStopServiceLockChecked(serviceRecordObj);
    }

    private static final String HWActiveServiceClassName = "com.android.server.am.HwActiveServices";
    private static final String ActiveServiceClassName = "com.android.server.am.ActiveServices";
    private static final String stopServiceLockMethodName = "stopServiceLocked";
    private static Method sCacheStopServiceLockedMethod = null;

    private boolean callStopServiceLockChecked(Object serviceRecordObj) {

        if (BuildConfig.DEBUG) {
            Class hostClass = getHost().getClass();
            new ErrorCatchRunnable(() -> ClazzDumper.dump(hostClass, ClazzDumper.ANDROID_UTIL_LOG_PRINTER), "ClazzDumper.dump for debug.").run();
        }

        try {
            Method stopMethod = sCacheStopServiceLockedMethod;
            if (stopMethod == null) {
                Class paramClz = serviceRecordObj.getClass();
                stopMethod = ReflectionUtils.findMethod(getHost().getClass(), stopServiceLockMethodName, paramClz);
                sCacheStopServiceLockedMethod = stopMethod;
            }

            XposedLog.verbose("ActiveServicesProxy callStopServiceLockChecked: " + stopMethod);

            if (stopMethod == null) {
                XposedLog.wtf("ActiveServicesProxy can not find stop method!!!");
                return false;
            }

            ReflectionUtils.makeAccessible(stopMethod);
            ReflectionUtils.invokeMethod(stopMethod, getHost(), serviceRecordObj);
            return true;
        } catch (Throwable e) {
            XposedLog.wtf("FATAL *** ActiveServicesProxy fail callStopServiceLockChecked: " + Log.getStackTraceString(e));
            sCacheStopServiceLockedMethod = null;
            new ErrorCatchRunnable(() -> ClazzDumper.dump(getHost().getClass(), ClazzDumper.ANDROID_UTIL_LOG_PRINTER),
                    "callStopServiceLockChecked dump class").run();
            return false;
        }
    }

    static class ServiceMapProxy extends InvokeTargetProxy<Object> {

        ServiceMapProxy(Object host) {
            super(host);
        }

        // Checked N M
        android.util.ArrayMap getMServicesByNameField() {
            try {
                return (ArrayMap) XposedHelpers.getObjectField(getHost(), "mServicesByName");
            } catch (Exception e) {
                XposedLog.wtf("ActiveServicesProxy Fail getMServicesByNameField: " + Log.getStackTraceString(e));
                new ErrorCatchRunnable(() -> ClazzDumper.dump(getHost().getClass(), ClazzDumper.ANDROID_UTIL_LOG_PRINTER),
                        "getMServicesByNameField dump class").run();
                return null;
            }
        }

        private static String sEnsureNotNMethodName = "ensureNotStartingBackground";
        private static String sEnsureNotNLockedMethodName = "ensureNotStartingBackgroundLocked";

        // Checked N M O.
        // https://github.com/LineageOS/android_frameworks_base/blob/cm-14.0/services/core/java/com/android/server/am/ActiveServices.java
        void ensureNotStartingBackground(Object serviceRecordObj) {
            invokeMethod(sEnsureNotNLockedMethodName, serviceRecordObj);
            // Try twice.
            invokeMethod(sEnsureNotNMethodName, serviceRecordObj);
        }
    }
}
