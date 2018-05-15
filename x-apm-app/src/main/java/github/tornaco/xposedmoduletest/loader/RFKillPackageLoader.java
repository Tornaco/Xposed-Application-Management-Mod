package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface RFKillPackageLoader {

    @NonNull
    List<CommonPackageInfo> loadInstalled(int filterOption, boolean blocked);

    class Impl implements RFKillPackageLoader {

        public static RFKillPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadInstalled(int filterOption, boolean willBeKill) {

            List<CommonPackageInfo> out = new ArrayList<>();

            XAPMManager xAshmanManager = XAPMManager.get();
            if (!xAshmanManager.isServiceAvailable()) return out;

            String[] packages = xAshmanManager.getRFKApps(willBeKill);

            for (String pkg : packages) {
                CommonPackageInfo p = LoaderUtil.constructCommonPackageInfo(context, pkg);
                if (p == null) continue;
                boolean match = filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS && !p.isSystemApp())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS && p.isSystemApp());

                if (match) out.add(p);
            }
            java.util.Collections.sort(out, new RFComparator());

            return out;
        }
    }

    class RFComparator implements Comparator<CommonPackageInfo> {
        public int compare(CommonPackageInfo o1, CommonPackageInfo o2) {
            return new PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
