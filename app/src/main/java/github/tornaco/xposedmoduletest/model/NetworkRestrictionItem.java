package github.tornaco.xposedmoduletest.model;

import ir.mirrajabi.searchdialog.core.Searchable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by guohao4 on 2017/12/5.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class NetworkRestrictionItem implements Searchable {

    private String packageName;
    private String appName;
    private int uid;
    private boolean systemApp;
    private boolean isRestrictedData;
    private boolean isRestrictedWifi;

    @Override
    public String getTitle() {
        return String.valueOf(getAppName());
    }
}
