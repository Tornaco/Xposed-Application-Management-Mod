package github.tornaco.xposedmoduletest.ui.activity.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.Themes;
import github.tornaco.xposedmoduletest.ui.activity.WithWithCustomTabActivity;

/**
 * Created by guohao4 on 2017/12/29.
 * Email: Tornaco@163.com
 */

public class GetPlayVersionActivity extends WithWithCustomTabActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, GetPlayVersionActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int getUserSetThemeResId(Themes themes) {
        return themes.getThemeStyleRes();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_on_play);
        showHomeAsUp();

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToWebPage("https://play.google.com/store/apps/details?id=github.tornaco.xposedmoduletest");
            }
        });
    }
}
