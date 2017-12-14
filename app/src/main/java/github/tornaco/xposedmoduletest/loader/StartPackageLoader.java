package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.bean.AutoStartPackage;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface StartPackageLoader {

    @NonNull
    List<AutoStartPackage> loadInstalled(boolean block);

    class Impl implements StartPackageLoader {

        public static StartPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<AutoStartPackage> loadInstalled(boolean block) {

            List<AutoStartPackage> out = new ArrayList<>();

            XAshmanManager xAshmanManager = XAshmanManager.get();
            if (!xAshmanManager.isServiceAvailable()) return out;

            String[] packages = xAshmanManager.getStartBlockApps(block);

            for (String pkg : packages) {
                if (!PkgUtil.isPkgInstalled(context, pkg)) continue;
                String name = String.valueOf(PkgUtil.loadNameByPkgName(context, pkg));
                if (!TextUtils.isEmpty(name)) {
                    name = name.replace(" ", "");
                }

                AutoStartPackage p = new AutoStartPackage();
                p.setAllow(false);
                p.setAppName(name);
                p.setPkgName(pkg);
                p.setSystemApp(PkgUtil.isSystemApp(context, pkg));

                out.add(p);
            }

            java.util.Collections.sort(out, new PinyinComparator());

            return out;
        }
    }

    class PinyinComparator implements Comparator<AutoStartPackage> {
        public int compare(AutoStartPackage o1, AutoStartPackage o2) {
            return new github.tornaco.xposedmoduletest.util.PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
