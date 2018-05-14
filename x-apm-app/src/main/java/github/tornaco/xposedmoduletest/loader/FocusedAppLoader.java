package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface FocusedAppLoader {

    @NonNull
    List<CommonPackageInfo> load();

    class Impl implements FocusedAppLoader {

        public static FocusedAppLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> load() {
            List<CommonPackageInfo> out = new ArrayList<>();

            XAshmanManager xAshmanManager = XAshmanManager.get();
            if (!xAshmanManager.isServiceAvailable()) return out;

            String[] packages = xAshmanManager.getAppFocusActionPackages();

            for (String pkg : packages) {
                if (!PkgUtil.isPkgInstalled(context, pkg)) {
                    Logger.d("Package not installed: " + pkg);
                    continue;
                }
                String name = String.valueOf(PkgUtil.loadNameByPkgName(context, pkg));
                CommonPackageInfo p = new CommonPackageInfo();
                p.setAppName(name);
                p.setPkgName(pkg);
                p.setPayload(xAshmanManager.getAppFocusActions(pkg));
                out.add(p);
            }

            LoaderUtil.commonSort(out);

            return out;
        }
    }
}
