package github.tornaco.xposedmoduletest.xposed.bean;

import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */
@ToString
@Setter
@SuppressLint("UseSparseArrays")
public class AppOpsTemplate implements Parcelable {

    private static final String DEFAULT_ALIAS = "OPS TEMPLATE";

    @Getter
    private String id;

    private Map<Integer, Integer> opsSettings;

    @Getter
    private String alias;
    @Getter
    private long createdAtMills;

    private AppOpsTemplate(Parcel in) {
        id = in.readString();
        opsSettings = new Gson().fromJson(in.readString(), new TypeToken<HashMap<Integer, Integer>>() {
            // Noop.
        }.getType());
        alias = in.readString();
        createdAtMills = in.readLong();
    }

    public AppOpsTemplate() {
        id = UUID.randomUUID().toString();
        opsSettings = new HashMap<>();
        createdAtMills = System.currentTimeMillis();
        alias = DEFAULT_ALIAS;
    }

    public void setMode(int code, int mode) {
        // Default we return allow.
        if (opsSettings == null) {
            Log.e(XposedLog.TAG, "Try to set mode but opsSettings is null");
            return;
        }
        opsSettings.put(code, mode);
    }

    public int getMode(int code) {
        // Default we return allow.
        if (opsSettings == null || opsSettings.get(code) == null) {
            Log.e(XposedLog.TAG, "Try to get mode but opsSettings is null or no code in it");
            return XAppOpsManager.MODE_ALLOWED;
        }
        return opsSettings.get(code);
    }

    public static final Creator<AppOpsTemplate> CREATOR = new Creator<AppOpsTemplate>() {
        @Override
        public AppOpsTemplate createFromParcel(Parcel in) {
            return new AppOpsTemplate(in);
        }

        @Override
        public AppOpsTemplate[] newArray(int size) {
            return new AppOpsTemplate[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        String opsJson = new Gson().toJson(opsSettings);
        dest.writeString(opsJson);
        dest.writeString(alias);
        dest.writeLong(createdAtMills);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppOpsTemplate template = (AppOpsTemplate) o;
        return Objects.equals(id, template.id) &&
                Objects.equals(opsSettings, template.opsSettings);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @Nullable
    public static AppOpsTemplate fromJson(String json) {
        try {
            return new Gson().fromJson(json, AppOpsTemplate.class);
        } catch (Throwable e) {
            Log.e(XposedLog.TAG, "AppOpsTemplate error from json: " + Log.getStackTraceString(e));
            return null;
        }
    }
}
