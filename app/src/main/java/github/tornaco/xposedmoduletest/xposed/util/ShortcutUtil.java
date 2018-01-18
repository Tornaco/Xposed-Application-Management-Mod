package github.tornaco.xposedmoduletest.xposed.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import org.newstand.logger.Logger;

import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.xposedmoduletest.ui.activity.ShortcutStubActivity;
import github.tornaco.xposedmoduletest.util.BitmapUtil;

/**
 * Created by guohao4 on 2018/1/18.
 * Email: Tornaco@163.com
 */

public class ShortcutUtil {

    public static void addShortcut(Context context, String pkgName, boolean redisable) {
        Logger.e("addShortcut: " + pkgName);

        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");

        Intent shortcutIntent = ShortcutStubActivity.createIntent(context, pkgName, redisable);

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);


        String title = null;
        try {
            final PackageManager pm = context.getPackageManager();
            title = pm.getApplicationLabel(
                    pm.getApplicationInfo(pkgName,
                            PackageManager.GET_META_DATA)).toString();
        } catch (Exception ignored) {
        }
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, String.valueOf(title + "*"));
        shortcut.putExtra("duplicate", false);
        Drawable d = ApkUtil.loadIconByPkgName(context, pkgName);
        Bitmap bd = BitmapUtil.getBitmap(context, d);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapUtil.createDisabledAppLauncherIcon(context, bd));

        context.sendBroadcast(shortcut);

        Logger.e("addShortcut done: " + pkgName);
    }

    public static void removeShortcut(Context context, String pkgName) {
        Intent shortcut = new Intent(
                "com.android.launcher.action.UNINSTALL_SHORTCUT");

        String title = null;
        try {
            final PackageManager pm = context.getPackageManager();
            title = pm.getApplicationLabel(pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA)).toString();
        } catch (Exception ignored) {
        }
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        Intent shortcutIntent = context.getPackageManager()
                .getLaunchIntentForPackage(pkgName);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        context.sendBroadcast(shortcut);
    }
}
