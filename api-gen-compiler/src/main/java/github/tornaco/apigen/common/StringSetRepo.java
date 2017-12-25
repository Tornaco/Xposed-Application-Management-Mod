package github.tornaco.apigen.common;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by guohao4 on 2017/12/11.
 * Email: Tornaco@163.com
 */

public class StringSetRepo implements SetRepo<String> {

    // Flush data too many times, may drain battery.
    private static final int FLUSH_DELAY = 2000;
    private static final int FLUSH_DELAY_FAST = 100;

    private File mFile;

    public StringSetRepo(File file) {
        Preconditions.checkNotNull("File can not be null", file);
        Preconditions.checkNotNull("Handler can not be null", file);

        this.mFile = file;

        if (!this.mFile.exists()) {
            try {
                Files.createParentDirs(file);
            } catch (IOException e) {
            }
        }

        reload();
    }

    private final Set<String> mStorage = new HashSet<>();

    private final Object sync = new Object();

    @Override
    public Set<String> getAll() {
        return new HashSet<>(mStorage);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void reload() {
        synchronized (sync) {
            try {

                if (!mFile.exists()) {
                    return;
                }

                if (mFile.isDirectory()) {
                    mFile.delete();
                }

                // A
                // B
                // C
                Set h = new HashSet();

                InputStreamReader fr = new InputStreamReader(Files.asByteSource(mFile).openStream());
                BufferedReader br = new BufferedReader(fr);
                String line;
                while ((line = br.readLine()) != null) {
                    // XposedLog.verbose("Read of line: " + line);
                    h.add(line.trim());
                }

                try {
                    fr.close();
                    br.close();
                } catch (Throwable ignored) {
                }

                mStorage.addAll(h);

            } catch (IOException ignored) {
            }
        }
    }


    @Override
    public void reloadAsync() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                reload();
            }
        };
        new Thread(r).start();
    }

    @Override
    public void flush() {

    }

    private Runnable mFlusher = new Runnable() {
        @Override
        public void run() {
            flush();
        }
    };

    private Runnable mFlushCaller = new Runnable() {
        @Override
        public void run() {
            flushAsync();
        }
    };

    @Override
    public void flushAsync() {
        flush();
    }

    @Override
    public boolean add(String s) {
        if (s == null) return false;
        mStorage.add(s);
        return true;
    }

    @Override
    public boolean remove(String s) {
        if (s == null) return false;
        mStorage.remove(s);
        return true;
    }

    @Override
    public void removeAll() {
        mStorage.clear();
    }

    @Override
    public boolean has(String s) {
        return s != null && mStorage.contains(s);
    }

    @Override
    public String name() {
        return Files.getNameWithoutExtension(mFile.getPath());
    }

}
