package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface BlurPackageLoader {

    @NonNull
    List<CommonPackageInfo> loadInstalled(int filterOption, boolean blur);

    class Impl implements BlurPackageLoader {

        public static BlurPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadInstalled(int filterOption, boolean blur) {

            List<CommonPackageInfo> out = new ArrayList<>();

            XAppLockManager appGuardManager = XAppLockManager.get();
            if (!appGuardManager.isServiceAvailable()) return out;

            String[] packages = appGuardManager.getBlurApps(blur);

            for (String pkg : packages) {
                CommonPackageInfo p = LoaderUtil.constructCommonPackageInfo(context, pkg);
                if (p == null) continue;
                boolean match = filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS && !p.isSystemApp())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS && p.isSystemApp());

                if (match) out.add(p);
            }

            LoaderUtil.commonSort(out);

            return out;
        }
    }
}
