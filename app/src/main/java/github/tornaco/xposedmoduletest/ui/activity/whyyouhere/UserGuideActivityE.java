package github.tornaco.xposedmoduletest.ui.activity.whyyouhere;

import android.content.Intent;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.AppSettings;

public class UserGuideActivityE extends UserGuideActivityA {
    @Override
    int getIntroMessage() {
        return R.string.user_notice_title_e;
    }

    @Override
    Intent getNextIntent() {
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppSettings.setShowInfo(this, "USER_GUIDES_AIO", false);
    }
}
