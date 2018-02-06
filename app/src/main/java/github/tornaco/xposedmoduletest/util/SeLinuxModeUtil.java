package github.tornaco.xposedmoduletest.util;

import com.jaredrummler.android.shell.Shell;

/**
 * Created by guohao4 on 2018/2/6.
 * Email: Tornaco@163.com
 */

public class SeLinuxModeUtil {

    public static void applyMode(final boolean enforce) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                String cmd = enforce ? "setenforce 1" : "setenforce 0";
                Shell.SU.run(cmd);
            }
        });
    }
}
