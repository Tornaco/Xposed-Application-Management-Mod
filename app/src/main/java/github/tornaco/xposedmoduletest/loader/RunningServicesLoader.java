package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.xposed.bean.RunningServiceInfoCompat;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface RunningServicesLoader {

    @NonNull
    List<RunningServiceInfoCompat> loadServices(boolean showCached);

    class Impl implements RunningServicesLoader {

        public static RunningServicesLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<RunningServiceInfoCompat> loadServices(boolean showCached) {
//            List<RunningServiceInfoCompat> runningServiceInfoCompats = XAshmanManager.get()
//                    .getRunningServices(100);
//            if (Collections.isNullOrEmpty(runningServiceInfoCompats)) return new ArrayList<>(0);
//            Collections.consumeRemaining(runningServiceInfoCompats, new Consumer<RunningServiceInfoCompat>() {
//                @Override
//                public void accept(RunningServiceInfoCompat runningServiceInfoCompat) {
//                    Logger.d("loadServices: " + runningServiceInfoCompat);
//                    String packageName = runningServiceInfoCompat.service.getPackageName();
//                    String name = String.valueOf(PkgUtil.loadNameByPkgName(context, packageName));
//                    runningServiceInfoCompat.setAppName(name);
//                    runningServiceInfoCompat.setPkgName(packageName);
//                    runningServiceInfoCompat.setSystemApp(PkgUtil.isSystemApp(context, packageName));
//                }
//            });
//            return runningServiceInfoCompats;
            return new ArrayList<>();
        }
    }

}
