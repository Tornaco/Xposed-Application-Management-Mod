package github.tornaco.xposedmoduletest.ui.activity.helper;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Delegate;

/**
 * Created by guohao4 on 2017/12/19.
 * Email: Tornaco@163.com
 */
@ToString
@Getter
public class RunningServiceInfoDisplay extends CommonPackageInfo {
    @Delegate
    private RunningState.MergedItem mergedItem;

    public RunningServiceInfoDisplay(RunningState.MergedItem mergedItem) {
        this.mergedItem = mergedItem;
        String appName =
                mergedItem.mDisplayLabel == null ? mergedItem.mLabel : mergedItem.mDisplayLabel.toString();
        setAppName(appName);
        String pkgName = mergedItem.mPackageInfo
                == null ? mergedItem.mProcess.mProcessName : mergedItem.mPackageInfo.packageName;
        setPkgName(pkgName);
    }
}