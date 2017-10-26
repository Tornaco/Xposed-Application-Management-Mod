package github.tornaco.xposedmoduletest.x;

import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

public interface XKey {
    String ENABLED = "key_enabled";
    String BLUR = "blur_enabled";
    String BLUR_ALL = "blur_all_enabled";
    String ALLOW_3RD_VERIFIER = "3rd_ver_allowed";
    String TAKE_PHOTO_ENABLED = "key_take_photo_enabled";
    String TAKE_FULL_SCREEN_NOTER = "key_full_screen_noter";
    String PASSCODE_ENCRYPT = "key_pass_code_enc";
    String ACTIVATE_CODE = "key_act_code";
    String DEV_MODE = "dev_mode_enabled";
    String FP_ENABLED = "fp_enabled";
    String ALWAYS_NOTE = "always_note";
    String THEME = "theme";
    String FIRST_RUN = "first_ru" + BuildConfig.VERSION_NAME;

    String EXTRA_PKG_NAME = "extra.pkg";
    String EXTRA_TRANS_ID = "extra.tid";
}
