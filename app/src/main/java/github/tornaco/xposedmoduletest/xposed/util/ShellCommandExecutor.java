package github.tornaco.xposedmoduletest.xposed.util;

import com.android.internal.os.Zygote;

/**
 * Created by guohao4 on 2017/11/23.
 * Email: Tornaco@163.com
 */

class ShellCommandExecutor {

    public static void execute(String command) {
        Zygote.execShell(command);
    }
}
