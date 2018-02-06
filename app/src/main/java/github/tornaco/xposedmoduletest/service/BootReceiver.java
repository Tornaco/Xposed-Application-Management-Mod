package github.tornaco.xposedmoduletest.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.provider.AppSettings;
import github.tornaco.xposedmoduletest.util.SeLinuxModeUtil;

/**
 * Created by guohao4 on 2018/2/6.
 * Email: Tornaco@163.com
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Logger.d("BootReceiver, boot complete");
            if (AppSettings.isSelinuxModeAutoSetEnabled(context)) {
                boolean enforce = AppSettings.isSelinuxModeEnforceEnabled(context);
                SeLinuxModeUtil.applyMode(enforce);
            }
        }
    }
}
