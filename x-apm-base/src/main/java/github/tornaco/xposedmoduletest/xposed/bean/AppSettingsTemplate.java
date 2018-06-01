package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.Parcelable;

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
public class AppSettingsTemplate implements Parcelable {

    private AppSettings appSettings;
    private AppOpsTemplate appOpsTemplate;

    private AppSettingsTemplate(Parcel in) {
        appSettings = in.readParcelable(AppSettings.class.getClassLoader());
        appOpsTemplate = in.readParcelable(AppOpsTemplate.class.getClassLoader());
    }

    public static final Creator<AppSettingsTemplate> CREATOR = new Creator<AppSettingsTemplate>() {
        @Override
        public AppSettingsTemplate createFromParcel(Parcel in) {
            return new AppSettingsTemplate(in);
        }

        @Override
        public AppSettingsTemplate[] newArray(int size) {
            return new AppSettingsTemplate[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(appSettings, flags);
        dest.writeParcelable(appOpsTemplate, flags);
    }
}
