package github.tornaco.xposedmoduletest.xposed.service.am;

/**
 * Created by Tornaco on 2018/3/14.
 */

public interface AppIdler {

    void setAppIdle(String pkg);

    void setListener(OnAppIdleListener listener);

    interface OnAppIdleListener {
        void onAppIdle(String pkg);
    }
}
