package github.tornaco.xposedmoduletest.ui.activity.whyyouhere;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.compat.pm.PackageManagerCompat;

public class UserGuideActivityA extends AppCompatActivity {

    public static void start(Context context) {
        Intent starter = new Intent(context, UserGuideActivityA.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_guide_page);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView messageView = findViewById(R.id.intro_text);
        messageView.setText(getIntroMessage());
        TextView titleView = findViewById(R.id.intro_title);
        titleView.setText(getIntroTitle());

        findViewById(R.id.yes)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        if (getNextIntent() != null) {
                            startActivity(getNextIntent());
                        }
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

    int getIntroTitle() {
        return R.string.user_notice_title;
    }

    int getIntroMessage() {
        return R.string.user_notice_title_a;
    }

    Intent getNextIntent() {
        return new Intent(this, UserGuideActivityB.class);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
