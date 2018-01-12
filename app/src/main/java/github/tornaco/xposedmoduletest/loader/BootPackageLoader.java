package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface BootPackageLoader {

    @NonNull
    List<CommonPackageInfo> loadInstalled(boolean blocked);

    class Impl implements BootPackageLoader {

        public static BootPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadInstalled(boolean blocked) {
            List<CommonPackageInfo> out = new ArrayList<>();

            XAshmanManager xAshmanManager = XAshmanManager.get();
            if (!xAshmanManager.isServiceAvailable()) return out;

            String[] packages = xAshmanManager.getBootBlockApps(blocked);
            Logger.d(Arrays.toString(packages));
            int length = packages.length;

            for (String pkg : packages) {
                CommonPackageInfo p = LoaderUtil.constructCommonPackageInfo(context, pkg);
                if (p != null) {
                    Logger.d("Adding: " + pkg);
                    out.add(p);
                }
            }

            int size = out.size();
            Logger.d("Adding: " + size + ", expected: " + length);

            java.util.Collections.sort(out, new BootComparator());

            return out;
        }
    }

    class BootComparator implements Comparator<CommonPackageInfo> {
        public int compare(CommonPackageInfo o1, CommonPackageInfo o2) {
            return new PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
