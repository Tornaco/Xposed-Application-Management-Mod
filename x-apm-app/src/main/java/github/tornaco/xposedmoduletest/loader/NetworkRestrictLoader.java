package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.model.NetworkRestrictionItem;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface NetworkRestrictLoader {

    @NonNull
    List<NetworkRestrictionItem> loadAll(int filterOption, boolean showSystemApp);


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
        public List<NetworkRestrictionItem> loadAll(int filterOption, boolean showSystemApp) {

            List<NetworkRestrictionItem> restrictionItems = Lists.newArrayList();

            XAPMManager ash = XAPMManager.get();
            if (!ash.isServiceAvailable()) return restrictionItems;

            PackageManager pm = this.context.getPackageManager();

            List<android.content.pm.PackageInfo> packages;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                packages = pm.getInstalledPackages(PackageManager.MATCH_UNINSTALLED_PACKAGES);
            } else {
                packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            }

            for (android.content.pm.PackageInfo packageInfo : packages) {

                String name = String.valueOf(PkgUtil.loadNameByPkgName(context, packageInfo.packageName));

                boolean isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                if (isSystemApp && !showSystemApp) continue;

                int uid = PkgUtil.uidForPkg(context, packageInfo.packageName);

                NetworkRestrictionItem item = new NetworkRestrictionItem();
                item.setSystemApp(isSystemApp);


                boolean match = filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS && !item.isSystemApp())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS && item.isSystemApp());
                if (!match) continue;

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
