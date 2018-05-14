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

package dev.nick.eventbus;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import dev.nick.eventbus.utils.Preconditions;

/**
 * Created by nick on 16-4-1.
 * Email: nick.guo.dev@icloud.com
 */
public class Event implements Cloneable, Parcelable {

    private int eventType;

    private Bundle data;

    private int arg1;
    private int arg2;
    private int arg3;
    private int arg4;
    private int arg5;

    private Object obj;

    public Event(int eventType) {
        this.eventType = eventType;
    }

    protected Event(Parcel in) {
        eventType = in.readInt();
        data = in.readBundle(getClass().getClassLoader());
        arg1 = in.readInt();
        arg2 = in.readInt();
        arg3 = in.readInt();
        arg4 = in.readInt();
        arg5 = in.readInt();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    private Event(int eventType, Bundle data, int arg1, int arg2, int arg3, int arg4, int arg5, Object o) {
        this.eventType = eventType;
        this.data = data;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.arg3 = arg3;
        this.arg4 = arg4;
        this.arg5 = arg5;
        this.obj = o;
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public int getArg1() {
        return arg1;
    }

    public int getArg2() {
        return arg2;
    }

    public int getArg4() {
        return arg4;
    }

    public int getArg3() {
        return arg3;
    }

    public int getArg5() {
        return arg5;
    }

    public Object getObj() {
        return obj;
    }

    public int getEventType() {
        return eventType;
    }

    public Bundle getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventType=" + eventType +
                ", data=" + data +
                ", arg1=" + arg1 +
                ", arg2=" + arg2 +
                ", arg3=" + arg3 +
                ", arg4=" + arg4 +
                ", arg5=" + arg5 +
                ", obj=" + obj +
                '}';
    }

    public static Event fromClone(@NonNull Event event) {
        try {
            return (Event) Preconditions.checkNotNull(event, "Event cannot be null.").clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Internal error:" + e);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(eventType);
        dest.writeBundle(data);
        dest.writeInt(arg1);
        dest.writeInt(arg2);
        dest.writeInt(arg3);
        dest.writeInt(arg4);
        dest.writeInt(arg5);
    }

    public static class EventBuilder {

        private int eventType;
        private Bundle data;

        // For none-rmi
        private int arg1;
        private int arg2;
        private int arg3;
        private int arg4;
        private int arg5;
        private Object obj;

        EventBuilder() {
        }

        public Event.EventBuilder eventType(int eventType) {
            this.eventType = eventType;
            return this;
        }

        public Event.EventBuilder data(Bundle data) {
            this.data = data;
            return this;
        }

        public Event.EventBuilder arg1(int arg1) {
            this.arg1 = arg1;
            return this;
        }

        public Event.EventBuilder arg2(int arg2) {
            this.arg2 = arg2;
            return this;
        }

        public Event.EventBuilder arg3(int arg3) {
            this.arg3 = arg3;
            return this;
        }

        public Event.EventBuilder arg4(int arg4) {
            this.arg4 = arg4;
            return this;
        }

        public Event.EventBuilder arg5(int arg5) {
            this.arg5 = arg5;
            return this;
        }

        public Event.EventBuilder obj(Object o) {
            this.obj = o;
            return this;
        }

        public Event build() {
            return new Event(eventType, data, arg1, arg2, arg3, arg4, arg5, obj);
        }
    }
}