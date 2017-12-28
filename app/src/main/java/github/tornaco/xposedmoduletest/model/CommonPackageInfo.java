package github.tornaco.xposedmoduletest.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonPackageInfo {

    private String pkgName;
    private String appName;
    private int version;
    private boolean checked; // Used for selection.
    private boolean isSystemApp;
    private boolean isServiceOpAllowed;
    private boolean isAlarmOpAllowed;
    private boolean isWakelockOpAllowed;
    private boolean isDisabled; // Used for package viewer in comp list.
    private String[] payload;

    public boolean isAllExtraPermDisabled() {
        return !isServiceOpAllowed() && !isAlarmOpAllowed() && !isWakelockOpAllowed();
    }

    public boolean isAllExtraPermAllowed() {
        return isServiceOpAllowed() && isWakelockOpAllowed() && isAlarmOpAllowed();
    }

    public int getOpDistance() {
        int d = 0;
        if (isServiceOpAllowed()) d++;
        if (isAlarmOpAllowed()) d++;
        if (isWakelockOpAllowed()) d++;
        return d;
    }

    @Override
    public String toString() {
        return getAppName();
    }
}
