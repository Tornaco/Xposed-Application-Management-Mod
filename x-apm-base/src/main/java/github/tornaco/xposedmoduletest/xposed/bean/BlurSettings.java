package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import github.tornaco.xposedmoduletest.xposed.DefaultConfiguration;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
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
public class BlurSettings implements Parcelable, Cloneable {

    public static final float BITMAP_SCALE = DefaultConfiguration.BITMAP_SCALE;
    /* Initial blur radius. */
    public static final int BLUR_RADIUS = 16;
    public static final int BLUR_RADIUS_MAX = 25;

    public static final String KEY_SETTINGS = "tornaco.ag_blur";

    private boolean enabled = false;
    private int policy = XAppLockManager.BlurPolicy.BLUR_POLICY_UNKNOWN;
    private int radius = BLUR_RADIUS;
    private float scale = BITMAP_SCALE;

    protected BlurSettings(Parcel in) {
        enabled = in.readByte() != 0;
        policy = in.readInt();
        radius = in.readInt();
        scale = in.readFloat();
    }

    public static final Creator<BlurSettings> CREATOR = new Creator<BlurSettings>() {
        @Override
        public BlurSettings createFromParcel(Parcel in) {
            return new BlurSettings(in);
        }

        @Override
        public BlurSettings[] newArray(int size) {
            return new BlurSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeInt(policy);
        dest.writeInt(radius);
        dest.writeFloat(scale);
    }

    public String formatJson() {
        return new Gson().toJson(this);
    }

    public static BlurSettings from(String str) {
        return new Gson().fromJson(str, BlurSettings.class);
    }

    public BlurSettings duplicate() {
        try {
            return (BlurSettings) this.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
