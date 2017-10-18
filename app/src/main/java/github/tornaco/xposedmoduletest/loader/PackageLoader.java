package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.bean.PackageInfo;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface PackageLoader {
    @NonNull
    List<PackageInfo> loadInstalled();

    @NonNull
    List<PackageInfo> loadStored();

    @NonNull
    List<PackageInfo> loadStoredGuarded();

    class Impl implements PackageLoader {

        public static PackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<PackageInfo> loadInstalled() {
            List<PackageInfo> out = new ArrayList<>();
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

                PackageInfo p = new PackageInfo();
                p.setGuard(false);
                p.setAppName(name);
                p.setPkgName(packageInfo.packageName);

                out.add(p);
            }

            return out;
        }

        @NonNull
        @Override
        public List<PackageInfo> loadStored() {
            DaoSession daoSession = DaoManager.getInstance().getSession(context);
            if (daoSession == null) return new ArrayList<>(0);
            return daoSession.getPackageInfoDao().loadAll();
        }

        @NonNull
        @Override
        public List<PackageInfo> loadStoredGuarded() {
            final List<PackageInfo> out = new ArrayList<>();
            Collections.consumeRemaining(loadStored(), new Consumer<PackageInfo>() {
                @Override
                public void accept(PackageInfo packageInfo) {
                    if (packageInfo.getGuard()) out.add(packageInfo);
                }
            });
            return out;
        }
    }
}
