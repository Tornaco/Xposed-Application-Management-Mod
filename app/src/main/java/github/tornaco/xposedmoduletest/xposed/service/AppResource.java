package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;

/**
 * Created by Tornaco on 2018/4/4 10:56.
 * God bless no bug!
 */
@AllArgsConstructor
@Getter
public class AppResource {

    private Context context;

    public Bitmap loadBitmapFromAPMApp(String resName) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("loadBitmapFromAPMApp, resName: " + resName);
        }
        try {
            Context appContext = getAPMAppContext();
            if (appContext != null) {
                Resources res = appContext.getResources();
                if (XposedLog.isVerboseLoggable()) {
                    XposedLog.verbose("loadBitmapFromAPMApp, res: " + res);
                }
                if (res != null) {
                    int id = res.getIdentifier(resName, "drawable", BuildConfig.APPLICATION_ID);
                    if (XposedLog.isVerboseLoggable()) {
                        XposedLog.verbose("loadBitmapFromAPMApp, id: " + id);
                    }
                    if (id > 0) {
                        Bitmap bitmap = BitmapFactory.decodeResource(res, id);
                        if (XposedLog.isVerboseLoggable()) {
                            XposedLog.verbose("loadBitmapFromAPMApp, bitmap: " + bitmap);
                        }
                        if (bitmap != null) {
                            return bitmap;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadBitmapFromAPMApp: " + Log.getStackTraceString(e));
        }
        return null;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    public Icon loadIconFromAPMApp(String resName) {
        if (XposedLog.isVerboseLoggable()) {
            XposedLog.verbose("loadIconFromAPMApp, resName: " + resName);
        }
        try {
            Context appContext = getAPMAppContext();
            if (appContext != null) {
                Resources res = appContext.getResources();
                if (XposedLog.isVerboseLoggable()) {
                    XposedLog.verbose("loadIconFromAPMApp, res: " + res);
                }
                if (res != null) {
                    int id = res.getIdentifier(resName, "drawable", BuildConfig.APPLICATION_ID);
                    if (XposedLog.isVerboseLoggable()) {
                        XposedLog.verbose("loadIconFromAPMApp, id: " + id);
                    }
                    if (id > 0) {
                        Icon ic = Icon.createWithResource(res, id);
                        if (XposedLog.isVerboseLoggable()) {
                            XposedLog.verbose("loadIconFromAPMApp, ic: " + ic);
                        }
                        if (ic != null) {
                            return ic;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail loadIconFromAPMApp: " + Log.getStackTraceString(e));
        }
        return Icon.createWithResource(getContext(), android.R.drawable.stat_sys_warning);
    }

    private Context getAPMAppContext() {
        Context context = getContext();
        if (context == null) {
            XposedLog.wtf("Context is null!!!");
            return null;
        }
        try {
            return context.createPackageContext(BuildConfig.APPLICATION_ID, CONTEXT_IGNORE_SECURITY);
        } catch (Throwable e) {
            XposedLog.wtf("Fail createPackageContext: " + Log.getStackTraceString(e));
        }
        return null;
    }

    String[] readStringArrayFromAPMApp(String resName) {
        Context context = getContext();
        if (context == null) {
            XposedLog.wtf("Context is null!!!");
            return new String[0];
        }
        try {
            Context appContext =
                    context.createPackageContext(BuildConfig.APPLICATION_ID, CONTEXT_IGNORE_SECURITY);
            Resources res = appContext.getResources();
            int id = res.getIdentifier(resName, "array", BuildConfig.APPLICATION_ID);
            XposedLog.debug("readStringArrayFromAPMApp get id: " + id + ", for res: " + resName);
            if (id != 0) {
                return res.getStringArray(id);
            }
        } catch (Throwable e) {
            XposedLog.wtf("Fail createPackageContext: " + Log.getStackTraceString(e));
        }
        return new String[0];
    }
}
