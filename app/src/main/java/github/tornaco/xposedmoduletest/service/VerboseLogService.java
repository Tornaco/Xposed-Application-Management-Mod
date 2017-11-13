package github.tornaco.xposedmoduletest.service;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.provider.BlockRecordProvider;

/**
 * Created by guohao4 on 2017/11/13.
 * Email: Tornaco@163.com
 */

public class VerboseLogService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getContentResolver().registerContentObserver(BlockRecordProvider.CONTENT_URI,
                false, new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange, Uri uri) {
                        super.onChange(selfChange, uri);
                        Logger.d("VerboseLogService@onChange: " + uri);
                    }
                });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }
}
