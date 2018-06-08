package github.tornaco.xposedmoduletest.xposed.service.am;

/**
 * Created by Tornaco on 2018/3/14.
 */

public interface AppIdler {

    /**
     * Make the app idle, means make it 'idle' or stop it.
     *
     * @param pkg Target package.
     */
    void setAppIdle(String pkg);

    void setListener(OnAppIdleListener listener);

    interface OnAppIdleListener {
        void onAppIdle(String pkg);
    }
}
