package github.tornaco.xposedmoduletest.xposed.bean;

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
}
