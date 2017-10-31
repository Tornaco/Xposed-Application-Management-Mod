package github.tornaco.xposedmoduletest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.keyguard.KeyguardPINView;
import com.android.keyguard.KeyguardSecurityCallback;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyguard_pin_view);
        KeyguardPINView keyguardPINView = (KeyguardPINView) findViewById(R.id.keyguard_pin_view);
        keyguardPINView.setKeyguardCallback(new KeyguardSecurityCallback() {
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
        KeyguardPINView keyguardPINView = (KeyguardPINView) findViewById(R.id.keyguard_pin_view);
        keyguardPINView.startAppearAnimation();
    }
}
