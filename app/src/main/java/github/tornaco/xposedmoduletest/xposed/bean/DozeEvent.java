package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/1/3.
 * Email: Tornaco@163.com
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DozeEvent implements Parcelable, Cloneable {

    public static final int RESULT_SUCCESS = 0x1;
    public static final int RESULT_FAIL = 0x2;
    public static final int RESULT_UNKNOWN = 0x3;

    public static final int FAIL_UNKNOWN = 0x100;
    public static final int FAIL_DEVICE_INTERACTIVE = 0x101;
    public static final int FAIL_POWER_CHARGING = 0x102;
    public static final int FAIL_RETRY_TIMEOUT = 0x103;

    private int result;
    private int failCode;
    private long startTimeMills, enterTimeMills, endTimeMills;

    private DozeEvent(Parcel in) {
        result = in.readInt();
        failCode = in.readInt();
        startTimeMills = in.readLong();
        enterTimeMills = in.readLong();
        endTimeMills = in.readLong();
    }

    public static final Creator<DozeEvent> CREATOR = new Creator<DozeEvent>() {
        @Override
        public DozeEvent createFromParcel(Parcel in) {
            return new DozeEvent(in);
        }

        @Override
        public DozeEvent[] newArray(int size) {
            return new DozeEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(result);
        dest.writeInt(failCode);
        dest.writeLong(startTimeMills);
        dest.writeLong(enterTimeMills);
        dest.writeLong(endTimeMills);
    }

    public DozeEvent duplicate() {
        try {
            return (DozeEvent) clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
