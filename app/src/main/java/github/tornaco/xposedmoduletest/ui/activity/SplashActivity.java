package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.MessageQueue;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.XApp;

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

        XApp.getApp().lazyInit();

        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                finish();
                overridePendingTransition(0, R.anim.fade_out);
                NavigatorActivity.start(SplashActivity.this);
                return false;
            }
        });
    }
}
