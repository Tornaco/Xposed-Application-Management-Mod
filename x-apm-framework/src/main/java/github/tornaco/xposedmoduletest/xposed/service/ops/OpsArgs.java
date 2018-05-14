package github.tornaco.xposedmoduletest.xposed.service.ops;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2018/2/2.
 * Email: Tornaco@163.com
 */
@Builder
@Setter
@Getter
@ToString
public class OpsArgs {
    private String pkgName;
    private int code;
    private boolean remember;
    private OpsVerifyCallback opsVerifyCallback;
}
