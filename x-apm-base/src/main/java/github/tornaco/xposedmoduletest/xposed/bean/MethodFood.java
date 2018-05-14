package github.tornaco.xposedmoduletest.xposed.bean;

import com.google.gson.Gson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by Tornaco on 2018/3/13.
 */

@ToString
@Builder
@Getter
@AllArgsConstructor
public class MethodFood {

    private String targetPackage;
    private String className;
    private String methodName;
    private String[] paramClasses;
    private String rtn;

    public String toJson() {
        return new Gson().toJson(this);
    }

    public static MethodFood fromJson(String js) {
        return new Gson().fromJson(js, MethodFood.class);
    }
}
