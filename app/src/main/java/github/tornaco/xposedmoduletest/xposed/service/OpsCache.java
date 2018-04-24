package github.tornaco.xposedmoduletest.xposed.service;

import android.os.RemoteException;
import android.util.Singleton;
import android.util.SparseArray;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.bean.OpLog;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2018/1/31.
 * Email: Tornaco@163.com
 */

class OpsCache {

    private static final String TAG = "OPS_CACHE-";

    private static final int MAX_OP_LOG_ENTRY_SIZE = 1024;

    private final SparseArray<List<OpLog>> mOpCache = new SparseArray<>();

    private final Map<String, List<OpLog>> mPkgCache = new HashMap<>();

    private static final Singleton<OpsCache> sCache = new Singleton<OpsCache>() {
        @Override
        protected OpsCache create() {
            return new OpsCache();
        }
    };

    static OpsCache singleInstance() {
        return sCache.get();
    }

    private static final Object sLock = new Object();

    private OpsCache() {
    }

    // Sync.
    void logPackageOp(int code, int mode, String pkg, String[] payload) {
        synchronized (sLock) {
            logForOp(code, mode, pkg, payload);
            logForPkg(code, mode, pkg, payload);

            if (BuildConfig.DEBUG) {
                XposedLog.verbose(TAG + "logPackageOp %s %s %s", code, mode, pkg);
            }
        }
    }

    List<OpLog> getLogForPackage(String who) {
        synchronized (sLock) {
            return Lists.newArrayList(mPkgCache.get(who));
        }
    }

    List<OpLog> getLogForOp(int code) {
        synchronized (sLock) {
            return Lists.newArrayList(mOpCache.get(code));
        }
    }

    void clearOpLogForPackage(String packageName) throws RemoteException {
        synchronized (sLock) {
            mPkgCache.remove(packageName);
        }
    }

    void clearOpLogForOp(int cod) throws RemoteException {
        synchronized (sLock) {
            mOpCache.remove(cod);
        }
    }

    private void logForOp(int code, int mode, String pkg, String[] payload) {
        List<OpLog> ops = mOpCache.get(code);
        if (ops == null) {
            ops = new ArrayList<>();
        } else {
            trimListInNecessary(ops);
        }
        OpLog log = OpLog.builder()
                .when(System.currentTimeMillis())
                .packageName(pkg)
                .mode(mode)
                .code(code)
                .payload(payload == null ? new String[0] : payload)
                .build();
        ops.add(log);
        mOpCache.put(code, ops);
    }

    private void logForPkg(int code, int mode, String pkg, String[] payload) {
        List<OpLog> ops = mPkgCache.get(pkg);
        if (ops == null) {
            ops = new ArrayList<>();
        } else {
            trimListInNecessary(ops);
        }
        OpLog log = OpLog.builder()
                .when(System.currentTimeMillis())
                .packageName(pkg)
                .mode(mode)
                .code(code)
                .payload(payload == null ? new String[0] : payload)
                .build();
        ops.add(log);
        mPkgCache.put(pkg, ops);
    }

    private static void trimListInNecessary(List list) {
        if (list.size() > MAX_OP_LOG_ENTRY_SIZE) { // MAX 3, CURRENT 4, REMOVE 3.
//            list.remove(list.size() - 1);
            list.clear();
            XposedLog.verbose("OpsCache trimListInNecessary, done");
        }
    }
}
