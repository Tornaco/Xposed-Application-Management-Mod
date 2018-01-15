package github.tornaco.xposedmoduletest.xposed.service.shell;

import android.os.RemoteException;

import java.io.PrintWriter;

import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.util.XposedLog;
import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2018/1/15.
 * Email: Tornaco@163.com
 */
@AllArgsConstructor
public class AshShellCommand extends ShellCommandCompat {

    private IAshmanService mService;

    @Override
    public int onCommand(String cmd) throws RemoteException {
        XposedLog.verbose("AshShellCommand@ onCommand: " + cmd);
        if (cmd == null) {
            handleDefaultCommands(cmd);
            return 0;
        }
        if (cmd.equals("-h") || cmd.equals("help") || cmd.equals("-help")) {
            onHelp();
            return 0;
        }

        switch (cmd) {
            case "enable":
                String module = getNextArg();
                switch (module) {
                    case "app_lock":
                        mService.setAppLockEnabled(true);
                        break;

                }
                break;

            case "disable":
                module = getNextArg();
                switch (module) {
                    case "app_lock":
                        mService.setAppLockEnabled(false);
                        break;

                }
                break;

            case "reset":
                mService.restoreDefaultSettings();
                break;
        }

        return 0;
    }

    @Override
    public void onHelp() {
        PrintWriter pw = getOutPrintWriter();
        pw.println("APM commands:");
        pw.println("    help");
        pw.println("        Print this help text.");
        pw.println("");

        pw.println("    enable [module]");
        pw.println("        Enable [module], [module] can be one of:");
        StringBuilder modules = new StringBuilder("        ");
        for (Object m : XAppBuildVar.BUILD_VARS) {
            modules.append(m.toString()).append(", ");
        }
        pw.println(modules.toString());
        pw.println("");

        pw.println("    disable [module]");
        pw.println("        Disable [module], [module] can be one of:");
        pw.println(modules.toString());
        pw.println("");

        pw.println("    reset");
        pw.println("        Rest all settings to default");
        pw.println("");
    }
}
