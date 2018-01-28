package github.tornaco.xposedmoduletest.ui.activity.whyyouhere;

import android.content.Intent;

import github.tornaco.xposedmoduletest.R;

public class UserGuideActivityE extends UserGuideActivityA {
    @Override
    int getIntroMessage() {
        return R.string.user_notice_title_e;
    }

    @Override
    Intent getNextIntent() {
        return null;
    }
}
