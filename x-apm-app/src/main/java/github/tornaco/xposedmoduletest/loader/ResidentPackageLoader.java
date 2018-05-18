package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface ResidentPackageLoader {

    @NonNull
    List<CommonPackageInfo> loadInstalled(boolean redisent);

    class Impl implements ResidentPackageLoader {

        public static ResidentPackageLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadInstalled(boolean redisent) {

            List<CommonPackageInfo> out = new ArrayList<>();

            XAPMManager manager = XAPMManager.get();
            if (!manager.isServiceAvailable()) return out;

            String[] packages = manager.getResidentApps(redisent);

            for (String pkg : packages) {
                CommonPackageInfo p = LoaderUtil.constructCommonPackageInfo(context, pkg);
                if (p != null) out.add(p);
            }

            LoaderUtil.commonSort(out);

            return out;
        }
    }
}
