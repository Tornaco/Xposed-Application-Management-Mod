package github.tornaco.xposedmoduletest.xposed.util;

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;

/**
 * Created by guohao4 on 2017/11/23.
 * Email: Tornaco@163.com
 */

class ShellCommandExecutor {

    public static boolean execute(String command) {
        XPosedLog.wtf("ShellCommandExecutor execute command: " + command);
        CommandResult result = Shell.SH.run(command);
        XPosedLog.wtf("STD getStdout: " + result.getStdout());
        XPosedLog.wtf("STD getStderr: " + result.getStderr());
        XPosedLog.wtf("STD isSuccessful: " + result.isSuccessful());
        XPosedLog.wtf("STD exitCode: " + result.exitCode);
        return result.isSuccessful();
    }
}
