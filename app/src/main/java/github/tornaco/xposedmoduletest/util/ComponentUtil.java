package github.tornaco.xposedmoduletest.util;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;

import com.google.common.collect.Lists;

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
            PackageInfo packageInfo = pm.getPackageInfo(pkg, GET_SERVICES);
            if (packageInfo == null) return Lists.newArrayListWithCapacity(0);
            ServiceInfo serviceInfoArray[] = packageInfo.services;
            if (serviceInfoArray == null || serviceInfoArray.length == 0)
                return Lists.newArrayListWithCapacity(0);
            return Lists.newArrayList(serviceInfoArray);
        } catch (PackageManager.NameNotFoundException e) {
            return Lists.newArrayListWithCapacity(0);
        }
    }

    public static List<ActivityInfo> getBroadcasts(Context context, String pkg) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(pkg, GET_RECEIVERS);
            if (packageInfo == null) return Lists.newArrayListWithCapacity(0);
            ActivityInfo[] activityInfos = packageInfo.receivers;
            if (activityInfos == null || activityInfos.length == 0)
                return Lists.newArrayListWithCapacity(0);
            return Lists.newArrayList(activityInfos);
        } catch (PackageManager.NameNotFoundException e) {
            return Lists.newArrayListWithCapacity(0);
        }
    }
}
