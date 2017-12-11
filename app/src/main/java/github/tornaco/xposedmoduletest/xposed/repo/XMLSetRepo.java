package github.tornaco.xposedmoduletest.xposed.repo;

import android.util.Log;

import com.android.internal.util.XmlUtils;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/12/11.
 * Email: Tornaco@163.com
 */

public class XMLSetRepo<T> implements SetRepo<T> {

    @Getter
    private ExecutorService executorService;

    @Getter
    private File file;

    public XMLSetRepo(File file) {
        Preconditions.checkNotNull("File can not be null", file);
        this.file = file;

        if (!this.file.exists()) {
            try {
                Files.createParentDirs(file);
            } catch (IOException e) {
                XposedLog.wtf("Fail createParentDirs for: " + file + "\n" + Log.getStackTraceString(e));
            }
        }

        reload();
    }

    private final Set<T> mStorage = new HashSet<>();

    private final Object sync = new Object();

    @Override
    public Set<T> getAll() {
        return mStorage;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void reload() {
        synchronized (sync) {
            try {
                InputStream fis = Files.asByteSource(file)
                        .openStream();
                HashSet hashSet = XmlUtils.readSetXml(fis);
                XposedLog.debug("hashSet of: " + file + " has been read: " + hashSet);
                this.mStorage.addAll(hashSet);
            } catch (IOException e) {
                XposedLog.wtf("Fail reload@IOException: " + file + "\n" + Log.getStackTraceString(e));
            } catch (XmlPullParserException e) {
                XposedLog.wtf("Fail reload@XmlPullParserException: " + file + "\n" + Log.getStackTraceString(e));
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
        if (getExecutorService() == null) {
            new Thread(r).start();
        } else {
            getExecutorService().execute(r);
        }
    }

    @Override
    public void flush() {
        synchronized (sync) {
            try {
                Set out = new HashSet();
                out.addAll(mStorage);
                OutputStream os = Files.asByteSink(file)
                        .openStream();

            } catch (IOException e) {

            }

        }
    }

    @Override
    public void flushAsync() {

    }

    @Override
    public boolean add(T t) {
        mStorage.add(t);
        return true;
    }

    @Override
    public boolean remove(T t) {
        mStorage.remove(t);
        return true;
    }

    @Override
    public boolean has(T t) {
        return mStorage.contains(t);
    }

    @Override
    public void setExecutor(ExecutorService service) {

    }
}
