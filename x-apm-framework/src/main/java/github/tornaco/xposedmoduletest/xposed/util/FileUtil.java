package github.tornaco.xposedmoduletest.xposed.util;

import android.util.Log;

import com.google.common.io.Files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import github.tornaco.android.common.BlackHole;
import github.tornaco.android.common.Collections;

/**
 * Created by Nick@NewStand.org on 2017/3/13 10:03
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public abstract class FileUtil {

    /**
     * Interface definition for a callback to be invoked regularly as
     * verification proceeds.
     */
    public interface ProgressListener {
        /**
         * Called periodically as the verification progresses.
         *
         * @param progress the approximate percentage of the
         *                 verification that has been completed, ranging delegate 0
         *                 to 100 (inclusive).
         */
        public void onProgress(float progress);
    }


    public static void copy(String spath, String dpath, ProgressListener listener) throws IOException {
        FileInputStream fis = new FileInputStream(spath);
        FileOutputStream fos = new FileOutputStream(dpath);
        int totalByte = fis.available();
        int read = 0;
        int n;
        byte[] buffer = new byte[4096];
        while ((n = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, n);
            fos.flush();
            read += n;
            float per = (float) read / (float) totalByte;
            if (listener != null) {
                listener.onProgress(per * 100);
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {

        }
        Closer.closeQuietly(fis);
        Closer.closeQuietly(fos);
    }

    public static String formatSize(long fileSize) {
        String wellFormatSize = "";
        if (fileSize >= 0 && fileSize < 1024) {
            wellFormatSize = fileSize + "B";
        } else if (fileSize >= 1024 && fileSize < (1024 * 1024)) {
            wellFormatSize = Long.toString(fileSize / 1024) + "KB";
        } else if (fileSize >= (1024 * 1024) && fileSize < (1024 * 1024 * 1024)) {
            wellFormatSize = Long.toString(fileSize / (1024 * 1024)) + "MB";
        } else if (fileSize >= (1024 * 1024 * 1024)) {
            wellFormatSize = Long.toString(fileSize / (1024 * 1024 * 1024)) + "GB";
        }
        return wellFormatSize;
    }

    public static boolean deleteDir(File dir) {
        final boolean[] res = {true};
        Collections.consumeRemaining(Files.fileTreeTraverser()
                .postOrderTraversal(dir), file -> {
            if (!file.delete()) res[0] = false;
        });
        return res[0];
    }

    public static void deleteDirQuiet(File dir) {
        try {
            BlackHole.eat(deleteDir(dir));
            BlackHole.eat(dir.delete());
        } catch (Throwable ignored) {

        }
    }

    public static boolean writeString(String str, String path) {
        BufferedWriter bf = null;
        try {
            com.google.common.io.Files.createParentDirs(new File(path));
            bf = com.google.common.io.Files.newWriter(new File(path), Charset.defaultCharset());
            bf.write(str, 0, str.length());
            return true;
        } catch (Exception e) {
            Log.e(XposedLog.TAG, "Fail to write file:" + Log.getStackTraceString(e));
        } finally {
            Closer.closeQuietly(bf);
        }
        return false;
    }

    public static String readString(String path) {
        BufferedReader reader = null;
        try {
            if (!new File(path).exists())
                return null;
            reader = com.google.common.io.Files.newReader(new File(path), Charset.defaultCharset());
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            Log.e(XposedLog.TAG, "Fail to read file:" + Log.getStackTraceString(e));
        } catch (OutOfMemoryError oom) {
            Log.e(XposedLog.TAG, "OOM while read file:" + Log.getStackTraceString(oom));
        } finally {
            Closer.closeQuietly(reader);
        }
        return null;
    }

    public static boolean isEmptyDir(File dir) {
        return dir.exists() && dir.isDirectory() && dir.list().length == 0;
    }

    public static boolean isEmptyDirOrNoExist(File dir) {
        if (!dir.exists()) return true;
        return isEmptyDir(dir);
    }
}
