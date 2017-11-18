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
@AllArgsConstructor
@ToString
public class ReceiverInfoSettingsList {
    private List<ReceiverInfoSettings.Export> exports;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Nullable
    public static ReceiverInfoSettingsList fromJson(String jsonStr) {
        Gson gson = new Gson();
        return gson.fromJson(jsonStr, ReceiverInfoSettingsList.class);
    }
}