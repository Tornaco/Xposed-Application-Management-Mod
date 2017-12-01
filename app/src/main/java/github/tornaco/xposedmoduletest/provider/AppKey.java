package github.tornaco.xposedmoduletest.provider;

import github.tornaco.xposedmoduletest.BuildConfig;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

public interface AppKey {
    String FIRST_RUN = "first_ru" + BuildConfig.VERSION_NAME;
    String BUILD_DATE = "build_date";
    String SHOW_INFO_PREFIX = "key_show_info_";
}
