package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;

/**
 * Created by guohao4 on 2018/1/18.
 * Email: Tornaco@163.com
 */

public class ShortcutStubActivity extends BaseActivity {

    private static final String EXTRA_TARGET_PKG = "stub.extra.pkg";
    private static final String EXTRA_RE_DISABLE = "stub.extra.re.disable";
    private static final String EXTRA_RE_DISABLE_TR = "stub.extra.re.disable_tr";

    public static Intent createIntent(Context context, String targetPackage, boolean redisable, boolean redisabletr) {
        Intent intent = new Intent(context, ShortcutStubActivity.class);
        intent.putExtra(EXTRA_TARGET_PKG, targetPackage);
        intent.putExtra(EXTRA_RE_DISABLE, redisable);
        intent.putExtra(EXTRA_RE_DISABLE_TR, redisabletr);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            internalResolveIntent();
        } finally {
            finish();
        }
    }

    private void internalResolveIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String target = intent.getStringExtra(EXTRA_TARGET_PKG);
        if (target == null) {
            Toast.makeText(getActivity(), R.string.short_stub_invalid_target, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean redisale = intent.getBooleanExtra(EXTRA_RE_DISABLE, true);
        boolean redisaletr = intent.getBooleanExtra(EXTRA_RE_DISABLE_TR, true);

        // Enable this app first.
        if (XAPMManager.get().isServiceAvailable()) {

            int state = XAPMManager.get().getApplicationEnabledSetting(target);
            boolean disabled = state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    && state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;

            if (disabled) {
                XAPMManager.get().setApplicationEnabledSetting(target, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0, true);
            }

            if (redisale) {
                XAPMManager.get().addPendingDisableApps(target);
            }

            if (redisaletr) {
                XAPMManager.get().addPendingDisableAppsTR(target);
            }

            Intent launcherIntent = getPackageManager().getLaunchIntentForPackage(target);
            if (launcherIntent == null) {
                Toast.makeText(getActivity(), R.string.short_stub_invalid_target, Toast.LENGTH_SHORT).show();
                return;
            }
            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launcherIntent);
        }
    }
}
