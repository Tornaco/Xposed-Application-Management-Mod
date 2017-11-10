package github.tornaco.xposedmoduletest.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

/**
 * Created by guohao4 on 2017/10/21.
 * Email: Tornaco@163.com
 */

public class LockedActivity extends WithWithCustomTabActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (showLockOnCreate()) {

        }
    }

    protected boolean showLockOnCreate() {
        return false;
    }

    protected void onModeIgnoreOrDeny() {
        Logger.v("onModeIgnoreOrDeny");
        finish();
    }
}
