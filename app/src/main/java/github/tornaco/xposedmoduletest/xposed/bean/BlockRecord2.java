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

    private String pkgName;
    private String callerPkgName;
    @Deprecated
    private String appName;
    private long timeWhen;
    private long howManyTimes;
    private String reason;

    protected BlockRecord2(Parcel in) {
        pkgName = in.readString();
        callerPkgName = in.readString();
        appName = in.readString();
        timeWhen = in.readLong();
        howManyTimes = in.readLong();
        reason = in.readString();
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
        dest.writeLong(howManyTimes);
        dest.writeString(reason);
    }
}