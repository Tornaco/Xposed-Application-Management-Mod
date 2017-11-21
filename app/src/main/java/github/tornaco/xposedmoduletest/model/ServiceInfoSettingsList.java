package github.tornaco.xposedmoduletest.model;

import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */
@Getter
@ToString
@AllArgsConstructor
public class ServiceInfoSettingsList {

    private static final int VERSION = -1;

    private int version = VERSION;
    private String packageName;
    private List<ServiceInfoSettings.Export> exports;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Nullable
    public static ServiceInfoSettingsList fromJson(String jsonStr) {
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, ServiceInfoSettingsList.class);
    }
}
