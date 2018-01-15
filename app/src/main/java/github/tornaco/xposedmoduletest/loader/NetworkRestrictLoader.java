package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.io.File;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.model.NetworkRestrictionItem;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface NetworkRestrictLoader {

    @NonNull
    List<NetworkRestrictionItem> loadAll(boolean showSystemApp);


    class Impl implements NetworkRestrictLoader {

        public static NetworkRestrictLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }


        @NonNull
        @Override
        public List<NetworkRestrictionItem> loadAll(boolean showSystemApp) {

            List<NetworkRestrictionItem> restrictionItems = Lists.newArrayList();

            XAshmanManager ash = XAshmanManager.get();
            if (!ash.isServiceAvailable()) return restrictionItems;

            PackageManager pm = this.context.getPackageManager();

            List<android.content.pm.PackageInfo> packages;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                packages = pm.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            }

            for (android.content.pm.PackageInfo packageInfo : packages) {
                String name = packageInfo.applicationInfo.loadLabel(pm).toString();
                if (TextUtils.isEmpty(name)) {
                    name = packageInfo.packageName;
                }

                if ("android.uid.system".equals(packageInfo.sharedUserId)) {
                    continue;
                }

                // Check if app file exists.
                String appPath = PkgUtil.pathOf(context, packageInfo.packageName);
                if (appPath == null) continue;
                File f = new File(appPath);
                if (!f.exists()) continue;

                boolean isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (isSystemApp && !showSystemApp) continue;

                int uid = PkgUtil.uidForPkg(context, packageInfo.packageName);

                if (uid == 1000) continue;// This is core system uid, ignore.

                NetworkRestrictionItem item = new NetworkRestrictionItem();
                item.setSystemApp(isSystemApp);
                item.setAppName(name);
                item.setPackageName(packageInfo.packageName);
                item.setRestrictedData(ash.isRestrictOnData(uid));
                item.setRestrictedWifi(ash.isRestrictOnWifi(uid));
                item.setUid(uid);

                Logger.d("restrictionItems: " + item);
                restrictionItems.add(item);
            }

            java.util.Collections.sort(restrictionItems, new PinyinComparator());

            return restrictionItems;
        }
    }

    class PinyinComparator implements Comparator<NetworkRestrictionItem> {

        public int compare(NetworkRestrictionItem o1, NetworkRestrictionItem o2) {
            int disO1 = 0;
            if (o1.isRestrictedData()) {
                disO1++;
            }
            if (o1.isRestrictedWifi()) {
                disO1++;
            }
            int disO2 = 0;
            if (o2.isRestrictedData()) {
                disO2++;
            }
            if (o2.isRestrictedWifi()) {
                disO2++;
            }
            if (disO1 != disO2) {
                return disO1 > disO2 ? -1 : 1;
            }
            return new github.tornaco.xposedmoduletest.util.PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
