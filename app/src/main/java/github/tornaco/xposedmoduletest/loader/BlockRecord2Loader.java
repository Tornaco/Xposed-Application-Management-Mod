package github.tornaco.xposedmoduletest.loader;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
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
            if (XAshmanManager.get().isServiceAvailable()) {
                List<BlockRecord2> all = XAshmanManager.get().getBlockRecords();

                java.util.Collections.sort(all, new Comparator<BlockRecord2>() {
                    @Override
                    public int compare(BlockRecord2 o1, BlockRecord2 o2) {
                        return o1.getTimeWhen() > o2.getTimeWhen() ? -1 : 1;
                    }
                });

                return all;
            }
            return new ArrayList<>();
        }
    }
}
