// IEventBus.aidl
package dev.nick.eventbus;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.IEventReceiver;

interface IEventBus {
   void publish(in Event event);
   void publishEmptyEvent(int event);
   void subscribe(in IEventReceiver receiver);
   void unSubscribe(in IEventReceiver receiver);
}
