package github.tornaco.xposedmoduletest.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.model.ActivityInfoSettings;
import github.tornaco.xposedmoduletest.model.ActivityInfoSettingsList;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettings;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettingsList;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

import static android.content.pm.PackageManager.GET_ACTIVITIES;
import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_SERVICES;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public class ComponentUtil {

    /**
     * Those that has not service and broadcast and no activity.
     */
    public static boolean isGreenPackage(PackageInfo packageInfo) {
        return (packageInfo.receivers == null || packageInfo.receivers.length == 0)
                && (packageInfo.services == null || packageInfo.services.length == 0)
                && (packageInfo.activities == null || packageInfo.activities.length == 0);
    }

    public static List<ServiceInfo> getServices(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                packageInfo = pm.getPackageInfo(pkg, GET_SERVICES | PackageManager.MATCH_DISABLED_COMPONENTS);
            } else {
                packageInfo = pm.getPackageInfo(pkg, GET_SERVICES | PackageManager.GET_DISABLED_COMPONENTS);
            }
            if (packageInfo == null) return Lists.newArrayListWithCapacity(0);
            ServiceInfo serviceInfoArray[] = packageInfo.services;
            if (serviceInfoArray == null || serviceInfoArray.length == 0)
                return Lists.newArrayListWithCapacity(0);
            return Lists.newArrayList(serviceInfoArray);
        } catch (Exception e) {
            Logger.e("getServices: " + Logger.getStackTraceString(e));
            return Lists.newArrayListWithCapacity(0);
        }
    }

    public static List<ActivityInfo> getActivities(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                packageInfo = pm.getPackageInfo(pkg, GET_ACTIVITIES | PackageManager.MATCH_DISABLED_COMPONENTS);
            } else {
                packageInfo = pm.getPackageInfo(pkg, GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS);
            }
            if (packageInfo == null) return Lists.newArrayListWithCapacity(0);
            ActivityInfo[] activityInfos = packageInfo.activities;
            if (activityInfos == null || activityInfos.length == 0)
                return Lists.newArrayListWithCapacity(0);
            return Lists.newArrayList(activityInfos);
        } catch (Exception e) {
            Logger.e("getActivities: " + Logger.getStackTraceString(e));
            return Lists.newArrayListWithCapacity(0);
        }
    }

    public static List<ActivityInfo> getBroadcasts(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                packageInfo = pm.getPackageInfo(pkg, GET_RECEIVERS | PackageManager.MATCH_DISABLED_COMPONENTS);
            } else {
                packageInfo = pm.getPackageInfo(pkg, GET_RECEIVERS | PackageManager.GET_DISABLED_COMPONENTS);
            }
            if (packageInfo == null) return Lists.newArrayListWithCapacity(0);
            ActivityInfo[] activityInfos = packageInfo.receivers;
            if (activityInfos == null || activityInfos.length == 0)
                return Lists.newArrayListWithCapacity(0);
            return Lists.newArrayList(activityInfos);
        } catch (Exception e) {
            Logger.e("getBroadcasts: " + Logger.getStackTraceString(e));
            return Lists.newArrayListWithCapacity(0);
        }
    }

    public static ComponentName getComponentName(ActivityInfo activityInfo) {
        return new ComponentName(activityInfo.packageName, activityInfo.name);
    }

    public static ComponentName getComponentName(ServiceInfo serviceInfo) {
        return new ComponentName(serviceInfo.packageName, serviceInfo.name);
    }

    public static boolean applyBatch(Context context, ServiceInfoSettingsList serviceInfoSettingsList) {
        final XAPMManager xAshmanManager = XAPMManager.get();
        if (xAshmanManager == null) return false;
        if (serviceInfoSettingsList == null) return false;
        String pkg = serviceInfoSettingsList.getPackageName();
        int version = serviceInfoSettingsList.getVersion();
        if (version != PkgUtil.loadVersionCodeByPkgName(context, pkg)) {
            Logger.w("Different version of app config applying... for " + pkg);
        }
        List<ServiceInfoSettings.Export> exports = serviceInfoSettingsList.getExports();
        if (exports == null) return false;
        Collections.consumeRemaining(exports, new Consumer<ServiceInfoSettings.Export>() {
            @Override
            public void accept(ServiceInfoSettings.Export export) {
                ComponentName c = export.getComponentName();
                boolean allowed = export.isAllowed();
                // Apply to ASH Man.
                xAshmanManager.setComponentEnabledSetting(c,
                        allowed ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        0);
            }
        });
        return true;
    }

    public static boolean applyBatch(Context context, ActivityInfoSettingsList activityInfoSettingsList) {
        final XAPMManager xAshmanManager = XAPMManager.get();
        if (xAshmanManager == null) return false;
        if (activityInfoSettingsList == null) return false;
        String pkg = activityInfoSettingsList.getPackageName();
        int version = activityInfoSettingsList.getVersion();
        if (version != PkgUtil.loadVersionCodeByPkgName(context, pkg)) {
            Logger.w("Different version of app config applying... for " + pkg);
        }
        List<ActivityInfoSettings.Export> exports = activityInfoSettingsList.getExports();
        if (exports == null) return false;
        Collections.consumeRemaining(exports, new Consumer<ActivityInfoSettings.Export>() {
            @Override
            public void accept(ActivityInfoSettings.Export export) {
                ComponentName c = export.getComponentName();
                boolean allowed = export.isAllowed();
                // Apply to ASH Man.
                xAshmanManager.setComponentEnabledSetting(c,
                        allowed ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        0);
            }
        });
        return true;
    }
}
