package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface UPPackageLoader {

    @NonNull
    List<CommonPackageInfo> loadInstalled(boolean up);

    class Impl implements UPPackageLoader {

        public static UPPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadInstalled(boolean up) {

            List<CommonPackageInfo> out = new ArrayList<>();

            XAppLockManager appGuardManager = XAppLockManager.get();
            if (!appGuardManager.isServiceAvailable()) return out;

            String[] packages = appGuardManager.getUPApps(up);

            for (String pkg : packages) {
                CommonPackageInfo p = LoaderUtil.constructCommonPackageInfo(context, pkg);
                if (p != null) out.add(p);
            }

            LoaderUtil.commonSort(out);

            return out;
        }
    }
}
