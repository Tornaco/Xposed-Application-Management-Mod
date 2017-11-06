package github.tornaco.keyguard;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

public class KeyguardStorage {
    private static final String PATTERN_SEC = "key_pattern_enc";

    private KeyguardStorage() {
    }

    public static boolean iaPatternSet(Context context) {
        return !TextUtils.isEmpty(getPattern(context));
    }

    public static String getPattern(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PATTERN_SEC, null);
    }

    public static void setPattern(Context context, String code) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(PATTERN_SEC, code)
                .apply();
    }

}
