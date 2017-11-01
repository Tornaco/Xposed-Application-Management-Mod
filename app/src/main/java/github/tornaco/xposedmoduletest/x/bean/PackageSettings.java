package github.tornaco.xposedmoduletest.x.bean;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.File;
import java.util.List;

import github.tornaco.xposedmoduletest.x.util.FileUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Builder
public class PackageSettings implements Parcelable {

    private String pkgName;
    private boolean verify;
    private int verifyPolicy;
    private List<ComponentName> verifyComponents;

    protected PackageSettings(Parcel in) {
        pkgName = in.readString();
        verify = in.readByte() != 0;
        verifyPolicy = in.readInt();
        verifyComponents = in.createTypedArrayList(ComponentName.CREATOR);
    }

    public static final Creator<PackageSettings> CREATOR = new Creator<PackageSettings>() {
        @Override
        public PackageSettings createFromParcel(Parcel in) {
            return new PackageSettings(in);
        }

        @Override
        public PackageSettings[] newArray(int size) {
            return new PackageSettings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pkgName);
        dest.writeByte((byte) (verify ? 1 : 0));
        dest.writeInt(verifyPolicy);
        dest.writeTypedList(verifyComponents);
    }

    public String toJsonString() {
        return new Gson().toJson(this);
    }

    public static PackageSettings fromJsonString(String js) {
        if (TextUtils.isEmpty(js)) {
            return null;
        }
        return new Gson().fromJson(js, PackageSettings.class);
    }

    public static PackageSettings readFrom(File path) {
        String js = FileUtil.readString(path.getPath());
        return fromJsonString(js);
    }

    public boolean deleteFrom(File dir) {
        return new File(constructPath(dir)).delete();
    }

    public boolean writeTo(File dir) {
        String str = toJsonString();
        String path = constructPath(dir);
        return FileUtil.writeString(str, path);
    }

    private String constructPath(File dir) {
        return dir.getPath() + File.separator + pkgName + ".json";
    }
}
