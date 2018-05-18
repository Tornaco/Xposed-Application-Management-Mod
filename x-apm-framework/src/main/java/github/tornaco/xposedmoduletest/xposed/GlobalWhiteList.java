package github.tornaco.xposedmoduletest.xposed;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Created by Tornaco on 2018/5/4 13:57.
 * God bless no bug!
 */
public class GlobalWhiteList {

    private static final Set<String> GLOBAL_WHITE_LIST = Sets.newHashSet(
            "android",

            "com.android.systemui",
            "com.android.phone",
            "com.android.mtp",
            "android.ext.shared",
            "com.android.pacprocessor",
            "com.android.server.telecom",
            "com.android.carrierconfig",
            "com.android.defcontainer",
            "com.android.mms.service",
            "com.validation",

            "com.android.providers.media",
            "com.android.providers.calendar",
            "com.android.providers.contacts",
            "com.android.providers.downloads",
            "com.android.providers.downloads.ui",
            "com.android.providers.settings",
            "com.android.providers.telephony",
            "com.android.providers.userdictionary",
            "com.android.providers.phone",
            "com.android.providers.contacts",

            "com.zui.incallui",

            "github.tornaco.xposedmoduletest",
            "de.robv.android.xposed.installer"
    );

    public static boolean isInGlobalWhiteList(String pkg) {
        return GLOBAL_WHITE_LIST.contains(pkg);
    }
}
