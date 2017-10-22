package github.tornaco.xposedmoduletest.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.ICallback;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.x.XMode;

/**
 * Created by guohao4 on 2017/10/21.
 * Email: Tornaco@163.com
 */

public class LockedActivity extends WithWithCustomTabActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (showLockOnCreate()) {
            AppStartNoter startNoter = new AppStartNoter(new Handler(), this);
            startNoter.note(null, getPackageName(), getString(R.string.app_name),
                    new ICallback.Stub() {
                        @Override
                        public void onRes(int res, int flags) throws RemoteException {
                            if (res != XMode.MODE_ALLOWED) {
                                onModeIgnoreOrDeny();
                            }
                        }
                    });
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
