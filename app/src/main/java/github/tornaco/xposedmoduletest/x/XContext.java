package github.tornaco.xposedmoduletest.x;

/**
 * Created by guohao4 on 2017/10/23.
 * Email: Tornaco@163.com
 */

public interface XContext {

    String APP_GUARD_SERVICE = "user.appguard";

    String SETTINGS_APP_GUARD_ENABLED = "settings_app_guard_enabled";
    String SETTINGS_APP_SCREENSHOT_BLUR_ENABLED = "settings_app_screenshot_blur_enabled";

    interface Feature {
        String BASE = "feature.base";
        String BLUR = "feature.blur";
    }

}
