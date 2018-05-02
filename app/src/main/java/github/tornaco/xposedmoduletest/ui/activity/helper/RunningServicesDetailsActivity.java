package github.tornaco.xposedmoduletest.ui.activity.helper;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.ui.activity.BaseActivity;

/**
 * Created by Tornaco on 2018/5/2 15:05.
 * God bless no bug!
 */
public class RunningServicesDetailsActivity extends BaseActivity {

    public static void start(Context context, Bundle args) {
        Intent starter = new Intent(context, RunningServicesDetailsActivity.class);
        starter.putExtra("args", args);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_with_appbar_template);
        showHomeAsUp();
        Bundle args = getIntent().getBundleExtra("args");
        RunningServiceDetails details = new RunningServiceDetails();
        details.setArguments(args);
        getFragmentManager().beginTransaction().replace(R.id.container, details)
                .commitAllowingStateLoss();
    }
}
