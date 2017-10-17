package github.tornaco.xposedmoduletest;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.newstand.logger.Logger;

public class AppStartNoterActivity extends AppCompatActivity {

    public static final String KEY_TRANS_ID = "github.tornaco.key.trans_id";
    public static final String KEY_TRANS_PACKAGE = "github.tornaco.key.trans_pkg";
    public static final String KEY_TRANS_RES = "github.tornaco.key.trans_res";
    public static final String ACTION_TRANS_RES = "github.tornaco.action.trans_res";

    private String pkg;
    private int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_start_noter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                Intent serviceIntent = new Intent(AppStartNoterActivity.this, AppService.class);
                serviceIntent.setAction(ACTION_TRANS_RES);
                serviceIntent.putExtra(KEY_TRANS_ID, id);
                serviceIntent.putExtra(KEY_TRANS_RES, true);
                startService(serviceIntent);
                finish();
            }
        });

        resolveIntent();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        pkg = intent.getStringExtra(KEY_TRANS_PACKAGE);
        id = intent.getIntExtra(KEY_TRANS_ID, -1);

        Logger.d("resolveIntent: id %s pkg %s", id, pkg);


    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
