package github.tornaco.xposedmoduletest.xposed.service.doze;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/1/3.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@Getter
@ToString
public class BatterState {
    private int status;
    private int level;
}
