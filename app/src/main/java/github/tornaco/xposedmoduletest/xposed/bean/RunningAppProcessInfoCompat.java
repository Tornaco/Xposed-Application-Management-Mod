package github.tornaco.xposedmoduletest.xposed.bean;

import android.annotation.IntDef;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static android.app.ActivityManager.PROCESS_STATE_BOUND_FOREGROUND_SERVICE;
import static android.app.ActivityManager.PROCESS_STATE_FOREGROUND_SERVICE;
import static android.app.ActivityManager.PROCESS_STATE_HEAVY_WEIGHT;
import static android.app.ActivityManager.PROCESS_STATE_HOME;
import static android.app.ActivityManager.PROCESS_STATE_IMPORTANT_FOREGROUND;
import static android.app.ActivityManager.PROCESS_STATE_NONEXISTENT;
import static android.app.ActivityManager.PROCESS_STATE_SERVICE;
import static android.app.ActivityManager.PROCESS_STATE_TOP_SLEEPING;
import static android.app.ActivityManager.PROCESS_STATE_TRANSIENT_BACKGROUND;

/**
 * Created by guohao4 on 2017/12/20.
 * Email: Tornaco@163.com
 */

public class RunningAppProcessInfoCompat implements Parcelable {
    /**
     * The name of the process that this object is associated with
     */
    public String processName;

    /**
     * The pid of this process; 0 if none
     */
    public int pid;

    /**
     * The user id of this process.
     */
    public int uid;

    /**
     * All packages that have been loaded into the process.
     */
    public String pkgList[];

    /**
     * Constant for {@link #flags}: this is an app that is unable to
     * correctly save its state when going to the background,
     * so it can not be killed while in the background.
     *
     * @hide
     */
    public static final int FLAG_CANT_SAVE_STATE = 1 << 0;

    /**
     * Constant for {@link #flags}: this process is associated with a
     * persistent system app.
     *
     * @hide
     */
    public static final int FLAG_PERSISTENT = 1 << 1;

    /**
     * Constant for {@link #flags}: this process is associated with a
     * persistent system app.
     *
     * @hide
     */
    public static final int FLAG_HAS_ACTIVITIES = 1 << 2;

    /**
     * Flags of information.  May be any of
     * {@link #FLAG_CANT_SAVE_STATE}.
     *
     * @hide
     */
    public int flags;

    /**
     * Last memory trim level reported to the process: corresponds to
     * the values supplied to {@link android.content.ComponentCallbacks2#onTrimMemory(int)
     * ComponentCallbacks2.onTrimMemory(int)}.
     */
    public int lastTrimLevel;

    /**
     * @hide
     */
    @IntDef(prefix = {"IMPORTANCE_"}, value = {
            IMPORTANCE_FOREGROUND,
            IMPORTANCE_FOREGROUND_SERVICE,
            IMPORTANCE_TOP_SLEEPING,
            IMPORTANCE_VISIBLE,
            IMPORTANCE_PERCEPTIBLE,
            IMPORTANCE_CANT_SAVE_STATE,
            IMPORTANCE_SERVICE,
            IMPORTANCE_CACHED,
            IMPORTANCE_GONE,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Importance {
    }

    /**
     * Constant for {@link #importance}: This process is running the
     * foreground UI; that is, it is the thing currently at the top of the screen
     * that the user is interacting with.
     */
    public static final int IMPORTANCE_FOREGROUND = 100;

    /**
     * Constant for {@link #importance}: This process is running a foreground
     * service, for example to perform music playback even while the user is
     * not immediately in the app.  This generally indicates that the process
     * is doing something the user actively cares about.
     */
    public static final int IMPORTANCE_FOREGROUND_SERVICE = 125;

    /**
     * Constant for {@link #importance}: This process is running the foreground
     * UI, but the device is asleep so it is not visible to the user.  This means
     * the user is not really aware of the process, because they can not see or
     * interact with it, but it is quite important because it what they expect to
     * return to once unlocking the device.
     */
    public static final int IMPORTANCE_TOP_SLEEPING = 150;

    /**
     * Constant for {@link #importance}: This process is running something
     * that is actively visible to the user, though not in the immediate
     * foreground.  This may be running a window that is behind the current
     * foreground (so paused and with its state saved, not interacting with
     * the user, but visible to them to some degree); it may also be running
     * other services under the system's control that it inconsiders important.
     */
    public static final int IMPORTANCE_VISIBLE = 200;

    /**
     * Constant for {@link #importance}: {@link #IMPORTANCE_PERCEPTIBLE} had this wrong value
     * before {@link Build.VERSION_CODES#O}.  Since the {@link Build.VERSION_CODES#O} SDK,
     * the value of {@link #IMPORTANCE_PERCEPTIBLE} has been fixed.
     * <p>
     * <p>The system will return this value instead of {@link #IMPORTANCE_PERCEPTIBLE}
     * on Android versions below {@link Build.VERSION_CODES#O}.
     * <p>
     * <p>On Android version {@link Build.VERSION_CODES#O} and later, this value will still be
     * returned for apps with the target API level below {@link Build.VERSION_CODES#O}.
     * For apps targeting version {@link Build.VERSION_CODES#O} and later,
     * the correct value {@link #IMPORTANCE_PERCEPTIBLE} will be returned.
     */
    public static final int IMPORTANCE_PERCEPTIBLE_PRE_26 = 130;

    /**
     * Constant for {@link #importance}: This process is not something the user
     * is directly aware of, but is otherwise perceptible to them to some degree.
     */
    public static final int IMPORTANCE_PERCEPTIBLE = 230;

    /**
     * Constant for {@link #importance}: {@link #IMPORTANCE_CANT_SAVE_STATE} had
     * this wrong value
     * before {@link Build.VERSION_CODES#O}.  Since the {@link Build.VERSION_CODES#O} SDK,
     * the value of {@link #IMPORTANCE_CANT_SAVE_STATE} has been fixed.
     * <p>
     * <p>The system will return this value instead of {@link #IMPORTANCE_CANT_SAVE_STATE}
     * on Android versions below {@link Build.VERSION_CODES#O}.
     * <p>
     * <p>On Android version {@link Build.VERSION_CODES#O} after, this value will still be
     * returned for apps with the target API level below {@link Build.VERSION_CODES#O}.
     * For apps targeting version {@link Build.VERSION_CODES#O} and later,
     * the correct value {@link #IMPORTANCE_CANT_SAVE_STATE} will be returned.
     *
     * @hide
     */
    public static final int IMPORTANCE_CANT_SAVE_STATE_PRE_26 = 170;

    /**
     * Constant for {@link #importance}: This process is running an
     * application that can not save its state, and thus can't be killed
     * while in the background.
     *
     * @hide
     */
    public static final int IMPORTANCE_CANT_SAVE_STATE = 270;

    /**
     * Constant for {@link #importance}: This process is contains services
     * that should remain running.  These are background services apps have
     * started, not something the user is aware of, so they may be killed by
     * the system relatively freely (though it is generally desired that they
     * stay running as long as they want to).
     */
    public static final int IMPORTANCE_SERVICE = 300;

    /**
     * Constant for {@link #importance}: This process process contains
     * cached code that is expendable, not actively running any app components
     * we care about.
     */
    public static final int IMPORTANCE_CACHED = 400;

    /**
     * @deprecated Renamed to {@link #IMPORTANCE_CACHED}.
     */
    public static final int IMPORTANCE_BACKGROUND = IMPORTANCE_CACHED;

    /**
     * Constant for {@link #importance}: This process is empty of any
     * actively running code.
     *
     * @deprecated This value is no longer reported, use {@link #IMPORTANCE_CACHED} instead.
     */
    @Deprecated
    public static final int IMPORTANCE_EMPTY = 500;

    /**
     * Constant for {@link #importance}: This process does not exist.
     */
    public static final int IMPORTANCE_GONE = 1000;

    /**
     * Convert a proc state to the correspondent IMPORTANCE_* constant.  If the return value
     * will be passed to a client, use {@link #procStateToImportanceForClient}.
     *
     * @hide
     */
    public static @RunningAppProcessInfoCompat.Importance
    int procStateToImportance(int procState) {
        if (procState == PROCESS_STATE_NONEXISTENT) {
            return IMPORTANCE_GONE;
        } else if (procState >= PROCESS_STATE_HOME) {
            return IMPORTANCE_CACHED;
        } else if (procState >= PROCESS_STATE_SERVICE) {
            return IMPORTANCE_SERVICE;
        } else if (procState > PROCESS_STATE_HEAVY_WEIGHT) {
            return IMPORTANCE_CANT_SAVE_STATE;
        } else if (procState >= PROCESS_STATE_TRANSIENT_BACKGROUND) {
            return IMPORTANCE_PERCEPTIBLE;
        } else if (procState >= PROCESS_STATE_IMPORTANT_FOREGROUND) {
            return IMPORTANCE_VISIBLE;
        } else if (procState >= PROCESS_STATE_TOP_SLEEPING) {
            return IMPORTANCE_TOP_SLEEPING;
        } else if (procState >= PROCESS_STATE_FOREGROUND_SERVICE) {
            return IMPORTANCE_FOREGROUND_SERVICE;
        } else {
            return IMPORTANCE_FOREGROUND;
        }
    }

    /**
     * Convert a proc state to the correspondent IMPORTANCE_* constant for a client represented
     * by a given {@link Context}, with converting {@link #IMPORTANCE_PERCEPTIBLE}
     * and {@link #IMPORTANCE_CANT_SAVE_STATE} to the corresponding "wrong" value if the
     * client's target SDK < {@link Build.VERSION_CODES#O}.
     *
     * @hide
     */
    public static @RunningAppProcessInfoCompat.Importance
    int procStateToImportanceForClient(int procState,
                                       Context clientContext) {
        return procStateToImportanceForTargetSdk(procState,
                clientContext.getApplicationInfo().targetSdkVersion);
    }

    /**
     * See {@link #procStateToImportanceForClient}.
     *
     * @hide
     */
    public static @RunningAppProcessInfoCompat.Importance
    int procStateToImportanceForTargetSdk(int procState,
                                          int targetSdkVersion) {
        final int importance = procStateToImportance(procState);

        // For pre O apps, convert to the old, wrong values.
        if (targetSdkVersion < Build.VERSION_CODES.O) {
            switch (importance) {
                case IMPORTANCE_PERCEPTIBLE:
                    return IMPORTANCE_PERCEPTIBLE_PRE_26;
                case IMPORTANCE_CANT_SAVE_STATE:
                    return IMPORTANCE_CANT_SAVE_STATE_PRE_26;
            }
        }
        return importance;
    }

    /**
     * @hide
     */
    public static int importanceToProcState(@RunningAppProcessInfoCompat.Importance int importance) {
        if (importance == IMPORTANCE_GONE) {
            return PROCESS_STATE_NONEXISTENT;
        } else if (importance >= IMPORTANCE_CACHED) {
            return PROCESS_STATE_HOME;
        } else if (importance >= IMPORTANCE_SERVICE) {
            return PROCESS_STATE_SERVICE;
        } else if (importance > IMPORTANCE_CANT_SAVE_STATE) {
            return PROCESS_STATE_HEAVY_WEIGHT;
        } else if (importance >= IMPORTANCE_PERCEPTIBLE) {
            return PROCESS_STATE_TRANSIENT_BACKGROUND;
        } else if (importance >= IMPORTANCE_VISIBLE) {
            return PROCESS_STATE_IMPORTANT_FOREGROUND;
        } else if (importance >= IMPORTANCE_TOP_SLEEPING) {
            return PROCESS_STATE_TOP_SLEEPING;
        } else if (importance >= IMPORTANCE_FOREGROUND_SERVICE) {
            return PROCESS_STATE_FOREGROUND_SERVICE;
        } else {
            return PROCESS_STATE_BOUND_FOREGROUND_SERVICE;
        }
    }

    /**
     * The relative importance level that the system places on this process.
     * These constants are numbered so that "more important" values are
     * always smaller than "less important" values.
     */
    public @RunningAppProcessInfoCompat.Importance
    int importance;

    /**
     * An additional ordering within a particular {@link #importance}
     * category, providing finer-grained information about the relative
     * utility of processes within a category.  This number means nothing
     * except that a smaller values are more recently used (and thus
     * more important).  Currently an LRU value is only maintained for
     * the {@link #IMPORTANCE_CACHED} category, though others may
     * be maintained in the future.
     */
    public int lru;

    /**
     * Constant for {@link #importanceReasonCode}: nothing special has
     * been specified for the reason for this level.
     */
    public static final int REASON_UNKNOWN = 0;

    /**
     * Constant for {@link #importanceReasonCode}: one of the application's
     * content providers is being used by another process.  The pid of
     * the client process is in {@link #importanceReasonPid} and the
     * target provider in this process is in
     * {@link #importanceReasonComponent}.
     */
    public static final int REASON_PROVIDER_IN_USE = 1;

    /**
     * Constant for {@link #importanceReasonCode}: one of the application's
     * content providers is being used by another process.  The pid of
     * the client process is in {@link #importanceReasonPid} and the
     * target provider in this process is in
     * {@link #importanceReasonComponent}.
     */
    public static final int REASON_SERVICE_IN_USE = 2;

    /**
     * The reason for {@link #importance}, if any.
     */
    public int importanceReasonCode;

    /**
     * For the specified values of {@link #importanceReasonCode}, this
     * is the process ID of the other process that is a client of this
     * process.  This will be 0 if no other process is using this one.
     */
    public int importanceReasonPid;

    /**
     * For the specified values of {@link #importanceReasonCode}, this
     * is the name of the component that is being used in this process.
     */
    public ComponentName importanceReasonComponent;

    /**
     * When {@link #importanceReasonPid} is non-0, this is the importance
     * of the other pid. @hideAndDetach
     */
    public int importanceReasonImportance;

    /**
     * Current process state, as per PROCESS_STATE_* constants.
     *
     * @hide
     */
    public int processState;

    public RunningAppProcessInfoCompat() {
        importance = IMPORTANCE_FOREGROUND;
        importanceReasonCode = REASON_UNKNOWN;
        processState = PROCESS_STATE_IMPORTANT_FOREGROUND;
    }

    public RunningAppProcessInfoCompat(String pProcessName, int pPid, String pArr[]) {
        processName = pProcessName;
        pid = pPid;
        pkgList = pArr;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(processName);
        dest.writeInt(pid);
        dest.writeInt(uid);
        dest.writeStringArray(pkgList);
        dest.writeInt(this.flags);
        dest.writeInt(lastTrimLevel);
        dest.writeInt(importance);
        dest.writeInt(lru);
        dest.writeInt(importanceReasonCode);
        dest.writeInt(importanceReasonPid);
        ComponentName.writeToParcel(importanceReasonComponent, dest);
        dest.writeInt(importanceReasonImportance);
        dest.writeInt(processState);
    }

    public void readFromParcel(Parcel source) {
        processName = source.readString();
        pid = source.readInt();
        uid = source.readInt();
        pkgList = source.readStringArray();
        flags = source.readInt();
        lastTrimLevel = source.readInt();
        importance = source.readInt();
        lru = source.readInt();
        importanceReasonCode = source.readInt();
        importanceReasonPid = source.readInt();
        importanceReasonComponent = ComponentName.readFromParcel(source);
        importanceReasonImportance = source.readInt();
        processState = source.readInt();
    }

    public static final Parcelable.Creator<RunningAppProcessInfoCompat> CREATOR =
            new Parcelable.Creator<RunningAppProcessInfoCompat>() {
                public RunningAppProcessInfoCompat createFromParcel(Parcel source) {
                    return new RunningAppProcessInfoCompat(source);
                }

                public RunningAppProcessInfoCompat[] newArray(int size) {
                    return new RunningAppProcessInfoCompat[size];
                }
            };

    private RunningAppProcessInfoCompat(Parcel source) {
        readFromParcel(source);
    }
}
