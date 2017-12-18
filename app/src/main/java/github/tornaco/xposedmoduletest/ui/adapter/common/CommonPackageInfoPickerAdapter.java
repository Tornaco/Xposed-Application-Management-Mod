package github.tornaco.xposedmoduletest.ui.adapter.common;

import android.content.Context;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public class CommonPackageInfoPickerAdapter extends CommonPackageInfoAdapter {

    public CommonPackageInfoPickerAdapter(Context context) {
        super(context);
        setChoiceMode(true);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }
}
