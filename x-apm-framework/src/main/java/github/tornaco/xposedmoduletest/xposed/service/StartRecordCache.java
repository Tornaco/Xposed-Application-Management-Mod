package github.tornaco.xposedmoduletest.xposed.service;

import android.util.Singleton;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import github.tornaco.xposedmoduletest.xposed.bean.BlockRecord2;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2018/1/31.
 * Email: Tornaco@163.com
 */

class StartRecordCache {

    private static final int MAX_OP_LOG_ENTRY_SIZE = 1024;

    private final Map<String, List<BlockRecord2>> mPkgCache = new HashMap<>();

    private static final Singleton<StartRecordCache> sCache = new Singleton<StartRecordCache>() {
        @Override
        protected StartRecordCache create() {
            return new StartRecordCache();
        }
    };

    static StartRecordCache singleInstance() {
        return sCache.get();
    }

    private static final Object sLock = new Object();

    private StartRecordCache() {
    }

    public void addStartRecordForPackage(String pkg, BlockRecord2 record) {
        synchronized (sLock) {
            List<BlockRecord2> blockRecord2s = mPkgCache.get(pkg);
            if (blockRecord2s == null) {
                blockRecord2s = new ArrayList<>();
            } else {
                trimListInNecessary(blockRecord2s);
            }
            blockRecord2s.add(record);
            mPkgCache.put(pkg, blockRecord2s);
        }
    }

    public List<BlockRecord2> getStartRecordsForPackage(String pkg) {
        synchronized (sLock) {
            if (mPkgCache.containsKey(pkg)) {
                return Lists.newArrayList(mPkgCache.get(pkg));
            }
            return new ArrayList<>(0);
        }
    }

    public void clearStartRecordsForPackage(String pkg) {
        synchronized (sLock) {
            if (mPkgCache.containsKey(pkg)) {
                List<BlockRecord2> blockRecord2s = mPkgCache.remove(pkg);
                blockRecord2s = null;
            }
        }
    }

    private static void trimListInNecessary(List list) {
        if (list.size() > MAX_OP_LOG_ENTRY_SIZE) { // MAX 3, CURRENT 4, REMOVE 3.
            list.clear();
            XposedLog.verbose("StartRecordCache trimListInNecessary, done");
        }
    }
}
