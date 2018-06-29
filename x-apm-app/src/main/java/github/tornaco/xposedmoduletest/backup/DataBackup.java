package github.tornaco.xposedmoduletest.backup;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import com.google.common.io.Files;

import org.newstand.logger.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.UUID;

import github.tornaco.xposedmoduletest.IBackupAgent;
import github.tornaco.xposedmoduletest.IFileDescriptorConsumer;
import github.tornaco.xposedmoduletest.IFileDescriptorInitializer;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.util.ZipUtils;
import github.tornaco.xposedmoduletest.xposed.app.IBackupCallbackAdapter;
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
        void onDataBackupFail(Throwable e);

        void onDataBackupSuccess();
    }

    private static File getBackupDirWrapDir(Context context) {
        return new File(context.getFilesDir(), ".backup");
    }

    public static void performBackup(Context context, File destDir, BackupRestoreListener listener) {

        XAPMManager ash = XAPMManager.get();
        if (!ash.isServiceAvailable()) {
            listener.onDataBackupFail(new NullPointerException("Service not available"));
            return;
        }

        listener.onProgress(0f);

        IBackupAgent agent = XAPMManager.get().getBackupAgent();

        try {
            if (agent != null) {
                File backDir = getBackupDirWrapDir(context);
                agent.performBackup(
                        new IFileDescriptorInitializer.Stub() {
                            @Override
                            public void initParcelFileDescriptor(String domain, String path, IFileDescriptorConsumer consumer)
                                    throws RemoteException {
                                File subFile = new File(backDir, path);
                                Logger.e("IBackupAgent create sub file: " + subFile);
                                try {
                                    Files.createParentDirs(subFile);
                                    if (subFile.createNewFile()) {
                                        ParcelFileDescriptor pfd = ParcelFileDescriptor.open(subFile, ParcelFileDescriptor.MODE_READ_WRITE);
                                        consumer.acceptAppParcelFileDescriptor(pfd);
                                    } else {
                                        consumer.acceptAppParcelFileDescriptor(null);
                                    }
                                } catch (IOException e) {
                                    Logger.e("IBackupAgent createParentDirs fail: " + Logger.getStackTraceString(e));
                                    consumer.acceptAppParcelFileDescriptor(null);
                                }
                            }
                        },
                        null, /* domain */
                        null, /* path */
                        new IBackupCallbackAdapter() {
                            @Override
                            public void onBackupFinished(String domain, String path) {
                                Logger.d("IBackupAgent onBackupFinished: " + path);
                                File subFile = new File(backDir, path);
                                // Move it to dest.
                                try {
                                    Files.move(subFile, new File(destDir, subFile.getName()));
                                    listener.onDataBackupSuccess();
                                } catch (Throwable e) {
                                    listener.onDataBackupFail(e);
                                    Logger.d("IBackupAgent move fail: " + Logger.getStackTraceString(e));
                                } finally {
                                    FileUtil.deleteDirQuiet(backDir);
                                    Logger.d("IBackupAgent deleteDirQuiet cleanup: " + backDir);
                                }
                            }

                            @Override
                            public void onFail(String message) {
                                Logger.e("IBackupAgent onFail: " + message);
                                listener.onDataBackupFail(new Exception(message));
                            }

                            @Override
                            public void onProgress(String progressMessage) {
                                Logger.d("IBackupAgent onProgress: " + progressMessage);
                            }
                        });
            } else {
                listener.onDataBackupFail(new NullPointerException("IBackupAgent is null"));
            }
        } catch (Exception e) {
            listener.onDataBackupFail(e);
        }

    }

    public static void performRestore(Context context, File zipFile, BackupRestoreListener listener) {

        performRestoreOld(context, zipFile, new BackupRestoreListener() {
            @Override
            public void onDataBackupFail(Throwable e) {
                // Nothing.
            }

            @Override
            public void onDataBackupSuccess() {
                // Nothing.
            }

            @Override
            public void onProgress(float progress) {
                // Nothing.
            }
        });

        XAPMManager ash = XAPMManager.get();
        if (!ash.isServiceAvailable()) {
            listener.onDataBackupFail(new NullPointerException("Service not available"));
            return;
        }

        if (!zipFile.exists()) {
            listener.onDataBackupFail(new NullPointerException("Zip not exist"));
            return;
        }

        IBackupAgent agent = ash.getBackupAgent();
        if (agent == null) {
            listener.onDataBackupFail(new NullPointerException("IBackupAgent not available"));
            return;
        }

        File backDir = getBackupDirWrapDir(context);

        try {
            File tmpZipFile = new File(backDir, zipFile.getName());
            Files.createParentDirs(tmpZipFile);
            Files.copy(zipFile, tmpZipFile);

            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(tmpZipFile, ParcelFileDescriptor.MODE_READ_ONLY);
            agent.performRestore(pfd, null, null, new IBackupCallbackAdapter() {
                @Override
                public void onRestoreFinished(String domain, String path) throws RemoteException {
                    super.onBackupFinished(domain, path);
                    listener.onDataBackupSuccess();
                    XAPMManager.get().showRebootNeededNotification("Restore");
                    Logger.e("IBackupAgent onRestoreFinished: " + path);
                }

                @Override
                public void onFail(String message) throws RemoteException {
                    super.onFail(message);
                    listener.onDataBackupFail(new Exception(message));
                    Logger.e("IBackupAgent onFail: " + message);
                }
            });
        } catch (FileNotFoundException e) {
            listener.onDataBackupFail(new NullPointerException("IBackupAgent FileNotFoundException: " + e));
        } catch (RemoteException e) {
            listener.onDataBackupFail(new NullPointerException("IBackupAgent RemoteException: " + e));
        } catch (IOException e) {
            listener.onDataBackupFail(new NullPointerException("IBackupAgent IOException: " + e));
        } finally {
            FileUtil.deleteDirQuiet(backDir);
        }
    }


    // Restore old data.
    private static void performRestoreOld(Context context, File zipFile, BackupRestoreListener listener) {

        XAppLockManager ag = XAppLockManager.get();
        XAPMManager ash = XAPMManager.get();
        if (!ag.isServiceAvailable() || !ash.isServiceAvailable()) {
            listener.onDataBackupFail(new NullPointerException("Service not available"));
            return;
        }

        if (!zipFile.exists()) {
            listener.onDataBackupFail(new NullPointerException("Zip not exist"));
            return;
        }

        // Create tmp dir.
        File tmpDir = null;
        try {
            String tmpPath = context.getCacheDir() + File.separator + UUID.randomUUID().toString();
            tmpDir = new File(tmpPath);
            Files.createParentDirs(tmpDir);
        } catch (IOException e) {
            listener.onDataBackupFail(e);
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
                    ash.setPermissionControlBlockModeForPkg(code, pkg, XAppOpsManager.MODE_IGNORED);
                }
                Closer.closeQuietly(fr);
                Closer.closeQuietly(br);
            }

        } catch (Exception e) {
            listener.onDataBackupFail(e);
        } finally {
            FileUtil.deleteDir(tmpDir);
            listener.onDataBackupSuccess();
        }
    }

}
