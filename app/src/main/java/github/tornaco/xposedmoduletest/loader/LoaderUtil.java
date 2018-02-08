package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public class LoaderUtil {

    public static CommonPackageInfo constructCommonPackageInfo(Context context, String pkg) {
        if (!PkgUtil.isPkgInstalled(context, pkg)) return null;
        String name = String.valueOf(PkgUtil.loadNameByPkgName(context, pkg));
        CommonPackageInfo p = new CommonPackageInfo();
        p.setAppName(name);
        p.setPkgName(pkg);
        p.setInstalledTime(PkgUtil.loadInstalledTimeByPkgName(context, pkg));
        p.setAppLevel(XAshmanManager.get().getAppLevel(pkg));
        p.setSystemApp(PkgUtil.isSystemApp(context, pkg));

        if (XAshmanManager.get().isServiceAvailable()) {
            int state = XAshmanManager.get().getApplicationEnabledSetting(pkg);
            boolean disabled = state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    && state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
            p.setDisabled(disabled);
        }
        return p;
    }

    public static void stateSort(List<CommonPackageInfo> commonPackageInfos) {
        final PinyinComparator pinyinComparator = new PinyinComparator();

        Collections.sort(commonPackageInfos, new Comparator<CommonPackageInfo>() {
            @Override
            public int compare(CommonPackageInfo o1, CommonPackageInfo o2) {
                // Check if this app is disabled.
                boolean o1D = o1.isDisabled();
                boolean o2D = o2.isDisabled();
                if (o1D != o2D) {
                    return o1D ? -1 : 1;
                }

                boolean o1S = o1.isSystemApp();
                boolean o2S = o2.isSystemApp();
                if (o1S != o2S) {
                    return o1S ? 1 : -1;
                }
                return pinyinComparator.compare(o1.getAppName(), o2.getAppName());
            }
        });
    }

    public static void opSort(List<CommonPackageInfo> commonPackageInfos) {
        final PinyinComparator pinyinComparator = new PinyinComparator();

        Collections.sort(commonPackageInfos, new Comparator<CommonPackageInfo>() {
            @Override
            public int compare(CommonPackageInfo o1, CommonPackageInfo o2) {

                int o1O = o1.getOpDistance();
                int o2O = o2.getOpDistance();
                if (o1O != o2O) {
                    return o1O > o2O ? 1 : -1;
                }

                boolean o1S = o1.isSystemApp();
                boolean o2S = o2.isSystemApp();
                if (o1S != o2S) {
                    return o1S ? 1 : -1;
                }
                return pinyinComparator.compare(o1.getAppName(), o2.getAppName());
            }
        });
    }

    public static void commonSort(List<CommonPackageInfo> commonPackageInfos) {
        final PinyinComparator pinyinComparator = new PinyinComparator();

        Collections.sort(commonPackageInfos, new Comparator<CommonPackageInfo>() {
            @Override
            public int compare(CommonPackageInfo o1, CommonPackageInfo o2) {
                boolean o1S = o1.isSystemApp();
                boolean o2S = o2.isSystemApp();
                if (o1S != o2S) {
                    return o1S ? 1 : -1;
                }
                return pinyinComparator.compare(o1.getAppName(), o2.getAppName());
            }
        });
    }
}
