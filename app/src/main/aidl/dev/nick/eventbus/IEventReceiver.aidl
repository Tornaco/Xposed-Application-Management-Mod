// IEventReceiver.aidl
package dev.nick.eventbus;

import dev.nick.eventbus.Event;

interface IEventReceiver {
    void onReceive(in Event event);
    int[] events();
}
