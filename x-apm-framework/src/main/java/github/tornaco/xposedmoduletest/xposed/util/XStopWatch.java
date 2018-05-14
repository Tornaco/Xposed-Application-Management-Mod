/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package github.tornaco.xposedmoduletest.xposed.util;

import android.os.SystemClock;

/**
 * A simple class to measure elapsed time.
 * <p>
 * <code>
 * XStopWatch s = XStopWatch.start();
 * // Do your stuff
 * s.split();
 * // More stuff
 * s.split();
 * // More stuff
 * s.stop();
 * </code>
 */
public class XStopWatch {

    private final String mName;
    private final long mStart;
    private long mLastSplit;

    private XStopWatch(String name) {
        mName = name;
        mStart = getCurrentTime();
        mLastSplit = mStart;
        XposedLog.wtf("XStopWatch(" + mName + ") start");
    }

    public static XStopWatch start(String name) {
        return new XStopWatch(name);
    }

    public void split(String label) {
        long now = getCurrentTime();
        long elapse = now - mLastSplit;
        XposedLog.wtf("XStopWatch(" + mName + ") split(" + label + ") " + elapse + "ms");
        mLastSplit = now;
    }

    public void stop() {
        long now = getCurrentTime();
        XposedLog.wtf("XStopWatch(" + mName + ") stop: "
                + (now - mLastSplit)
                + "  (total " + (now - mStart) + ")"
                + "ms");
    }

    private static long getCurrentTime() {
        return SystemClock.elapsedRealtime();
    }
}
