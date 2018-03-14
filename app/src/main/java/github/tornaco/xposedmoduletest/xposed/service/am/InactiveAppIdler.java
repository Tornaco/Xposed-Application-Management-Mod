package github.tornaco.xposedmoduletest.xposed.service.am;

import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;

/**
 * Created by Tornaco on 2018/3/14.
 */
// Only for 22+
public class InactiveAppIdler implements AppIdler {

    private UsageStatsManager mUsageStats;

    public InactiveAppIdler(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mUsageStats = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        }
    }

    @Override
    public void setAppIdle(String pkg) {
        if (mUsageStats != null) {
            mUsageStats.setAppInactive(pkg, true);
        }
    }
}
