package github.tornaco.xposedmoduletest.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/11/30.
 * Email: Tornaco@163.com
 */
@Getter
@Builder
@ToString
public class RemoteConfig {
    private boolean donate;
    private boolean shutdown;
}
