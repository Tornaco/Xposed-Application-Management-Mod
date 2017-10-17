package github.tornaco.xposedmoduletest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;

import ezy.assist.compat.SettingsCompat;
import github.tornaco.xposedmoduletest.license.ADM;
import github.tornaco.xposedmoduletest.license.License;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        SettingsCompat.manageDrawOverlays(this);

        Switch sw = (Switch) findViewById(R.id.switch1);

        sw.setChecked(getSharedPreferences(AMSModule.SELF_PREF_NAME, MODE_WORLD_READABLE)
                .getBoolean(XKey.ENABLED, false));

        findViewById(R.id.switch1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Switch s = (Switch) v;
                getSharedPreferences(AMSModule.SELF_PREF_NAME, MODE_PRIVATE).edit()
                        .putBoolean(XKey.ENABLED, s.isChecked()).apply();
            }
        });

        boolean valid = ADM.invalidate(new License("tornaco@163.com", "PUBLIC-SOURCE", 0, 0));
        sw.setText(String.valueOf(valid));
    }
}
