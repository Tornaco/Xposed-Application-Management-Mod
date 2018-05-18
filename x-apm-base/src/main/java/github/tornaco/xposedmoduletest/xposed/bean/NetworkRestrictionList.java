package github.tornaco.xposedmoduletest.xposed.bean;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

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
public class NetworkRestrictionList {

    private List<NetworkRestriction> restrictionList;

    public String toJson() {
        if (restrictionList == null) restrictionList = new ArrayList<>();
        return new Gson().toJson(this);
    }

    public static NetworkRestrictionList fromJson(String json) {
        return new Gson().fromJson(json, NetworkRestrictionList.class);
    }
}
