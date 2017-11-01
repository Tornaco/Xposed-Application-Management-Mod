package github.tornaco.xposedmoduletest.x.bean;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VerifySettings implements Parcelable {

    private boolean verifyOnScreenOff;
    private boolean verifyOnHome;
    private boolean verifyOnAppSwitch;

    protected VerifySettings(Parcel in) {
        verifyOnScreenOff = in.readByte() != 0;
        verifyOnHome = in.readByte() != 0;
        verifyOnAppSwitch = in.readByte() != 0;
    }

    public static final Creator<VerifySettings> CREATOR = new Creator<VerifySettings>() {
        @Override
        public VerifySettings createFromParcel(Parcel in) {
            return new VerifySettings(in);
        }

        @Override
        public VerifySettings[] newArray(int size) {
            return new VerifySettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (verifyOnScreenOff ? 1 : 0));
        dest.writeByte((byte) (verifyOnHome ? 1 : 0));
        dest.writeByte((byte) (verifyOnAppSwitch ? 1 : 0));
    }
}
