package github.tornaco.xposedmoduletest.xposed.submodules;

import android.content.ComponentName;

import com.google.common.collect.Sets;

import java.util.Set;

import de.robv.android.xposed.XposedHelpers;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public abstract class AndroidSubModule extends AbsSubModule {
    static final String PKG_NAME_ANDROID = "android";

    @Override
    public Set<String> getInterestedPackages() {
        return Sets.newHashSet(PKG_NAME_ANDROID);
    }

    protected String getPackageNameFromTaskRecord(Object taskRecord) {
        if (taskRecord == null) return null;
        String pkgName;
        ComponentName componentName;
        Object realActivityObj = XposedHelpers.getObjectField(taskRecord, "realActivity");
        if (realActivityObj != null) {
            componentName = (ComponentName) realActivityObj;
            pkgName = componentName.getPackageName();
        } else {
            // Using aff instead of PKG.
            pkgName = (String) XposedHelpers.getObjectField(taskRecord, "affinity");
        }
        return pkgName;
    }
}
