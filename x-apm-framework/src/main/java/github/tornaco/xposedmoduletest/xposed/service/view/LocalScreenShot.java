package github.tornaco.xposedmoduletest.xposed.service.view;

import android.app.NotificationManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceControl;
import android.view.WindowManager;

import com.google.common.io.Files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.repo.RepoProxy;
import github.tornaco.xposedmoduletest.xposed.util.DateUtils;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/5/24 14:34.
 * God bless no bug!
 */
public class LocalScreenShot {


    private Context mContext;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private NotificationManager mNotificationManager;
    private Display mDisplay;
    private DisplayMetrics mDisplayMetrics;
    private Matrix mDisplayMatrix;

    private Bitmap mScreenBitmap;

    public LocalScreenShot(Context context) {
        Resources r = context.getResources();
        mContext = context;
        LayoutInflater layoutInflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // Inflate the screenshot layout
        mDisplayMatrix = new Matrix();

        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mDisplay = mWindowManager.getDefaultDisplay();
        mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);
    }

    public void takeLongScreenshot() {
        mDisplay.getRealMetrics(mDisplayMetrics);
        takeScreenshot(() -> {
            XAPMManager.get().executeInputCommand("swipe 0 800 0 0".split("\\s+"));
            takeScreenshot(() -> {
            }, false, false, 0, mDisplayMetrics.heightPixels - 800, mDisplayMetrics.widthPixels, 800);
        }, false, false);
    }

    public void takeScreenshot(Runnable finisher, boolean statusBarVisible, boolean navBarVisible) {
        mDisplay.getRealMetrics(mDisplayMetrics);
        takeScreenshot(finisher, statusBarVisible, navBarVisible, 0, 0, mDisplayMetrics.widthPixels,
                mDisplayMetrics.heightPixels);
    }

    /**
     * Takes a screenshot of the current display.
     */
    public void takeScreenshot(Runnable finisher, boolean statusBarVisible, boolean navBarVisible,
                               int x, int y, int width, int height) {
        // We need to orient the screenshot correctly (and the Surface api seems to take screenshots
        // only in the natural orientation of the device :!)
        mDisplay.getRealMetrics(mDisplayMetrics);
        float[] dims = {mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels};
        float degrees = getDegreesForRotation(mDisplay.getRotation());
        boolean requiresRotation = (degrees > 0);
        if (requiresRotation) {
            // Get the dimensions of the device in its native orientation
            mDisplayMatrix.reset();
            mDisplayMatrix.preRotate(-degrees);
            mDisplayMatrix.mapPoints(dims);
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
        }

        // Take the screenshot
        mScreenBitmap = SurfaceControl.screenshot((int) dims[0], (int) dims[1]);
        if (mScreenBitmap == null) {
            notifyScreenshotError();
            finisher.run();
            return;
        }

        if (requiresRotation) {
            // Rotate the screenshot to the current orientation
            Bitmap ss = Bitmap.createBitmap(mDisplayMetrics.widthPixels,
                    mDisplayMetrics.heightPixels, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(ss);
            c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
            c.rotate(degrees);
            c.translate(-dims[0] / 2, -dims[1] / 2);
            c.drawBitmap(mScreenBitmap, 0, 0, null);
            c.setBitmap(null);
            // Recycle the previous bitmap
            mScreenBitmap.recycle();
            mScreenBitmap = ss;
        }

        if (width != mDisplayMetrics.widthPixels || height != mDisplayMetrics.heightPixels) {
            // Crop the screenshot to selected region
            Bitmap cropped = Bitmap.createBitmap(mScreenBitmap, x, y, width, height);
            mScreenBitmap.recycle();
            mScreenBitmap = cropped;
        }

        // Optimizations
        mScreenBitmap.setHasAlpha(false);
        mScreenBitmap.prepareToDraw();

        XposedLog.verbose("LocalScreenShot mScreenBitmap: " + mScreenBitmap);

        try {
            String fileName = RepoProxy.getBaseDataDir().getAbsolutePath()
                    + File.separator + Environment.DIRECTORY_PICTURES
                    + File.separator
                    + "LocalScreenShot_"
                    + DateUtils.formatForFileName(System.currentTimeMillis())
                    + ".png";
            Files.createParentDirs(new File(fileName));
            OutputStream os = new FileOutputStream(fileName);
            mScreenBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            XposedLog.verbose("LocalScreenShot saved");
        } catch (Exception e) {
            XposedLog.verbose("LocalScreenShot err: " + Log.getStackTraceString(e));
        }

        finisher.run();
    }

    /**
     * @return the current display rotation in degrees
     */
    private float getDegreesForRotation(int value) {
        switch (value) {
            case Surface.ROTATION_90:
                return 360f - 90f;
            case Surface.ROTATION_180:
                return 360f - 180f;
            case Surface.ROTATION_270:
                return 360f - 270f;
        }
        return 0f;
    }

    private void notifyScreenshotError() {

    }
}
