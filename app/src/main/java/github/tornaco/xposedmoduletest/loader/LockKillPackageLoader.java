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

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.bean.LockKillPackage;
import github.tornaco.xposedmoduletest.x.util.PkgUtil;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface LockKillPackageLoader {

    @NonNull
    List<LockKillPackage> loadInstalled(boolean showSystem);

    @NonNull
    List<LockKillPackage> loadStored();

    @NonNull
    List<LockKillPackage> loadStoredKilled();

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
        public List<LockKillPackage> loadInstalled(boolean showSystem) {

            List<LockKillPackage> guards = loadStoredKilled();

            List<LockKillPackage> out = new ArrayList<>();
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

                LockKillPackage p = new LockKillPackage();
                p.setKill(false);
                p.setAppName(name);
                p.setPkgName(packageInfo.packageName);

                if (!guards.contains(p)) out.add(p);
            }
            java.util.Collections.sort(out, new PinyinComparator());

            return out;
        }

        @NonNull
        @Override
        public List<LockKillPackage> loadStored() {
            final List<LockKillPackage> out = new ArrayList<>();
            DaoSession daoSession = DaoManager.getInstance().getSession(context);
            if (daoSession == null)
                return out;
            List<LockKillPackage> all = daoSession.getLockKillPackageDao().loadAll();
            if (all != null) {
                github.tornaco.android.common.Collections.consumeRemaining(all,
                        new Consumer<LockKillPackage>() {
                            @Override
                            public void accept(LockKillPackage LockKillPackage) {
                                if (PkgUtil.isPkgInstalled(context, LockKillPackage.getPkgName())) {
                                    out.add(LockKillPackage);
                                }
                            }
                        });
            }
            return out;
        }

        @NonNull
        @Override
        public List<LockKillPackage> loadStoredKilled() {
            return loadStored();
        }
    }

    class PinyinComparator implements Comparator<LockKillPackage> {
        public int compare(LockKillPackage o1, LockKillPackage o2) {
            return 1;
        }
    }
}
