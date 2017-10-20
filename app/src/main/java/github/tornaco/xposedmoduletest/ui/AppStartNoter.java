package github.tornaco.xposedmoduletest.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;

import junit.framework.Assert;

import org.newstand.logger.Logger;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;

import dev.tornaco.vangogh.Vangogh;
import dev.tornaco.vangogh.display.appliers.ScaleInXYApplier;
import dev.tornaco.vangogh.loader.Loader;
import dev.tornaco.vangogh.loader.LoaderObserver;
import dev.tornaco.vangogh.media.BitmapImage;
import dev.tornaco.vangogh.media.Image;
import dev.tornaco.vangogh.media.ImageSource;
import github.tornaco.android.common.util.ApkUtil;
import github.tornaco.xposedmoduletest.ICallback;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.camera.CameraManager;
import github.tornaco.xposedmoduletest.x.XMode;
import github.tornaco.xposedmoduletest.x.XSettings;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

@SuppressWarnings("ConstantConditions")
public class AppStartNoter {

    private Handler uiHandler;
    private Context context;

    private boolean takePhoto;

    public AppStartNoter(Handler uiHandler, Context context) {
        this.uiHandler = uiHandler;
        this.context = context;
        Assert.assertTrue(
                "MainLopper is needed",
                this.uiHandler.getLooper() == Looper.getMainLooper());
        this.takePhoto = XSettings.get().takenPhotoEnabled(context);
        registerObserver();
    }

    private void registerObserver() {
        XSettings.get().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                takePhoto = XSettings.get().takenPhotoEnabled(context);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void note(
            final String callingAppName,
            final String targetPkg,
            final String appName,
            final ICallback callback) {
        uiHandler.post(new LockDialog(callingAppName, targetPkg, appName, callback));
    }

    private class LockDialog implements Runnable {

        final String callingAppName;
        final String targetPkg;
        final String appName;
        final ICallback callback;

        LockDialog(String callingAppName, String targetPkg, String appName, ICallback callback) {
            this.callingAppName = callingAppName;
            this.targetPkg = targetPkg;
            this.appName = appName;
            this.callback = callback;
        }

        @Override
        public void run() {
            try {
                Logger.v("Init note dialog...");

                @SuppressLint("InflateParams") final View container = LayoutInflater.from(context)
                        .inflate(R.layout.app_noter, null, false);

                PinLockView pinLockView = (PinLockView) container.findViewById(R.id.pin_lock_view);
                IndicatorDots indicatorDots = (IndicatorDots) container.findViewById(R.id.indicator_dots);
                pinLockView.attachIndicatorDots(indicatorDots);

                final AlertDialog d = new AlertDialog.Builder(context, R.style.NoterLight)
                        .setView(container)
                        .setCancelable(true)
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                onFail(callback);
                            }
                        })
                        .create();
                d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

                pinLockView.setPinLockListener(new PinLockListener() {
                    @Override
                    public void onComplete(String pin) {
                        if (pin.equals("6666")) {
                            d.dismiss();
                            onPass(callback);
                        } else {
                            Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                            container.startAnimation(shake);

                            if (takePhoto) {
                                CameraManager.get().captureSaveAsync(new CameraManager.PictureCallback() {
                                    @Override
                                    public void onImageReady(String path) {
                                        Logger.v("onImageReady:" + path);
                                    }

                                    @Override
                                    public void onFail(Exception e) {
                                        Logger.v("onImageFail:" + e);
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onEmpty() {

                    }

                    @Override
                    public void onPinChange(int pinLength, String intermediatePin) {

                    }
                });

                Logger.d("Show note dialog...");
                d.show();

                // Setup camera preview.
                View softwareCameraPreview = container.findViewById(R.id.surface);
                if (softwareCameraPreview != null) softwareCameraPreview.setVisibility(takePhoto ? View.VISIBLE : View.GONE);

                // Load app icon.
                ImageView iconView = (ImageView) container.findViewById(R.id.icon);
                Vangogh.with(context)
                        .load(targetPkg)
                        .skipMemoryCache(true)
                        .usingLoader(new Loader<Image>() {
                            @Nullable
                            @Override
                            public Image load(@NonNull ImageSource source,
                                              @Nullable LoaderObserver observer) {
                                String pkgName = source.getUrl();
                                Drawable d = ApkUtil.loadIconByPkgName(context, pkgName);
                                BitmapDrawable bd = (BitmapDrawable) d;
                                Logger.v("XXX- Loading COMPLETE for: " + pkgName);
                                BitmapImage bitmapImage = new BitmapImage(bd.getBitmap());
                                if (observer != null) {
                                    observer.onImageReady(bitmapImage);
                                }
                                return bitmapImage;
                            }

                            @Override
                            public int priority() {
                                return 3;
                            }

                            @Override
                            public ExecutorService getExecutor() {
                                return null;
                            }
                        })
                        .applier(new ScaleInXYApplier())
                        .placeHolder(0)
                        .fallback(R.mipmap.ic_launcher_round)
                        .into(iconView);
            } catch (Exception e) {
                Logger.e("Can not show dialog:" + Logger.getStackTraceString(e));
                // We should tell the res here.
                try {
                    callback.onRes(XMode.MODE_IGNORED); // BYPASS.
                } catch (RemoteException e1) {
                    Logger.e(Logger.getStackTraceString(e1));
                }
            }
        }
    }

    private void onPass(ICallback callback) {
        try {
            callback.onRes(XMode.MODE_ALLOWED);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }

    private void onFail(ICallback callback) {
        try {
            callback.onRes(XMode.MODE_DENIED);
        } catch (RemoteException e) {
            Logger.e(Logger.getStackTraceString(e));
        }
    }
}
