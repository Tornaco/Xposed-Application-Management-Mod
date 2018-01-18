package github.tornaco.xposedmoduletest.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;

/**
 * Created by guohao4 on 2018/1/18.
 * Email: Tornaco@163.com
 */

public class ShortcutStubActivity extends BaseActivity {

    private static final String EXTRA_TARGET_PKG = "stub.extra.pkg";
    private static final String EXTRA_RE_DISABLE = "stub.extra.re.disable";

    public static Intent createIntent(Context context, String targetPackage, boolean redisable) {
        Intent intent = new Intent(context, ShortcutStubActivity.class);
        intent.putExtra(EXTRA_TARGET_PKG, targetPackage);
        intent.putExtra(EXTRA_RE_DISABLE, redisable);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }

        String target = intent.getStringExtra(EXTRA_TARGET_PKG);
        if (target == null) {
            Toast.makeText(getActivity(), R.string.short_stub_invalid_target, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean redisale = intent.getBooleanExtra(EXTRA_RE_DISABLE, false);

        // Enable this app first.
        if (XAshmanManager.get().isServiceAvailable()) {

            int state = XAshmanManager.get().getApplicationEnabledSetting(target);
            boolean disabled = state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    && state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;


            if (disabled) {
                XAshmanManager.get().setApplicationEnabledSetting(target, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);

                if (redisale) {
                    XAshmanManager.get().addPendingDisableApps(target);
                }
            }

            Intent launcherIntent = getPackageManager().getLaunchIntentForPackage(target);
            if (launcherIntent == null) {
                return;
            }
            launcherIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(launcherIntent);
        }
    }
}
