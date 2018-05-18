package github.tornaco.xposedmoduletest.loader;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface BlockRecord2Loader {

    @NonNull
    List<BlockRecord2> loadAll(String pkg);

    class Impl implements BlockRecord2Loader {

        public static BlockRecord2Loader create() {
            return new Impl();
        }

        @NonNull
        @Override
        public List<BlockRecord2> loadAll(String pkg) {
            if (XAPMManager.get().isServiceAvailable()) {
                List<BlockRecord2> res = pkg == null
                        ? XAPMManager.get().getBlockRecords()
                        : XAPMManager.get().getStartRecordsForPackage(pkg);

                java.util.Collections.sort(res, (o1, o2) -> o1.getTimeWhen() > o2.getTimeWhen() ? -1 : 1);

                return res;
            }
            return new ArrayList<>();
        }
    }
}
