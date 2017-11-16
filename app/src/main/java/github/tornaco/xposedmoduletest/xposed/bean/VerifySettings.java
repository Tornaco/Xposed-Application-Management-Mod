package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class VerifySettings implements Parcelable, Cloneable {

    public static final String KEY_SETTINGS = "tornaco.ag_verify";

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

    public String formatJson() {
        return new Gson().toJson(this);
    }

    public static VerifySettings from(String str) {
        return new Gson().fromJson(str, VerifySettings.class);
    }

    public VerifySettings duplicate() {
        try {
            return (VerifySettings) this.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
