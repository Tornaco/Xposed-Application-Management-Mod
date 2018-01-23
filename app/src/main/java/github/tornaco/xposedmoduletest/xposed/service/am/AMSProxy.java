package github.tornaco.xposedmoduletest.xposed.service.am;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by guohao4 on 2018/1/23.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@Getter
public class AMSProxy {

    private Object host;

    public boolean isAppRunning(String pkg) {
        return false;
    }
}
