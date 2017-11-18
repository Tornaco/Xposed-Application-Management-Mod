package github.tornaco.xposedmoduletest.model;

import android.content.pm.ServiceInfo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ServiceInfoSettings {
    private ServiceInfo serviceInfo;

    private String serviceLabel;
    private String displayName;

    private boolean allowed;
}
