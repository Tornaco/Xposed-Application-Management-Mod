package github.tornaco.xposedmoduletest.loader;

import android.content.Context;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

class LoaderUtil {

    static CommonPackageInfo constructCommonPackageInfo(Context context, String pkg) {
        if (!PkgUtil.isPkgInstalled(context, pkg)) return null;
        String name = String.valueOf(PkgUtil.loadNameByPkgName(context, pkg));
        CommonPackageInfo p = new CommonPackageInfo();
        p.setAppName(name);
        p.setPkgName(pkg);
        p.setSystemApp(PkgUtil.isSystemApp(context, pkg));
        return p;
    }
}
