package github.tornaco.xposedmoduletest.xposed.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * Created by guohao4 on 2018/1/4.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
public class ProtectedBroadcastReceiver extends BroadcastReceiver {

    @NonNull
    private BroadcastReceiver host;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            XposedLog.verbose("ProtectedBroadcastReceiver: received" + intent);
            host.onReceive(context, intent);
        } catch (Throwable e) {
            XposedLog.wtf("Fail onReceive: " + Log.getStackTraceString(e));
        }
    }
}
