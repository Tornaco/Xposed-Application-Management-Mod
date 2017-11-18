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

import static android.content.pm.PackageManager.GET_RECEIVERS;
import static android.content.pm.PackageManager.GET_SERVICES;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public class ComponentUtil {



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
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e("getServices: " + Logger.getStackTraceString(e));
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
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e("getServices: " + Logger.getStackTraceString(e));
            return Lists.newArrayListWithCapacity(0);
        }
    }

    public static ComponentName getComponentName(ActivityInfo activityInfo) {
        return new ComponentName(activityInfo.packageName, activityInfo.name);
    }

    public static ComponentName getComponentName(ServiceInfo serviceInfo) {
        return new ComponentName(serviceInfo.packageName, serviceInfo.name);
    }
}
