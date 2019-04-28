package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2018/2/27.
 * Email: Tornaco@163.com
 */

public class SplashActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, SplashActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Looper.myQueue().addIdleHandler(() -> {
            Logger.w("SplashActivity IDLE");
            finish();
            overridePendingTransition(0, R.anim.fade_out);
            NavigatorActivityBottomNav.start(SplashActivity.this);
            return false;
        });
    }
}
