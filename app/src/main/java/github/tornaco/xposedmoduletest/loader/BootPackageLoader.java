package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.bean.BootCompletePackage;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface BootPackageLoader {

    @NonNull
    List<BootCompletePackage> loadInstalled(boolean showSystem);

    @NonNull
    List<BootCompletePackage> loadStored();

    @NonNull
    List<BootCompletePackage> loadStoredDisAllowed();

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
        public List<BootCompletePackage> loadInstalled(boolean showSystem) {

            List<BootCompletePackage> guards = loadStoredDisAllowed();

            List<BootCompletePackage> out = new ArrayList<>();
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

                // Ignore our self.
                if (this.context.getPackageName().equals(packageInfo.packageName)) {
                    continue;
                }

                boolean isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (isSystemApp && !showSystem) continue;

                BootCompletePackage p = new BootCompletePackage();
                p.setAllow(true);
                p.setAppName(name);
                p.setPkgName(packageInfo.packageName);

                if (!guards.contains(p)) out.add(p);
            }
            java.util.Collections.sort(out, new PinyinComparator());

            return out;
        }

        @NonNull
        @Override
        public List<BootCompletePackage> loadStored() {
            final List<BootCompletePackage> out = new ArrayList<>();
            DaoSession daoSession = DaoManager.getInstance().getSession(context);
            if (daoSession == null)
                return out;
            List<BootCompletePackage> all = daoSession.getBootCompletePackageDao().loadAll();
            return all == null ? out : all;
        }

        @NonNull
        @Override
        public List<BootCompletePackage> loadStoredDisAllowed() {
            return loadStored();
        }
    }

    class PinyinComparator implements Comparator<BootCompletePackage> {
        public int compare(BootCompletePackage o1, BootCompletePackage o2) {
            return 1;
        }
    }
}
