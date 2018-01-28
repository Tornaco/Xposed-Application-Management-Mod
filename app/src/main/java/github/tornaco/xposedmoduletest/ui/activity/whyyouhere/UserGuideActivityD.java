package github.tornaco.xposedmoduletest.ui.activity.whyyouhere;

import android.content.Intent;

import github.tornaco.xposedmoduletest.R;

public class UserGuideActivityD extends UserGuideActivityA {
    @Override
    int getIntroMessage() {
        return R.string.user_notice_title_d;
    }

    @Override
    Intent getNextIntent() {
        return new Intent(this, UserGuideActivityE.class);
    }
}
