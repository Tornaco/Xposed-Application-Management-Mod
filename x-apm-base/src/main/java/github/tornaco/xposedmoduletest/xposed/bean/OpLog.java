package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/1/16.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class OpLog implements Parcelable {

    private int code;
    private int mode;
    private long when;
    private String packageName;
    private long times;
    private String[] payload;

    protected OpLog(Parcel in) {
        code = in.readInt();
        mode = in.readInt();
        when = in.readLong();
        packageName = in.readString();
        times = in.readLong();
        payload = in.readStringArray();
    }

    public static final Creator<OpLog> CREATOR = new Creator<OpLog>() {
        @Override
        public OpLog createFromParcel(Parcel in) {
            return new OpLog(in);
        }

        @Override
        public OpLog[] newArray(int size) {
            return new OpLog[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeInt(mode);
        dest.writeLong(when);
        dest.writeString(packageName);
        dest.writeLong(times);
        dest.writeStringArray(payload);
    }
}
