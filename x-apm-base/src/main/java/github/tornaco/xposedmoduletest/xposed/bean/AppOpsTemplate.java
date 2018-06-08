package github.tornaco.xposedmoduletest.xposed.bean;

import android.os.Parcel;
import android.os.ParcelUuid;
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
public class AppOpsTemplate implements Parcelable {

    private ParcelUuid opsTemplateDummyPackageName;

    private AppOpsTemplate(Parcel in) {
        opsTemplateDummyPackageName = in.readParcelable(ParcelUuid.class.getClassLoader());
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
        dest.writeParcelable(opsTemplateDummyPackageName, flags);
    }
}
