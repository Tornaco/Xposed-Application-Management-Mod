package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.bean.BlockRecord;
import github.tornaco.xposedmoduletest.bean.BlockRecordDao;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface BlockRecordLoader {

    @NonNull
    List<BlockRecord> loadAll(String pkg);

    class Impl implements BlockRecordLoader {

        public static BlockRecordLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<BlockRecord> loadAll(String pkg) {
            final List<BlockRecord> out = new ArrayList<>();
            DaoSession daoSession = DaoManager.getInstance().getSession(context);
            if (daoSession == null)
                return out;

            List<BlockRecord> all =
                    TextUtils.isEmpty(pkg) ?
                            daoSession.getBlockRecordDao().loadAll()
                            : daoSession.getBlockRecordDao().queryRaw(
                            BlockRecordDao.Properties.PkgName.columnName + "=?", pkg);

            java.util.Collections.sort(all, new Comparator<BlockRecord>() {
                @Override
                public int compare(BlockRecord o1, BlockRecord o2) {
                    return o1.getTimeWhen() > o2.getTimeWhen() ? -1 : 1;
                }
            });
            return all;
        }
    }
}
