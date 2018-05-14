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
public class SenseSetting implements Parcelable {

    private Sense sense;
    private String[] senseActionNames;

    private SenseSetting(Parcel in) {
        sense = in.readParcelable(Sense.class.getClassLoader());
        senseActionNames = in.readStringArray();
    }

    public static final Creator<SenseSetting> CREATOR = new Creator<SenseSetting>() {
        @Override
        public SenseSetting createFromParcel(Parcel in) {
            return new SenseSetting(in);
        }

        @Override
        public SenseSetting[] newArray(int size) {
            return new SenseSetting[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(sense, flags);
        dest.writeStringArray(senseActionNames);
    }
}
