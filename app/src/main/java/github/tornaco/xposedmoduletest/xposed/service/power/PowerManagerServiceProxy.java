package github.tornaco.xposedmoduletest.xposed.service.power;

import android.os.PowerManager;

import java.util.HashSet;
import java.util.Set;

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.xposed.service.DebugOnly;
import github.tornaco.xposedmoduletest.xposed.service.InvokeTargetProxy;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by Tornaco on 2018/5/10 12:07.
 * God bless no bug!
 */

@DebugOnly
public class PowerManagerServiceProxy extends InvokeTargetProxy<Object> {

    private final Set<WakelockAcquire> mSeenWakeLocks = new HashSet<>();
    private final Set<WakelockAcquire> mBlockedWakeLocks = new HashSet<>();

    private boolean mWakeLockBlockingEnabled;

    public PowerManagerServiceProxy(Object host) {
        super(host);
    }

    public String[] getSeanWakeLocks() {
        return null;
    }

    public boolean onAcquireWakeLockInternal(int flags, String tag, String packageName) {
        if (tag == null || packageName == null) {
            return true;
        }

        WakelockAcquire acquire = new WakelockAcquire(tag, packageName);

        // Sean.
        if (!mSeenWakeLocks.contains(acquire)) {
            if ((flags & PowerManager.WAKE_LOCK_LEVEL_MASK) == PowerManager.PARTIAL_WAKE_LOCK) {
                mSeenWakeLocks.add(acquire);
                if (BuildConfig.DEBUG) {
                    XposedLog.verbose("PowerManagerServiceProxy, sean: " + acquire);
                }
            }
        }

        // Check if it is blocked.
        if (mWakeLockBlockingEnabled) {
            if (mBlockedWakeLocks.contains(acquire)) {
                return false;
            }
        }

        return true;
    }
}
