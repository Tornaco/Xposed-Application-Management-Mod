package github.tornaco.xposedmoduletest.xposed.service.am;

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
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/4/25 10:09.
 * God bless no bug!
 */
public class ActiveServicesProxy extends InvokeTargetProxy<Object> {

    public ActiveServicesProxy(Object host) {
        super(host);
    }

    private SparseArray getMServiceMapField() {
        try {
            return (SparseArray) XposedHelpers.getObjectField(getHost(), "mServiceMap");
        } catch (Exception e) {
            XposedLog.wtf("ActiveServicesProxy Fail getMServiceMapField: " + Log.getStackTraceString(e));
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

    public void stopServicesForPackageUid(int uid, String packageName, StopServiceConfirm confirmer) {
        stopServicesForPackageUid(uid, new String[]{packageName}, confirmer);
    }

    // Checked N M
    public void stopServicesForPackageUid(int uid, String[] packageNames, StopServiceConfirm confirmer) {
        XposedLog.wtf("ActiveServicesProxy stopServicesForPackageUid: " + Arrays.toString(packageNames));

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

                // Check if confirm to stop it!
                if (confirmer != null && !confirmer.confirmToStop(serviceRecordProxy)) {
                    XposedLog.wtf("ActiveServicesProxy stopServicesForPackageUid, not confirmed: " + serviceRecordProxy);
                    continue;
                }

                serviceRecordProxy.setDelayed(false);

                serviceMapProxy.ensureNotStartingBackground(serviceRecordObject);

                stopServiceLocked(serviceRecordObject);
                XposedLog.verbose("ActiveServicesProxy stopServicesForPackageUid, stopped: " + serviceRecordObject);
            }
        }
    }

    private void stopServiceLocked(Object serviceRecordObj) {

        if (XposedLog.isVerboseLoggable()){
            // Dump all methods.
            Class c = getHost().getClass();
            for (Method m : c.getDeclaredMethods()){
                XposedLog.verbose("DUMP ActiveServices method: " + m);
            }
        }

        invokeMethod("stopServiceLocked", serviceRecordObj);
    }

    class ServiceMapProxy extends InvokeTargetProxy<Object> {

        ServiceMapProxy(Object host) {
            super(host);
        }

        // Checked N M
        android.util.ArrayMap getMServicesByNameField() {
            try {
                return (ArrayMap) XposedHelpers.getObjectField(getHost(), "mServicesByName");
            } catch (Exception e) {
                XposedLog.wtf("ActiveServicesProxy Fail getMServicesByNameField: " + Log.getStackTraceString(e));
                return null;
            }
        }

        // Checked N M
        void ensureNotStartingBackground(Object serviceRecordObj) {
            invokeMethod("ensureNotStartingBackground", serviceRecordObj);
        }
    }

    public interface StopServiceConfirm {
        boolean confirmToStop(ServiceRecordProxy proxy);
    }
}
