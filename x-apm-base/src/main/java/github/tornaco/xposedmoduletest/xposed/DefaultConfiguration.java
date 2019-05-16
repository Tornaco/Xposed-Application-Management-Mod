package github.tornaco.xposedmoduletest.xposed;

import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by Tornaco on 2018/5/14 10:26.
 * God bless no bug!
 */
public abstract class DefaultConfiguration {

    // Debug.
    public static final String LOG_TAG_PREFIX = "X-APM";

    // Blur config.
    public static final float BITMAP_SCALE = 0.18f;
    /* Initial blur radius. */
    public static final int BLUR_RADIUS = 8;
    public static final int BLUR_RADIUS_MAX = 25;

    @SuppressWarnings("unused")
    public static boolean isDogFoodBuild(String checkReason) {
        return BuildConfig.DEBUG;
    }
}
