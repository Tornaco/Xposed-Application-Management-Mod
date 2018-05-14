package dev.nick.eventbus.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import dev.nick.eventbus.EventBus;

/**
 * Created by nick on 16-4-2.
 * Email: nick.guo.dev@icloud.com
 */
public class EventBusService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        EventBus bus = EventBus.from();
        if (bus == null) {
            Log.e(getClass().getSimpleName(), "EventBus not created!");
            return null;
        }
        return bus.generateStub();
    }
}
