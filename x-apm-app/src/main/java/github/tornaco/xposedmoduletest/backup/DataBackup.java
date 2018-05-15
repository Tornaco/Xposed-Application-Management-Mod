package github.tornaco.xposedmoduletest.backup;

import android.content.Context;

import com.google.common.io.Files;

import org.newstand.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.UUID;

import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.util.DateUtils;
import github.tornaco.xposedmoduletest.util.ZipUtils;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import github.tornaco.xposedmoduletest.xposed.util.FileUtil;

/**
 * Created by guohao4 on 2018/1/2.
 * Email: Tornaco@163.com
 */

public abstract class DataBackup {

    public interface BackupRestoreListener extends FileUtil.ProgressListener {
        void onDataBackupFail(int errNum, Throwable e);

        void onDataBackupSuccess();
    }

    public static void performBackup(File dir, BackupRestoreListener listener) {

        XAppLockManager ag = XAppLockManager.get();
        XAPMManager ash = XAPMManager.get();
        if (!ag.isServiceAvailable() || !ash.isServiceAvailable()) {
            listener.onDataBackupFail(-1, new NullPointerException("Service not available"));
            return;
        }

        listener.onProgress(0f);

        // Create tmp dir.
        File tmpDir = null;
        try {
            String tmpPath = dir.getPath() + File.separator + UUID.randomUUID().toString();
            tmpDir = new File(tmpPath);
            Files.createParentDirs(tmpDir);
        } catch (IOException e) {
            listener.onDataBackupFail(0, e);
            return;
        }

        appendStringArray(new File(tmpDir, "boot"), ash.getBootBlockApps(true), listener);
        appendStringArray(new File(tmpDir, "start"), ash.getStartBlockApps(true), listener);
        appendStringArray(new File(tmpDir, "lk"), ash.getLKApps(true), listener);
        appendStringArray(new File(tmpDir, "rfk"), ash.getRFKApps(true), listener);
        appendStringArray(new File(tmpDir, "trk"), ash.getTRKApps(true), listener);
        appendStringArray(new File(tmpDir, "lazy"), ash.getLazyApps(true), listener);
        appendStringArray(new File(tmpDir, "privacy"), ash.getPrivacyList(true), listener);

        appendStringArray(new File(tmpDir, "lock"), ag.getLockApps(true), listener);
        appendStringArray(new File(tmpDir, "blur"), ag.getBlurApps(true), listener);
        appendStringArray(new File(tmpDir, "uninstall"), ag.getUPApps(true), listener);

        appendStringArray(new File(tmpDir, "perms"), ash.getRawPermSettings(0, 0), listener);

        try {
            long startTimeMills = System.currentTimeMillis();
            String name = "Backup" + DateUtils.formatForFileName(startTimeMills) + ".zip";
            ZipUtils.zip(tmpDir.getPath(), dir.getPath(), name);
        } catch (Exception e) {
            listener.onDataBackupFail(2, e);
            return;
        } finally {
            FileUtil.deleteDir(tmpDir);
        }

        listener.onDataBackupSuccess();
    }

    private static void appendStringArray(File file, String[] data, BackupRestoreListener listener) {
        try {
            Files.createParentDirs(file);
            OutputStream os = Files.asByteSink(file).openStream();
            PrintWriter printWriter = new PrintWriter(os);
            for (String p : data) {
                printWriter.println(p);
            }
            printWriter.flush();
            Closer.closeQuietly(printWriter);
            Closer.closeQuietly(os);
        } catch (IOException e) {
            listener.onDataBackupFail(1, e);
        }

    }

    public static void performRestore(Context context, File zipFile, BackupRestoreListener listener) {

        XAppLockManager ag = XAppLockManager.get();
        XAPMManager ash = XAPMManager.get();
        if (!ag.isServiceAvailable() || !ash.isServiceAvailable()) {
            listener.onDataBackupFail(-1, new NullPointerException("Service not available"));
            return;
        }

        if (!zipFile.exists()) {
            listener.onDataBackupFail(-2, new NullPointerException("Zip not exist"));
            return;
        }

        // Create tmp dir.
        File tmpDir = null;
        try {
            String tmpPath = context.getCacheDir() + File.separator + UUID.randomUUID().toString();
            tmpDir = new File(tmpPath);
            Files.createParentDirs(tmpDir);
        } catch (IOException e) {
            listener.onDataBackupFail(0, e);
            return;
        }

        try {
            listener.onProgress(0f);

            ZipUtils.unzip(zipFile.getPath(), tmpDir.getPath(), false);

            InputStreamReader fr;
            BufferedReader br;
            String line;

            // Boot.
            if (new File(tmpDir, "boot").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "boot")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ash.addOrRemoveBootBlockApps(new String[]{line}, XAPMManager.Op.ADD);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // Start.
            if (new File(tmpDir, "start").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "start")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ash.addOrRemoveStartBlockApps(new String[]{line}, XAPMManager.Op.ADD);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // LK.
            if (new File(tmpDir, "lk").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "lk")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ash.addOrRemoveLKApps(new String[]{line}, XAPMManager.Op.ADD);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // RFK.
            if (new File(tmpDir, "rfk").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "rfk")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ash.addOrRemoveRFKApps(new String[]{line}, XAPMManager.Op.ADD);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // TRK.
            if (new File(tmpDir, "trk").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "trk")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ash.addOrRemoveTRKApps(new String[]{line}, XAPMManager.Op.ADD);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // Lazy.
            if (new File(tmpDir, "lazy").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "lazy")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ash.addOrRemoveLazyApps(new String[]{line}, XAPMManager.Op.ADD);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // privacy.
            if (new File(tmpDir, "privacy").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "privacy")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ash.addOrRemoveFromPrivacyList(line, XAPMManager.Op.ADD);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // lock.
            if (new File(tmpDir, "lock").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "lock")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ag.addOrRemoveLockApps(new String[]{line}, true);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // blur.
            if (new File(tmpDir, "blur").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "blur")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ag.addOrRemoveBlurApps(new String[]{line}, true);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // uninstall.
            if (new File(tmpDir, "uninstall").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "uninstall")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    ag.addOrRemoveUPApps(new String[]{line}, true);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

            // Perms.
            if (new File(tmpDir, "perms").exists()) {
                fr = new InputStreamReader(Files.asByteSource(new File(tmpDir, "perms")).openStream());
                br = new BufferedReader(fr);
                while ((line = br.readLine()) != null) {
                    StringTokenizer t = new StringTokenizer(line, "@");
                    Logger.d("perms: " + line);
                    String pkg = t.nextToken();
                    int code = Integer.parseInt(t.nextToken());
                    ash.setPermissionControlBlockModeForPkg(code, pkg, AppOpsManagerCompat.MODE_IGNORED);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

        } catch (Exception e) {
            listener.onDataBackupFail(1, e);
        } finally {
            FileUtil.deleteDir(tmpDir);
            listener.onDataBackupSuccess();
        }
    }

}
