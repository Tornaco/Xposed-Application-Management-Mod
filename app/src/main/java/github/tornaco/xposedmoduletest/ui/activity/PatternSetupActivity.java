package github.tornaco.xposedmoduletest.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.andrognito.patternlockview.PatternLockView;
import com.andrognito.patternlockview.listener.PatternLockViewListener;
import com.andrognito.patternlockview.utils.PatternLockUtils;

import java.util.List;

import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.provider.LockStorage;

/**
 * Created by guohao4 on 2017/10/21.
 * Email: Tornaco@163.com
 */

public class PatternSetupActivity extends NeedLockActivity implements PatternLockViewListener {

    private PatternLockView patternLockView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pattern_setup);
        patternLockView = findViewById(R.id.pattern_lock_view);
        patternLockView.addPatternLockListener(this);
        patternLockView.setEnableHapticFeedback(true);
    }

    @Override
    protected String getLockLabel() {
        return getString(R.string.input_previous_password);
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onProgress(List<PatternLockView.Dot> progressPattern) {

    }

    @Override
    public void onComplete(List<PatternLockView.Dot> pattern) {
        LockStorage.setPattern(getApplicationContext(), PatternLockUtils.patternToString(patternLockView, pattern));
        finish();
    }

    @Override
    public void onCleared() {

    }
}
