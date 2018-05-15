package github.tornaco.xposedmoduletest.ui.activity.stub;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.loader.GlideApp;
import github.tornaco.xposedmoduletest.util.OSUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2018/3/5.
 * Email: Tornaco@163.com
 */

public class LockScreenStubActivity extends Activity {

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, LockScreenStubActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @SuppressLint("CheckResult")
    public static void addShortcut(final Context context) {
        GlideApp.with(context)
                .asBitmap()
                .load(R.mipmap.ic_lock_screen)
                .placeholder(0)
                .error(R.mipmap.ic_launcher_round)
                .fallback(R.mipmap.ic_launcher_round)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource,
                                                Transition<? super Bitmap> transition) {
                        if (OSUtil.isOOrAbove()) {
                            doAddForO(context, resource);
                        } else {
                            doAddForNAndBelow(context, resource);
                        }
                    }
                });
    }

    private static void doAddForNAndBelow(final Context context, Bitmap res) {
        Intent shortcut = new Intent(
                "com.android.launcher.action.INSTALL_SHORTCUT");

        Intent shortcutIntent = createIntent(context);

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, context.getString(R.string.title_lock_now));
        shortcut.putExtra("duplicate", false);

        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, res);

        context.sendBroadcast(shortcut);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void doAddForO(Context context,
                                  Bitmap resource) {
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);

        Logger.d("doAddForO");
        if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
            Intent shortcutInfoIntent = createIntent(context);
            shortcutInfoIntent.setAction(Intent.ACTION_VIEW);

            ShortcutInfo info = new ShortcutInfo.Builder(context, context.getPackageName() + "-LOCK")
                    .setIcon(Icon.createWithBitmap(resource))
                    .setShortLabel(context.getString(R.string.title_lock_now))
                    .setIntent(shortcutInfoIntent)
                    .build();

            shortcutManager.requestPinShortcut(info, null);
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (XAPMManager.get().isServiceAvailable()) {
            XAPMManager.get().injectPowerEvent();
        }
        Logger.w("LockScreenStubActivity create");
        finishAffinity();
    }
}
