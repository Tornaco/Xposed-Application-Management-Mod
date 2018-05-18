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

public interface LockPackageLoader {

    @NonNull
    List<CommonPackageInfo> loadInstalled(int filterOption, boolean locked);

    class Impl implements LockPackageLoader {

        public static LockPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadInstalled(int filterOption, boolean locked) {
            XAppLockManager appGuardManager = XAppLockManager.get();
            List<CommonPackageInfo> res = new ArrayList<>();
            String[] lockedPkgArr = appGuardManager.getLockApps(locked);
            for (String p : lockedPkgArr) {
                CommonPackageInfo packageInfo = LoaderUtil.constructCommonPackageInfo(context, p);
                if (packageInfo == null) continue;
                boolean match = filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS && !packageInfo.isSystemApp())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS && packageInfo.isSystemApp());
                if (match) {
                    res.add(packageInfo);
                }
            }
            LoaderUtil.commonSort(res);
            return res;
        }
    }

}
