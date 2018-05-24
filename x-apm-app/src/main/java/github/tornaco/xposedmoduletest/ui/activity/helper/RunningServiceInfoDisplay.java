package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */
@ToString
@Getter
class RunningServiceInfoDisplay extends CommonPackageInfo {
    @Delegate
    private RunningState.MergedItem mergedItem;

    RunningServiceInfoDisplay(Context context, RunningState.MergedItem mergedItem) {
        this.mergedItem = mergedItem;

        PackageManager pm = context.getPackageManager();

        if (mergedItem.mPackageInfo == null) {
            // Items for background processes don't normally load
            // their labels for performance reasons.  Do it now.
            if (mergedItem.mProcess != null) {
                mergedItem.mProcess.ensureLabel(pm);
                mergedItem.mPackageInfo = mergedItem.mProcess.mPackageInfo;
                mergedItem.mDisplayLabel = mergedItem.mProcess.mDisplayLabel;
            }
        }

        setAppName(String.valueOf(mergedItem.mDisplayLabel));

        String pkgName = mergedItem.mPackageInfo.packageName;
        setPkgName(pkgName);

        setAppIdle(XAPMManager.get().isAppInactive(pkgName, UserHandle.USER_CURRENT));
    }
}