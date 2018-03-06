/*
 * Copyright (c) 2016 Nick Guo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.nick.eventbus.internal;

import android.support.annotation.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.EventReceiver;
import dev.nick.eventbus.annotation.CallInMainThread;
import dev.nick.eventbus.annotation.Events;
import dev.nick.eventbus.annotation.ReceiverMethod;
import dev.nick.eventbus.utils.ReflectionUtils;

/**
 * Created by nick on 16-4-1.
 * Email: nick.guo.dev@icloud.com
 */
public class EventsWirer implements ClassWirer {

    private final List<TaggedEventsReceiver> mReceivers;
    private Subscriber mSubscriber;

    public EventsWirer(Subscriber subscriber) {
        this.mSubscriber = subscriber;
        this.mReceivers = new ArrayList<>();
    }

    @Override
    public void wire(final Object o) {
        Class clz = o.getClass();

        int[] events = null;

        if (clz.isAnnotationPresent(Events.class)) {
            Events annotation = (Events) clz.getAnnotation(Events.class);
            events = annotation.value();
        }

        Method[] methods = clz.getDeclaredMethods();

        for (final Method m : methods) {
            ReflectionUtils.makeAccessible(m);
            int modifier = m.getModifiers();
            boolean isPublic = Modifier.isPublic(modifier);
            if (!isPublic) continue;
            String methodName = m.getName();
            boolean isHandle = methodName.startsWith("handle")
                    || m.isAnnotationPresent(ReceiverMethod.class);
            if (!isHandle) continue;

            boolean noParam;

            Class[] params = m.getParameterTypes();
            noParam = params.length == 0;

            final boolean eventParam = params.length == 1 && params[0] == Event.class;

            if (!noParam && !eventParam) continue;

            int[] usingEvents = events;
            if (m.isAnnotationPresent(Events.class)) {
                Events methodAnno = m.getAnnotation(Events.class);
                usingEvents = methodAnno.value();
            }

            if (usingEvents == null) continue;

            final boolean callInMain = m.isAnnotationPresent(CallInMainThread.class);

            TaggedEventsReceiver receiver = new TaggedEventsReceiver(usingEvents, o, methodName) {
                @Override
                public void onReceive(@NonNull Event event) {
                    if (eventParam)
                        ReflectionUtils.invokeMethod(m, o, event);
                    else ReflectionUtils.invokeMethod(m, o);
                }

                @Override
                public boolean callInMainThread() {
                    return callInMain;
                }
            };

            mSubscriber.subscribe(receiver);
            saveReceiver(receiver);
        }
    }

    public void unWire(Object o) {
        List<EventReceiver> receivers = findReceiversByTag(o);
        for (EventReceiver receiver : receivers) {
            mSubscriber.unSubscribe(receiver);
        }
    }

    private void saveReceiver(TaggedEventsReceiver receiver) {
        synchronized (mReceivers) {
            mReceivers.add(receiver);
        }
    }

    private List<EventReceiver> findReceiversByTag(Object tag) {
        List<EventReceiver> outs = new ArrayList<>();
        synchronized (mReceivers) {
            for (TaggedEventsReceiver receiver : mReceivers) {
                if (receiver.from == tag) {
                    outs.add(receiver);
                }
            }
        }
        return outs;
    }

    @Override
    public Class<? extends Annotation> annotationClass() {
        return Events.class;
    }

    abstract class TaggedEventsReceiver extends EventReceiver {

        int[] events;
        Object from;
        // For debug.
        String methodName;

        TaggedEventsReceiver(int[] events, Object from, String methodName) {
            this.events = events;
            this.from = from;
            this.methodName = methodName;
        }

        @Override
        public int[] events() {
            return events;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TaggedEventsReceiver that = (TaggedEventsReceiver) o;

            if (!Arrays.equals(events, that.events)) return false;
            if (!from.equals(that.from)) return false;
            return methodName.equals(that.methodName);
        }

        @Override
        public int hashCode() {
            int result = Arrays.hashCode(events);
            result = 31 * result + from.hashCode();
            result = 31 * result + methodName.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "TaggedEventsReceiver{" +
                    "events=" + Arrays.toString(events) +
                    ", from=" + from +
                    ", methodName='" + methodName + '\'' +
                    '}';
        }
    }
}
