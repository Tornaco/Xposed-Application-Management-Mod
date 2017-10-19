package github.tornaco.xposedmoduletest.x;

import android.content.Context;
import android.preference.PreferenceManager;

import java.util.Observable;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

public class XSettings extends Observable {

    private static XSettings sMe = new XSettings();

    private XSettings() {
    }

    public static XSettings get() {
        return sMe;
    }

    public void setEnabled(Context context, boolean enabled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(XKey.ENABLED, enabled)
                .apply();
        setChanged();
        notifyObservers();
    }

    public boolean enabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(XKey.ENABLED, false);
    }
}
