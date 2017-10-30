package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.github.stuxuhai.jpinyin.PinyinException;
import com.github.stuxuhai.jpinyin.PinyinHelper;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.xposedmoduletest.bean.PackageInfo;
import github.tornaco.xposedmoduletest.x.XAppGuardManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface PackageLoader {

    @NonNull
    List<PackageInfo> loadInstalled(boolean showSystem);

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
        public List<PackageInfo> loadInstalled(boolean showSystem) {

            List<PackageInfo> guards = loadStoredGuarded();

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
                if (isSystemApp && !showSystem) continue;

                PackageInfo p = new PackageInfo();
                p.setGuard(false);
                p.setAppName(name);
                p.setPkgName(packageInfo.packageName);

                if (!guards.contains(p)) out.add(p);
            }
            java.util.Collections.sort(out, new PinyinComparator());

            return out;
        }

        @NonNull
        @Override
        public List<PackageInfo> loadStored() {
            final PackageManager pm = this.context.getPackageManager();
            final List<PackageInfo> out = new ArrayList<>();
            Collections.consumeRemaining(XAppGuardManager.from().getPackages(), new Consumer<String>() {
                @Override
                public void accept(String s) {
                    PackageInfo p = new PackageInfo();
                    p.setPkgName(s);
                    p.setAppName(String.valueOf(ApkUtil.loadNameByPkgName(context, s)));
                    if (TextUtils.isEmpty(p.getAppName())) return;
                    out.add(p);
                    java.util.Collections.sort(out, new PinyinComparator());
                }
            });
            return out;
        }

        @NonNull
        @Override
        public List<PackageInfo> loadStoredGuarded() {
            return loadStored();
        }
    }

    class PinyinComparator implements Comparator<PackageInfo> {
        public int compare(PackageInfo o1, PackageInfo o2) {
            int flag = 0;
            try {
                String str1 = PinyinHelper.getShortPinyin(String.valueOf(o1.getAppName()));
                String str2 = PinyinHelper.getShortPinyin(String.valueOf(o2.getAppName()));
                flag = str1.compareTo(str2);
            } catch (PinyinException ignored) {
            }

            return flag;
        }
    }
}
