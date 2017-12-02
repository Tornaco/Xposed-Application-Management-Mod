package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.bean.LockKillPackage;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface LockKillPackageLoader {

    @NonNull
    List<LockKillPackage> loadInstalled(boolean blocked);

    class Impl implements LockKillPackageLoader {

        public static LockKillPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<LockKillPackage> loadInstalled(boolean willBeKill) {

            List<LockKillPackage> out = new ArrayList<>();

            XAshmanManager xAshmanManager = XAshmanManager.singleInstance();
            if (!xAshmanManager.isServiceAvailable()) return out;

            String[] packages = xAshmanManager.getLKApps(willBeKill);

            for (String pkg : packages) {
                String name = String.valueOf(PkgUtil.loadNameByPkgName(context, pkg));
                if (!TextUtils.isEmpty(name)) {
                    name = name.replace(" ", "");
                }

                LockKillPackage p = new LockKillPackage();
                p.setKill(true);
                p.setAppName(name);
                p.setPkgName(pkg);
                p.setSystemApp(PkgUtil.isSystemApp(context, pkg));

                out.add(p);
            }
            java.util.Collections.sort(out, new LockComparator());

            return out;
        }
    }

    class LockComparator implements Comparator<LockKillPackage> {
        public int compare(LockKillPackage o1, LockKillPackage o2) {
            return new PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
