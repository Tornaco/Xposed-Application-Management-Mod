package github.tornaco.xposedmoduletest.xposed.service.am;

import android.content.Context;

import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import lombok.AllArgsConstructor;

/**
 * Created by Tornaco on 2018/3/14 15:14.
 */
// Just kill the package.
@AllArgsConstructor
public class KillAppIdler implements AppIdler {

    private Context context;
    private OnAppIdleListener listener;

    @Override
    public void setAppIdle(String pkg) {
        if (pkg != null) {
            PkgUtil.kill(context, pkg);
            listener.onAppIdle(pkg);
        }
    }

    @Override
    public void setListener(OnAppIdleListener listener) {
        this.listener = listener;
    }
}
