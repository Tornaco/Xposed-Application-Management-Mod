package github.tornaco.xposedmoduletest.ui;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.common.io.Files;

import org.newstand.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import dev.nick.tiles.tile.DashboardFragment;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by Tornaco on 2018/4/10 10:03.
 * God bless no bug!
 */
public class AppCustomDashboardFragment extends DashboardFragment {

    @Override
    protected boolean androidPStyleIcon() {
        return AppSettings.isPStyleIcon(this.getActivity());
    }

    @Override
    protected void onUIBuilt() {
        super.onUIBuilt();
        // Dump icons.
        if (false && BuildConfig.DEBUG) {
            ViewGroup parent = mDashboard;
            XExecutor.execute(() -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {

                }
                dumpImageViewIcon(parent);
            });
        }
    }

    private void dumpImageViewIcon(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            android.view.View child = viewGroup.getChildAt(i);

            Logger.i("dumpImageViewIcon: " + child);

            if (child instanceof ViewGroup) {
                dumpImageViewIcon((ViewGroup) child);
            }

            if (child instanceof ImageView) {
                ImageView imageView = (ImageView) child;

                Bitmap source = imageView.createSnapshot(Bitmap.Config.ARGB_8888, Color.TRANSPARENT, false);

                Logger.i("dumpImageViewIcon-- IMAGEVIEW: " + child + ", SOURCE: " + source);

                if (source != null) {
                    File imageFile = new File(Environment.getExternalStorageDirectory().getPath()
                            + File.separator + "X-APM-DUMPS"
                            + File.separator + UUID.randomUUID().toString() + ".png");
                    try {
                        Files.createParentDirs(imageFile);
                    } catch (IOException e) {
                        Logger.e("Fail createParentDirs:" + Log.getStackTraceString(e));
                    }
                    try {
                        OutputStream os = Files.asByteSink(imageFile).openStream();
                        source.compress(Bitmap.CompressFormat.PNG, 100, os);
                        os.close();

                        Logger.w("Icon dump: " + imageFile);
                    } catch (IOException e) {
                        Logger.e("Fail compress:" + Log.getStackTraceString(e));
                    }
                }
            }
        }
    }
}
