package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@ToString
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BlockRecord2 implements Parcelable {

    public static final int TYPE_ACTIVITY = 0x1;
    public static final int TYPE_SERVICE = 0x2;
    public static final int TYPE_BROADCAST = 0x3;
    public static final int TYPE_CONTENT_PROVIDER = 0x4;
    public static final int TYPE_UNKNOWN = 0;

    private String pkgName;
    private String callerPkgName;
    @Deprecated
    private String appName;
    private long timeWhen;
    private long howManyTimesBlocked;
    private long howManyTimesAllowed;
    private String reason;
    private boolean block; // Is this request blocked or allowed.

    @Builder.Default
    private int type = TYPE_UNKNOWN;

    public static String decodeType(int type) {
        switch (type) {
            case TYPE_ACTIVITY:
                return "Activity";
            case TYPE_BROADCAST:
                return "Broadcast";
            case TYPE_SERVICE:
                return "Service";
            case TYPE_CONTENT_PROVIDER:
                return "ContentProvider";
            default:
                return "Unknown";
        }
    }

    protected BlockRecord2(Parcel in) {
        pkgName = in.readString();
        callerPkgName = in.readString();
        appName = in.readString();
        timeWhen = in.readLong();
        howManyTimesBlocked = in.readLong();
        howManyTimesAllowed = in.readLong();
        reason = in.readString();
        block = in.readByte() != 0;
        type = in.readInt();
    }

    public static final Creator<BlockRecord2> CREATOR = new Creator<BlockRecord2>() {
        @Override
        public BlockRecord2 createFromParcel(Parcel in) {
            return new BlockRecord2(in);
        }

        @Override
        public BlockRecord2[] newArray(int size) {
            return new BlockRecord2[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pkgName);
        dest.writeString(callerPkgName);
        dest.writeString(appName);
        dest.writeLong(timeWhen);
        dest.writeLong(howManyTimesBlocked);
        dest.writeLong(howManyTimesAllowed);
        dest.writeString(reason);
        dest.writeByte((byte) (block ? 1 : 0));
        dest.writeInt(type);
    }
}