package github.tornaco.xposedmoduletest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.keyguard.KeyguardPatternView;
import com.android.keyguard.KeyguardSecurityCallback;

public class TestActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyguard_pattern_view);
        KeyguardPatternView keyguardPatternView = (KeyguardPatternView) findViewById(R.id.keyguard_pattern_view);
        keyguardPatternView.setKeyguardCallback(new KeyguardSecurityCallback() {

            @Override
            public void dismiss(boolean securityVerified) {

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
        KeyguardPatternView keyguardPatternView = (KeyguardPatternView) findViewById(R.id.keyguard_pattern_view);
        keyguardPatternView.startAppearAnimation();

    }
}
