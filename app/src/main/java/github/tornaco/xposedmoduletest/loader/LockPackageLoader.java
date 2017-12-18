package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.app.XAppGuardManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface LockPackageLoader {

    @NonNull
    List<CommonPackageInfo> loadInstalled(boolean showSystem, boolean locked);

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
        public List<CommonPackageInfo> loadInstalled(boolean showSystem, boolean locked) {
            XAppGuardManager appGuardManager = XAppGuardManager.get();
            List<CommonPackageInfo> res = new ArrayList<>();
            String[] lockedPkgArr = appGuardManager.getLockApps(locked);
            for (String p : lockedPkgArr) {
                CommonPackageInfo packageInfo = LoaderUtil.constructCommonPackageInfo(context, p);
                if (packageInfo == null) continue;
                res.add(packageInfo);
            }
            return res;
        }
    }

}
