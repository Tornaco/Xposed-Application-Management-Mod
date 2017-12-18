package github.tornaco.xposedmoduletest.model;

import ir.mirrajabi.searchdialog.core.Searchable;
import lombok.Data;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */
@Data
public class CommonPackageInfo implements Searchable {

    private String pkgName;
    private String appName;
    private int version;
    private boolean checked; // Used for selection.
    private boolean isSystemApp;
    private boolean isServiceOpAllowed;
    private boolean isAlarmOpAllowed;
    private boolean isWakelockOpAllowed;
    private boolean isDisabled; // Used for package viewer in comp list.

    @Override
    public String getTitle() {
        return getAppName();
    }

    public boolean isAllExtraPermDisabled() {
        return !isServiceOpAllowed() && !isAlarmOpAllowed() && !isWakelockOpAllowed();
    }

    public boolean isAllExtraPermAllowed() {
        return isServiceOpAllowed() && isWakelockOpAllowed() && isAlarmOpAllowed();
    }
}
