package github.tornaco.xposedmoduletest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/12/5.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NetworkRestrictionItem {

    private String packageName;
    private String appName;
    private int uid;
    private boolean systemApp;
    private boolean isRestrictedData;
    private boolean isRestrictedWifi;

    @Override
    public String toString() {
        return appName == null ? packageName : appName;
    }
}
