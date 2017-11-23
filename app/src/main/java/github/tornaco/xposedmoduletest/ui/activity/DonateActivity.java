package github.tornaco.xposedmoduletest.ui.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import github.tornaco.xposedmoduletest.R;

public class DonateActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donate);

        setupToolbar();
        showHomeAsUp();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
