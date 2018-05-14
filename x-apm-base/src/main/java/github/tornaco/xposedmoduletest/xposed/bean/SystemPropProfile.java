package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Data;

/**
 * Created by Tornaco on 2018/5/4 12:48.
 * God bless no bug!
 */
@Data
public class SystemPropProfile implements Parcelable {

    private String profileName;
    private String profileId;
    private SystemProp systemProp;

    private SystemPropProfile(Parcel in) {
        profileName = in.readString();
        profileId = in.readString();
        systemProp = in.readParcelable(SystemProp.class.getClassLoader());
    }

    public static final Creator<SystemPropProfile> CREATOR = new Creator<SystemPropProfile>() {
        @Override
        public SystemPropProfile createFromParcel(Parcel in) {
            return new SystemPropProfile(in);
        }

        @Override
        public SystemPropProfile[] newArray(int size) {
            return new SystemPropProfile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(profileName);
        dest.writeString(profileId);
        dest.writeParcelable(systemProp, flags);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    // Nullable.
    public static SystemPropProfile fromJson(String json) {
        try {
            return new Gson().fromJson(json, SystemPropProfile.class);
        } catch (Throwable e) {
            Log.e(XposedLog.TAG_DANGER, "SystemPropProfile fail parse json: " + Log.getStackTraceString(e));
            return null;
        }
    }
}
