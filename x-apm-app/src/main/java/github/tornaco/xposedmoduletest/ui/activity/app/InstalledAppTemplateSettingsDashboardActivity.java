package github.tornaco.xposedmoduletest.ui.activity.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppSettings;

/**
 * Created by guohao4 on 2017/11/2.
 * Email: Tornaco@163.com
 */

public class InstalledAppTemplateSettingsDashboardActivity
        extends PerAppSettingsDashboardActivity {

    public static void start(Context context, String pkg) {
        Intent starter = new Intent(context, InstalledAppTemplateSettingsDashboardActivity.class);
        starter.putExtra("pkg_name", pkg);
        context.startActivity(starter);
    }

    private AppSettings mAppSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_per_app_template);
    }

    @Override
    AppSettings onRetrieveAppSettings(String pkg) {
        mAppSettings = XAPMManager.get().getAppInstalledAutoApplyTemplate();
        if (mAppSettings == null) mAppSettings = AppSettings.builder()
                .boot(true)
                .start(true)
                .trk(true)
                .rfk(true)
                .lk(true)
                .build();
        return mAppSettings;
    }

    @Override
    void onFabClick() {
        XAPMManager.get().setAppInstalledAutoApplyTemplate(mAppSettings);
        super.onFabClick();
    }
}