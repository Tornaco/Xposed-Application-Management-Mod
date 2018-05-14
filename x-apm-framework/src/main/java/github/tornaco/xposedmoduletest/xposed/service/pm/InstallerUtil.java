package github.tornaco.xposedmoduletest.xposed.service.pm;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.common.io.Files;

import java.io.File;

import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/5/9 13:43.
 * God bless no bug!
 */
public class InstallerUtil {

    static File getMonolithicPackageFile(File file) {
        XposedLog.verbose(XposedLog.PREFIX_PM + "getMonolithicPackageFile: " + file);
        if (file.isFile()) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "getMonolithicPackageFile is file..");
            return file;
        }
        // Check if it is .apk
        String ext = Files.getFileExtension(file.getAbsolutePath());
        XposedLog.verbose(XposedLog.PREFIX_PM + "getMonolithicPackageFile ext: " + ext);
        if (ext.contains(".apk")) {
            return file;
        }
        // Find .apk file.
        String candidateName = file.getAbsolutePath() + File.separator + "base.apk";
        File candidateFile = new File(candidateName);
        boolean exist = candidateFile.exists();
        XposedLog.verbose(XposedLog.PREFIX_PM + "getMonolithicPackageFile candidateFile: " + candidateFile + ", exist? " + exist);
        if (exist) {
            return candidateFile;
        } else {
            return file;
        }
    }

    public static PackageInstallerManager.VerifyArgs generateVerifyArgs(Context context,
                                                                        PackageInfo packageInfo,
                                                                        String apkPath,
                                                                        String installerPackage) {
        String appPackageName = packageInfo.packageName;
        if (appPackageName == null) {
            XposedLog.verbose(XposedLog.PREFIX_PM + "generateVerifyArgs: package-name is null");
            return null;
        }

        String sourceApkPath = PackageInstallerManager.from(context)
                .getSourceApkFilePath(appPackageName);
        XposedLog.verbose(XposedLog.PREFIX_PM + "generateVerifyArgs, sourceApkPath: " + sourceApkPath);

        boolean isReplacing = PkgUtil.isPkgInstalled(context, appPackageName);

        // Retrieve label and icon.
        Resources pRes = context.getResources();
        AssetManager assetManager = new AssetManager();
        assetManager.addAssetPath(apkPath);
        Resources res = new Resources(assetManager, pRes.getDisplayMetrics(), pRes.getConfiguration());

        // Label.
        CharSequence label = null;
        if (packageInfo.applicationInfo != null && packageInfo.applicationInfo.labelRes != 0) {
            try {
                label = res.getText(packageInfo.applicationInfo.labelRes);
                XposedLog.verbose(XposedLog.PREFIX_PM + "label1: " + label);
            } catch (Throwable ignored) {
                XposedLog.wtf(XposedLog.PREFIX_PM + "generateVerifyArgs, Fail get label from res:" + Log.getStackTraceString(ignored));
            }
        }

        if (label == null && packageInfo.applicationInfo != null) {
            label = packageInfo.applicationInfo.nonLocalizedLabel == null ? packageInfo.packageName
                    : packageInfo.applicationInfo.nonLocalizedLabel;
            XposedLog.verbose(XposedLog.PREFIX_PM + "generateVerifyArgs, label2: " + label);
        }

        // Use previous app label if replacing.
        if (label == null && isReplacing) {
            CharSequence installedLabel = PkgUtil.loadNameByPkgName(context, appPackageName);
            if (installedLabel != null && !"NULL".equalsIgnoreCase(String.valueOf(installedLabel))) {
                label = installedLabel;
                XposedLog.verbose(XposedLog.PREFIX_PM + "generateVerifyArgs, label3: " + label);
            }
        }

        if (label == null) {
            label = packageInfo.packageName;
            XposedLog.verbose(XposedLog.PREFIX_PM + "generateVerifyArgs, label4: " + label);
        }

        // Icon.
        Drawable icon = null;
        if (packageInfo.applicationInfo != null && packageInfo.applicationInfo.icon != 0) {
            try {
                icon = res.getDrawable(packageInfo.applicationInfo.icon);
            } catch (Throwable ignored) {

            }
        }

        if (icon == null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            if (appInfo != null) {
                try {
                    appInfo.sourceDir = apkPath;
                    appInfo.publicSourceDir = apkPath;
                    icon = appInfo.loadIcon(context.getPackageManager());
                } catch (Throwable ignored) {

                }
            }
        }

        if (icon == null) {
            icon = context.getPackageManager().getDefaultActivityIcon();
        }

        // Installer.
        String installerAppLabel =
                installerPackage == null ?
                        "SHELL(Root)"
                        : String.valueOf(PkgUtil.loadNameByPkgName(context, installerPackage));

        // Need for rules checker.
        String installerPkg = installerPackage == null ? "SHELL" : installerPackage;

        return PackageInstallerManager.VerifyArgs
                .builder()
                .appIcon(icon)
                .isReplacing(isReplacing)
                .packageName(appPackageName)
                .appLabel(String.valueOf(label))
                .soucrePath(sourceApkPath)
                .installerAppLabel(installerAppLabel)
                .installerPackageName(installerPkg)
                .build();
    }

    private static boolean isStageName(String name) {
        final boolean isFile = name.startsWith("vmdl") && name.endsWith(".tmp");
        final boolean isContainer = name.startsWith("smdl") && name.endsWith(".tmp");
        final boolean isLegacyContainer = name.startsWith("smdl2tmp");
        return isFile || isContainer || isLegacyContainer;
    }

    private static boolean isStagePath(String path) {
        return path.contains("vmdl") || path.contains("smdl") || path.contains("smdl2tmp");
    }

    public static boolean isSourcePackageFilePath(String path) {
        File file = new File(path);
        return file.exists() && isSourcePackageFile(file);
    }

    private static boolean isSourcePackageFile(File file) {
        return !isStagePath(file.getAbsolutePath()) && !isStageName(file.getName()) && file.exists() && file.isFile();
    }
}
