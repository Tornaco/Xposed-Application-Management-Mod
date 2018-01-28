package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.util.GsonUtil;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */
@Getter
@ToString
@AllArgsConstructor
@Setter
public class OpsSettings implements Parcelable {

    private static final int[] DEFAULT_MODE = AppOpsManagerCompat.getDefaultModes();

    private int[] modes;

    private OpsSettings(Parcel in) {
        modes = in.createIntArray();
    }

    public static final Creator<OpsSettings> CREATOR = new Creator<OpsSettings>() {
        @Override
        public OpsSettings createFromParcel(Parcel in) {
            return new OpsSettings(in);
        }

        @Override
        public OpsSettings[] newArray(int size) {
            return new OpsSettings[size];
        }
    };

    public String toJson() {
        return GsonUtil.getGson().toJson(this);
    }

    public static OpsSettings fromJson(String js) {
        OpsSettings def = new OpsSettings(DEFAULT_MODE);
        if (js == null) return def;
        try {
            return GsonUtil.getGson().fromJson(js, OpsSettings.class);
        } catch (Throwable e) {
            XposedLog.wtf("Fail from json: " + Log.getStackTraceString(e));
            return def;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(modes);
    }
}
