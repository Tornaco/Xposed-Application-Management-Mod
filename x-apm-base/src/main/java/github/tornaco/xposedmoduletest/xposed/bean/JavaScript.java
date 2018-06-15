package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by Tornaco on 2018/6/15 12:33.
 * This file is writen for project X-APM at host guohao4.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class JavaScript implements Parcelable {

    private String id;
    private String script;
    private String alias;
    private long createdAt;

    private JavaScript(Parcel in) {
        id = in.readString();
        script = in.readString();
        alias = in.readString();
        createdAt = in.readLong();
    }

    public static final Creator<JavaScript> CREATOR = new Creator<JavaScript>() {
        @Override
        public JavaScript createFromParcel(Parcel in) {
            return new JavaScript(in);
        }

        @Override
        public JavaScript[] newArray(int size) {
            return new JavaScript[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(script);
        dest.writeString(alias);
        dest.writeLong(createdAt);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    // Nullable.
    public static JavaScript fromJson(String json) {
        try {
            return new Gson().fromJson(json, JavaScript.class);
        } catch (Throwable e) {
            Log.e(XposedLog.TAG_DANGER, "JavaScript fail parse json: " + Log.getStackTraceString(e));
            return null;
        }
    }
}
