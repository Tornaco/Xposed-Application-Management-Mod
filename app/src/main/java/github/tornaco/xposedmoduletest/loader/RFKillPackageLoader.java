package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.bean.RFKillPackage;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface RFKillPackageLoader {

    @NonNull
    List<RFKillPackage> loadInstalled(boolean blocked);

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
        public List<RFKillPackage> loadInstalled(boolean willBeKill) {

            List<RFKillPackage> out = new ArrayList<>();

            XAshmanManager xAshmanManager = XAshmanManager.singleInstance();
            if (!xAshmanManager.isServiceAvailable()) return out;

            List<String> whitelist = xAshmanManager.getWhiteListPackages();

            PackageManager pm = this.context.getPackageManager();
            List<android.content.pm.PackageInfo> packages;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                packages = pm.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            }

            for (android.content.pm.PackageInfo packageInfo : packages) {
                String name = packageInfo.applicationInfo.loadLabel(pm).toString();
                if (!TextUtils.isEmpty(name)) {
                    name = name.replace(" ", "");
                } else {
                    Logger.w("Ignored app with empty name:%s", packageInfo);
                    continue;
                }

                String packageName = packageInfo.packageName;

                if (whitelist.contains(packageName)) continue;

                // Ignore that will not be killed.
                if (willBeKill != xAshmanManager.isPackageRFKillEnabled(packageInfo.packageName)) {
                    continue;
                }

                RFKillPackage p = new RFKillPackage();
                p.setKill(true);
                p.setAppName(name);
                p.setPkgName(packageName);

                out.add(p);
            }
            java.util.Collections.sort(out, new RFComparator());

            return out;
        }
    }

    class RFComparator implements Comparator<RFKillPackage> {
        public int compare(RFKillPackage o1, RFKillPackage o2) {
            return new PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
