package github.tornaco.xposedmoduletest.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.android.keyguard.KeyguardPatternView;
import com.android.keyguard.KeyguardSecurityCallback;

import github.tornaco.xposedmoduletest.R;

/**
 * Created by guohao4 on 2017/10/21.
 * Email: Tornaco@163.com
 */

public class PatternSetupActivity extends BaseActivity {
    private KeyguardPatternView keyguardPatternView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pattern_setup);

        keyguardPatternView = (KeyguardPatternView) findViewById(R.id.keyguard_pattern_view);
        keyguardPatternView.setEditMode(true);
        keyguardPatternView.setKeyguardCallback(new KeyguardSecurityCallback() {
            @Override
            public void dismiss(boolean securityVerified) {
                finish();
            }

            @Override
            public void userActivity() {

            }

            @Override
            public boolean isVerifyUnlockOnly() {
                return false;
            }

            @Override
            public void reportUnlockAttempt(boolean success) {

            }

            @Override
            public void reset() {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        keyguardPatternView.startAppearAnimation();
    }
}
