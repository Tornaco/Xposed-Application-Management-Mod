package github.tornaco.xposedmoduletest.compat.os;

import com.jaredrummler.android.shell.Shell;

import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by guohao4 on 2017/12/4.
 * Email: Tornaco@163.com
 */

public class PowerManagerCompat {

    public static void restartAndroid() {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Shell.SU.run("stop; start");
            }
        });
    }
}
