package github.tornaco.xposedmoduletest.xposed.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.ShortcutStubActivity;
import github.tornaco.xposedmoduletest.util.BitmapUtil;

/**
 * Created by guohao4 on 2018/1/18.
 * Email: Tornaco@163.com
 */

public class ShortcutUtil {

    @SuppressLint("CheckResult")
    public static void addShortcut(final Context context, final String pkgName, final boolean redisable, final boolean redisabletr) {

        Logger.d("addShortcut: " + pkgName + "-" + redisable + "-" + redisabletr);

        CommonPackageInfo c = new CommonPackageInfo();
        c.setPkgName(pkgName);

        GlideApp.with(context)
                .asBitmap()
                .load(c)
                .placeholder(0)
                .error(R.mipmap.ic_launcher_round)
                .fallback(R.mipmap.ic_launcher_round)
                .transform(new CircleCrop())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        Intent shortcut = new Intent(
                                "com.android.launcher.action.INSTALL_SHORTCUT");

                        Intent shortcutIntent = ShortcutStubActivity.createIntent(context, pkgName, redisable, redisabletr);

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

                        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapUtil.createDisabledAppLauncherIcon(context, resource));

                        context.sendBroadcast(shortcut);

                        Logger.e("addShortcut done: " + pkgName);
                    }
                });

        Logger.e("addShortcut: " + pkgName);
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
