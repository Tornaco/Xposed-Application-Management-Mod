package github.tornaco.xposedmoduletest.xposed.util;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.ShortcutStubActivity;
import github.tornaco.xposedmoduletest.util.BitmapUtil;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.service.notification.UniqueIdFactory;

/**
 * Created by guohao4 on 2018/1/18.
 * Email: Tornaco@163.com
 */

public class ShortcutUtil {

    // FIXME. Not work on Oreo.
    @SuppressLint("CheckResult")
    public static void addShortcut(final Context context, final String pkgName,
                                   final boolean redisable, final boolean redisabletr,
                                   final boolean customIcon) {

        Logger.d("addShortcut: " + pkgName + "-" + redisable + "-" + redisabletr);

        CommonPackageInfo c = new CommonPackageInfo();
        c.setPkgName(pkgName);

        GlideApp.with(context)
                .asBitmap()
                .load(c)
                .placeholder(0)
                .error(R.mipmap.ic_launcher_round)
                .fallback(R.mipmap.ic_launcher_round)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        if (OSUtil.isOOrAbove()) {
                            doAddForO(context, pkgName, redisable, redisabletr, customIcon, resource);
                        } else {
                            doAddForNAndBelow(context, pkgName, redisable, redisabletr, customIcon, resource);
                        }
                    }
                });

        Logger.e("addShortcut: " + pkgName);
    }

    private static void doAddForNAndBelow(final Context context, final String pkgName,
                                          final boolean redisable, final boolean redisabletr,
                                          final boolean customIcon,
                                          Bitmap resource) {
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

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                customIcon ?
                        createDisabledAppLauncherIcon(context, resource)
                        : resource);

        context.sendBroadcast(shortcut);

        Logger.e("addShortcut done: " + pkgName);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void doAddForO(Context context, final String pkgName,
                                  final boolean redisable, final boolean redisabletr,
                                  final boolean customIcon,
                                  Bitmap resource) {
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);

        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
            Intent shortcutInfoIntent = ShortcutStubActivity.createIntent(context, pkgName, redisable, redisabletr);
            shortcutInfoIntent.setAction(Intent.ACTION_VIEW);

            String title = null;
            try {
                final PackageManager pm = context.getPackageManager();
                title = pm.getApplicationLabel(
                        pm.getApplicationInfo(pkgName,
                                PackageManager.GET_META_DATA)).toString();
            } catch (Exception ignored) {
            }
            if (title == null) title = pkgName;

            ShortcutInfo info = new ShortcutInfo.Builder(context, pkgName)
                    .setIcon(Icon.createWithBitmap(resource))
                    .setShortLabel(title)
                    .setIntent(shortcutInfoIntent)
                    .build();

            PendingIntent shortcutCallbackIntent = PendingIntent.getActivity(context, UniqueIdFactory.getNextId(),
                    shortcutInfoIntent, PendingIntent.FLAG_UPDATE_CURRENT);


            shortcutManager.requestPinShortcut(info, null);
        }

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

    public static Bitmap createDisabledAppLauncherIcon(Context context, Bitmap original) {
        Bitmap res = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
        Canvas canvas = new Canvas(res);
        canvas.drawBitmap(original, new Matrix(), null);
        Bitmap our = BitmapUtil.getBitmap(context, R.mipmap.ic_launcher_round);
        if (our == null) return original;
        Matrix matrix = new Matrix();
        matrix.postScale(0.2f, 0.2f);
        float padding = 3;
        Bitmap scaled = Bitmap.createBitmap(our, 0, 0, our.getWidth(), our.getHeight(),
                matrix, true);
        canvas.drawBitmap(scaled, original.getWidth() - scaled.getWidth()
                - padding, original.getHeight() - scaled.getHeight() - padding, null);
        return res;
    }
}
