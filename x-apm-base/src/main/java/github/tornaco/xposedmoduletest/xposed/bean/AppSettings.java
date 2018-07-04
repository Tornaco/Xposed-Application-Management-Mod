package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */
@Builder
@Getter
@ToString
@AllArgsConstructor
@Setter
public class AppSettings implements Parcelable {

    private String pkgName;
    private String appName;
    private int version;

    private int appLevel;

    private boolean isDisabled;

    private boolean boot, start, lk, rfk, trk, lazy;
    private boolean applock, blur, uninstall, privacy;
    private boolean green;

    private boolean wakeLock, service, alarm;

    private String appOpsTemplateId;

    protected AppSettings(Parcel in) {
        pkgName = in.readString();
        appName = in.readString();
        version = in.readInt();
        appLevel = in.readInt();
        isDisabled = in.readByte() != 0;
        boot = in.readByte() != 0;
        start = in.readByte() != 0;
        lk = in.readByte() != 0;
        rfk = in.readByte() != 0;
        trk = in.readByte() != 0;
        lazy = in.readByte() != 0;
        applock = in.readByte() != 0;
        blur = in.readByte() != 0;
        uninstall = in.readByte() != 0;
        privacy = in.readByte() != 0;
        green = in.readByte() != 0;
        wakeLock = in.readByte() != 0;
        service = in.readByte() != 0;
        alarm = in.readByte() != 0;

        appOpsTemplateId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pkgName);
        dest.writeString(appName);
        dest.writeInt(version);
        dest.writeInt(appLevel);
        dest.writeByte((byte) (isDisabled ? 1 : 0));
        dest.writeByte((byte) (boot ? 1 : 0));
        dest.writeByte((byte) (start ? 1 : 0));
        dest.writeByte((byte) (lk ? 1 : 0));
        dest.writeByte((byte) (rfk ? 1 : 0));
        dest.writeByte((byte) (trk ? 1 : 0));
        dest.writeByte((byte) (lazy ? 1 : 0));
        dest.writeByte((byte) (applock ? 1 : 0));
        dest.writeByte((byte) (blur ? 1 : 0));
        dest.writeByte((byte) (uninstall ? 1 : 0));
        dest.writeByte((byte) (privacy ? 1 : 0));
        dest.writeByte((byte) (green ? 1 : 0));
        dest.writeByte((byte) (wakeLock ? 1 : 0));
        dest.writeByte((byte) (service ? 1 : 0));
        dest.writeByte((byte) (alarm ? 1 : 0));

        dest.writeString(appOpsTemplateId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppSettings> CREATOR = new Creator<AppSettings>() {
        @Override
        public AppSettings createFromParcel(Parcel in) {
            return new AppSettings(in);
        }

        @Override
        public AppSettings[] newArray(int size) {
            return new AppSettings[size];
        }
    };

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static AppSettings fromJson(String js) {
        AppSettings def = AppSettings.builder()
                .boot(true)
                .start(true)
                .trk(true)
                .rfk(true)
                .lk(true)
                .build();
        if (js == null) return def;
        try {
            return new Gson().fromJson(js, AppSettings.class);
        } catch (Throwable e) {
            return def;
        }
    }
}
