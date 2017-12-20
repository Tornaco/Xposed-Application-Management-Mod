package github.tornaco.xposedmoduletest.xposed.bean;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */
@ToString
public class RunningServiceInfoCompat extends CommonPackageInfo implements Parcelable {
    /**
     * The service component.
     */
    public ComponentName service;

    /**
     * If non-zero, this is the process the service is running in.
     */
    public int pid;

    /**
     * The UID that owns this service.
     */
    public int uid;

    /**
     * The name of the process this service runs in.
     */
    public String process;

    /**
     * Set to true if the service has asked to run as a foreground process.
     */
    public boolean foreground;

    /**
     * The time when the service was first made active, either by someone
     * starting or binding to it.  This
     * is in units of {@link android.os.SystemClock#elapsedRealtime()}.
     */
    public long activeSince;

    /**
     * Set to true if this service has been explicitly started.
     */
    public boolean started;

    /**
     * Number of clients connected to the service.
     */
    public int clientCount;

    /**
     * Number of times the service's process has crashed while the service
     * is running.
     */
    public int crashCount;

    /**
     * The time when there was last activity in the service (either
     * explicit requests to start it or clients binding to it).  This
     * is in units of {@link android.os.SystemClock#uptimeMillis()}.
     */
    public long lastActivityTime;

    /**
     * If non-zero, this service is not currently running, but scheduled to
     * restart at the given time.
     */
    public long restarting;

    /**
     * Bit for {@link #flags}: set if this service has been
     * explicitly started.
     */
    public static final int FLAG_STARTED = 1 << 0;

    /**
     * Bit for {@link #flags}: set if the service has asked to
     * run as a foreground process.
     */
    public static final int FLAG_FOREGROUND = 1 << 1;

    /**
     * Bit for {@link #flags}: set if the service is running in a
     * core system process.
     */
    public static final int FLAG_SYSTEM_PROCESS = 1 << 2;

    /**
     * Bit for {@link #flags}: set if the service is running in a
     * persistent process.
     */
    public static final int FLAG_PERSISTENT_PROCESS = 1 << 3;

    /**
     * Running flags.
     */
    public int flags;

    /**
     * For special services that are bound to by system code, this is
     * the package that holds the binding.
     */
    public String clientPackage;

    /**
     * For special services that are bound to by system code, this is
     * a string resource providing a user-visible label for who the
     * client is.
     */
    public int clientLabel;

    public RunningServiceInfoCompat() {
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ComponentName.writeToParcel(service, dest);
        dest.writeInt(pid);
        dest.writeInt(uid);
        dest.writeString(process);
        dest.writeInt(foreground ? 1 : 0);
        dest.writeLong(activeSince);
        dest.writeInt(started ? 1 : 0);
        dest.writeInt(clientCount);
        dest.writeInt(crashCount);
        dest.writeLong(lastActivityTime);
        dest.writeLong(restarting);
        dest.writeInt(this.flags);
        dest.writeString(clientPackage);
        dest.writeInt(clientLabel);
    }

    public void readFromParcel(Parcel source) {
        service = ComponentName.readFromParcel(source);
        pid = source.readInt();
        uid = source.readInt();
        process = source.readString();
        foreground = source.readInt() != 0;
        activeSince = source.readLong();
        started = source.readInt() != 0;
        clientCount = source.readInt();
        crashCount = source.readInt();
        lastActivityTime = source.readLong();
        restarting = source.readLong();
        flags = source.readInt();
        clientPackage = source.readString();
        clientLabel = source.readInt();
    }

    public static final Parcelable.Creator<RunningServiceInfoCompat> CREATOR
            = new Parcelable.Creator<RunningServiceInfoCompat>() {
        public RunningServiceInfoCompat createFromParcel(Parcel source) {
            return new RunningServiceInfoCompat(source);
        }


        public RunningServiceInfoCompat[] newArray(int size) {
            return new RunningServiceInfoCompat[size];
        }
    };

    private RunningServiceInfoCompat(Parcel source) {
        readFromParcel(source);
    }
}
