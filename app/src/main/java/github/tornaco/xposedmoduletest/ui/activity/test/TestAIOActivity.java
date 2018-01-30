package github.tornaco.xposedmoduletest.ui.activity.test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2018/1/23.
 * Email: Tornaco@163.com
 */

public class TestAIOActivity extends BaseActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, TestAIOActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int getUserSetThemeResId(Themes themes) {
        return themes.getThemeStyleRes();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_aio_only_dev);

        findViewById(R.id.test_btn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        XAshmanManager.get().lockNow();
                    }
                });
    }
}
