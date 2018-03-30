package github.tornaco.xposedmoduletest.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.gcm.GcmReceiver;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by Tornaco on 2018/3/23 14:18.
 * God bless no bug!
 */

public class XGcmReceiver extends GcmReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // super.onReceive(context, intent);
        if (BuildConfig.DEBUG) try {
            Logger.d("XGcmReceiver, onReceive: " + intent);
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GcmIntentService.class.getName());
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        } catch (Throwable throwable) {
            // Fixme Do something later.
        }
    }
}
