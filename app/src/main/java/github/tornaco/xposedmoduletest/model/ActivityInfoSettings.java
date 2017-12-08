package github.tornaco.xposedmoduletest.model;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;

import ir.mirrajabi.searchdialog.core.Searchable;
import lombok.AllArgsConstructor;
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
public class ActivityInfoSettings implements Searchable {
    private ActivityInfo activityInfo;

    private boolean allowed;

    private String serviceLabel;
    private String displayName;

    @Override
    public String getTitle() {
        return simpleName();
    }

    @AllArgsConstructor
    @Getter
    public static class Export {
        private boolean allowed;
        private ComponentName componentName;
    }

    public boolean mayBeAdComponent() {
        return serviceLabel.contains("AD")
                || serviceLabel.contains("Ad");
    }


    public String simpleName() {
        String simpleName = getDisplayName();
        if (simpleName == null) {
            simpleName = getActivityInfo().packageName;
        }
        final int dot = simpleName.lastIndexOf(".");
        if (dot > 0) {
            return simpleName.substring(simpleName.lastIndexOf(".") + 1); // strip the package name
        }
        return simpleName;
    }
}
