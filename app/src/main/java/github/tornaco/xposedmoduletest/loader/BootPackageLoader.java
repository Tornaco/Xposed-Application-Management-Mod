package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.bean.BootCompletePackage;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface BootPackageLoader {

    @NonNull
    List<BootCompletePackage> loadInstalled(boolean blocked);

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
        public List<BootCompletePackage> loadInstalled(boolean blocked) {
            List<BootCompletePackage> out = new ArrayList<>();

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

                if (packageName.contains("com.qualcomm.qti")
                        || packageName.contains("com.qti.smq")) {
                    continue;
                }
                if (packageName.contains("com.google.android")) {
                    continue;
                }

                if (whitelist.contains(packageName)) continue;

                // Ignores that will not be blocked.
                if (blocked != xAshmanManager.isPackageBootBlockEnabled(packageName)) {
                    continue;
                }

                BootCompletePackage p = new BootCompletePackage();
                p.setAllow(false);
                p.setAppName(name);
                p.setPkgName(packageName);

                out.add(p);
            }

            java.util.Collections.sort(out, new BootComparator());

            return out;
        }
    }

    class BootComparator implements Comparator<BootCompletePackage> {
        public int compare(BootCompletePackage o1, BootCompletePackage o2) {
            return new PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
