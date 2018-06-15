package github.tornaco.xposedmoduletest.xposed.repo;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.AtomicFile;
import android.util.Log;

import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;

import github.tornaco.xposedmoduletest.xposed.util.Closer;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/12/28.
 * Email: Tornaco@163.com
 */

public class StringMapRepo2 implements MapRepo<String, String> {

    private static final String NULL_INDICATOR = "NULL";
    private static final String TOKEN = "TOKEN-990232389453584331321-TOKEN";
    private static final String TOKEN_OLD = "-";

    private final Map<String, String> mStorage = new HashMap<>();

    // Flush data too many times, may drain battery.
    private static final int FLUSH_DELAY = 5000;
    private static final int FLUSH_DELAY_FAST = 100;

    private Handler mHandler;
    private ExecutorService mExe;

    private AtomicFile mFile;

    private final Object sync = new Object();

    StringMapRepo2(File file, Handler handler, ExecutorService service) {
        this.mFile = new AtomicFile(file);
        this.mExe = service;
        this.mHandler = handler;

        if (!this.mFile.getBaseFile().exists()) {
            try {
                Files.createParentDirs(file);
            } catch (IOException e) {
                XposedLog.wtf("Fail createParentDirs for: " + file + "\n" + Log.getStackTraceString(e));
            }
        }

        XposedLog.debug("StringMapRepo2: " + name() + ", comes up");

        reload();
    }

    @Override
    public void reload() {
        synchronized (sync) {
            try {

                if (!mFile.getBaseFile().exists()) {
                    XposedLog.wtf("getBaseFile not exists, skip load: " + name());
                    return;
                }

                if (mFile.getBaseFile().isDirectory()) {
                    XposedLog.wtf("getBaseFile isDirectory, clean up: " + name());
                    FileUtil.deleteDirQuiet(mFile.getBaseFile());
                    mFile.delete();
                }

                // A-JSON
                // B-JSON
                // C-JSON
                Map<String, String> m = new HashMap<>();

                InputStreamReader fr = new InputStreamReader(mFile.openRead());
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    // XposedLog.verbose("Read of line: " + line);
                    StringTokenizer t = new StringTokenizer(line, TOKEN);
                    int c = t.countTokens();
                    if (c != 2) {
                        // Try parse with old one.
                        t = new StringTokenizer(line, TOKEN_OLD);
                        c = t.countTokens();
                        if (c != 2) {
                            XposedLog.wtf("Found invalid line: " + line);
                            continue;
                        }
                    }
                    String key = t.nextToken();
                    if (key == null || key.trim().length() == 0) {
                        XposedLog.wtf("Found invalid key@line: " + line);
                        continue;
                    }
                    String value = t.nextToken();
                    if (value == null || value.trim().length() == 0) {
                        XposedLog.wtf("Found invalid value@line: " + line);
                        continue;
                    }
                    m.put(key, value);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);

                mStorage.putAll(m);

            } catch (IOException e) {
                XposedLog.wtf("Fail reload@IOException: " + mFile + "\n" + Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void reloadAsync() {
        Runnable r = this::reload;
        if (mExe == null) {
            new Thread(r).start();
        } else {
            mExe.execute(r);
        }
    }

    @Override
    public void flush() {
        XposedLog.verbose("flush");
        synchronized (sync) {
            try {

                // A TOKEN JSON
                // B TOKEN JSON
                // C TOKEN JSON
                Map<String, String> m = new HashMap<>(mStorage);

                FileOutputStream fos = mFile.startWrite();
                PrintWriter printWriter = new PrintWriter(fos);

                for (String key : m.keySet()) {
                    String value = m.get(key);
                    // Use NULL_INDICATOR to indicate null value.
                    printWriter.println(key + TOKEN + (value == null ? NULL_INDICATOR : value));
                }

                printWriter.flush();
                mFile.finishWrite(fos);
                Closer.closeQuietly(printWriter);

            } catch (IOException e) {
                XposedLog.wtf("Fail flush@IOException: " + mFile + "\n" + Log.getStackTraceString(e));
            }
        }
    }

    private Runnable mFlusher = this::flush;

    private Runnable mFlushCaller = this::flushAsync;

    @Override
    public void flushAsync() {
        XposedLog.verbose("flush async");
        if (mExe == null) {
            new Thread(mFlusher).start();
        } else {
            mExe.execute(mFlusher);
        }
    }

    @Override
    public String name() {
        return Files.getNameWithoutExtension(mFile.getBaseFile().getPath());
    }

    @Override
    public Map<String, String> dup() {
        return new HashMap<>(mStorage);
    }

    @Override
    public boolean hasNoneNullValue(String s) {
        String v = mStorage.get(s);
        return v != null && (!NULL_INDICATOR.equals(v));
    }

    @Override
    public int size() {
        return mStorage.size();
    }

    @Override
    public boolean isEmpty() {
        return mStorage.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return mStorage.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return mStorage.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return mStorage.get(key);
    }

    @Override
    public String put(String key, String value) {
        String res = mStorage.put(key, value == null ? NULL_INDICATOR : value);
        mHandler.removeCallbacks(mFlushCaller);
        mHandler.postDelayed(mFlushCaller, FLUSH_DELAY);
        return res;
    }

    @Override
    public String remove(Object key) {
        String res = mStorage.remove(key);
        mHandler.removeCallbacks(mFlushCaller);
        mHandler.postDelayed(mFlushCaller, FLUSH_DELAY);
        return res;
    }

    @Override
    public void putAll(@NonNull Map<? extends String, ? extends String> m) {
        mStorage.putAll(m);
        mHandler.removeCallbacks(mFlushCaller);
        mHandler.postDelayed(mFlushCaller, FLUSH_DELAY);
    }

    @Override
    public void clear() {
        mStorage.clear();
        mHandler.removeCallbacks(mFlushCaller);
        mHandler.postDelayed(mFlushCaller, FLUSH_DELAY_FAST);
    }

    @NonNull
    @Override
    public Set<String> keySet() {
        return new HashSet<>(mStorage.keySet());
    }

    @NonNull
    @Override
    public Collection<String> values() {
        return mStorage.values();
    }

    @NonNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return new HashSet<>(mStorage.entrySet());
    }
}
