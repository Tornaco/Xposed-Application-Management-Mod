package github.tornaco.xposedmoduletest.xposed.bean;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/12/5.
 * Email: Tornaco@163.com
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NetworkRestriction {
    private int restrictPolicy;
    private int uid;

    public static NetworkRestriction from(String json) {
        return new Gson().fromJson(json, NetworkRestriction.class);
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
