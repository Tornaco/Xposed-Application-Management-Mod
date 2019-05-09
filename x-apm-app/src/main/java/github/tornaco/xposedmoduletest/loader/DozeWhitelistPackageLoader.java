package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.collect.Sets;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.util.ArrayUtil;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface DozeWhitelistPackageLoader {

    @NonNull
    List<CommonPackageInfo> loadWhiteList(boolean white);

    class Impl implements DozeWhitelistPackageLoader {

        public static DozeWhitelistPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadWhiteList(boolean white) {
            List<CommonPackageInfo> out = new ArrayList<>();

            XAPMManager xAshmanManager = XAPMManager.get();
            if (!xAshmanManager.isServiceAvailable()) return out;

            if (BuildConfig.DEBUG) {
                Logger.e(Arrays.toString(xAshmanManager.getFullPowerWhitelist()));
            }

            String[] system = xAshmanManager.getSystemPowerWhitelist();
            Set<String> systemSet = Sets.newHashSet(system);

            String[] packages = ArrayUtil.combine(xAshmanManager.getUserPowerWhitelist(), system);

            if (packages == null) return out;

            if (white) {
                for (String pkg : packages) {
                    CommonPackageInfo p = LoaderUtil.constructCommonPackageInfo(context, pkg);
                    if (p != null) {
                        if (systemSet.contains(pkg)) {
                            p.setPayload(new String[]{"SYSTEM"});
                        }
                        out.add(p);
                    }
                }
            } else {
                Set<String> whiteSet = Sets.newHashSet(packages);
                String[] allPackages = xAshmanManager.getInstalledApps(XAPMManager.FLAG_SHOW_SYSTEM_APP);
                for (String pkg : allPackages) {
                    boolean whitelisted = whiteSet.contains(pkg);
                    if (!whitelisted) {
                        CommonPackageInfo p = LoaderUtil.constructCommonPackageInfo(context, pkg);
                        if (p != null) {
                            out.add(p);
                        }
                    }
                }
            }

            java.util.Collections.sort(out, new BootComparator());

            return out;
        }
    }

    class BootComparator implements Comparator<CommonPackageInfo> {
        public int compare(CommonPackageInfo o1, CommonPackageInfo o2) {
            return new PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
