package github.tornaco.xposedmoduletest.ui.activity.extra;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;

/**
 * Created by guohao4 on 2017/11/22.
 * Email: Tornaco@163.com
 */

public class PayListBrowserActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        setupToolbar();
        showHomeAsUp();
        replaceV4(R.id.container, onCreateFragment(), null, false);
    }

    Fragment onCreateFragment() {
        return new PayListBrowserFragment();
    }
}
