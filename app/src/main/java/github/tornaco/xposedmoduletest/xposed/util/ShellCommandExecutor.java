package github.tornaco.xposedmoduletest.xposed.util;

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;

/**
 * Created by guohao4 on 2017/11/23.
 * Email: Tornaco@163.com
 */

class ShellCommandExecutor {

    public static boolean execute(String command) {
        XLog.logF("ShellCommandExecutor execute command: " + command);
        CommandResult result = Shell.SH.run(command);
        XLog.logF("STD getStdout: " + result.getStdout());
        XLog.logF("STD getStderr: " + result.getStderr());
        XLog.logF("STD isSuccessful: " + result.isSuccessful());
        XLog.logF("STD exitCode: " + result.exitCode);
        return result.isSuccessful();
    }
}
