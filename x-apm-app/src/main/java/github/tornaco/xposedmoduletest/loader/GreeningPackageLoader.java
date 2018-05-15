package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface GreeningPackageLoader {

    @NonNull
    List<CommonPackageInfo> loadInstalled(boolean green);

    class Impl implements GreeningPackageLoader {

        public static GreeningPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadInstalled(boolean green) {

            List<CommonPackageInfo> out = new ArrayList<>();

            XAPMManager xAshmanManager = XAPMManager.get();
            if (!xAshmanManager.isServiceAvailable()) return out;

            String[] packages = xAshmanManager.getGreeningApps(green);

            for (String pkg : packages) {
                CommonPackageInfo p = LoaderUtil.constructCommonPackageInfo(context, pkg);
                if (p != null && !p.isDisabled()) out.add(p);
            }

            java.util.Collections.sort(out, new PinyinComparator());

            return out;
        }
    }

    class PinyinComparator implements Comparator<CommonPackageInfo> {
        public int compare(CommonPackageInfo o1, CommonPackageInfo o2) {
            return new github.tornaco.xposedmoduletest.util.PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
