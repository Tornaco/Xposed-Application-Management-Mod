package github.tornaco.xposedmoduletest.ui.activity.whyyouhere;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.pm.PackageManagerCompat;

public class UserGuideActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, UserGuideActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_giude);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.yes)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        findViewById(R.id.no)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finishAffinity();
                        PackageManagerCompat.unInstallUserAppWithIntent(getApplicationContext(), getPackageName());
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
