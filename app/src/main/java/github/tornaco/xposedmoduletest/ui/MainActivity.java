package github.tornaco.xposedmoduletest.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;

import ezy.assist.compat.SettingsCompat;
import github.tornaco.xposedmoduletest.ICallback;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.license.ADM;
import github.tornaco.xposedmoduletest.license.License;
import github.tornaco.xposedmoduletest.x.XAIOModule;
import github.tornaco.xposedmoduletest.x.XKey;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SettingsCompat.manageDrawOverlays(this);

        Switch sw = (Switch) findViewById(R.id.switch1);

        sw.setChecked(getSharedPreferences(XAIOModule.SELF_PREF_NAME, MODE_WORLD_READABLE)
                .getBoolean(XKey.ENABLED, false));

        findViewById(R.id.switch1).setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v;
                getSharedPreferences(XAIOModule.SELF_PREF_NAME, MODE_PRIVATE).edit()
                        .putBoolean(XKey.ENABLED, s.isChecked()).apply();

                new AppStartNoter().note(new Handler(Looper.getMainLooper()),
                        MainActivity.this,
                        "STARTER",
                        "TARGET",
                        new ICallback.Stub() {
                            @Override
                            public void onRes(boolean res) throws RemoteException {

                            }
                        });
            }
        });

        boolean valid = ADM.invalidate(new License("tornaco@163.com", "PUBLIC-SOURCE", 0, 0));
        sw.setText(String.valueOf(valid));
    }
}
