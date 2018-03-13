package github.tornaco.xposedmoduletest.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/12/28.
 * Email: Tornaco@163.com
 */
@Getter
@ToString
public class Sense implements Parcelable {

    private SenseType type;
    private String[] payload;

    private Sense(Parcel in) {
        payload = in.readStringArray();
        type = SenseType.valueOf(in.readString());
    }

    public static final Creator<Sense> CREATOR = new Creator<Sense>() {
        @Override
        public Sense createFromParcel(Parcel in) {
            return new Sense(in);
        }

        @Override
        public Sense[] newArray(int size) {
            return new Sense[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(payload);
        dest.writeString(type.name());
    }
}
