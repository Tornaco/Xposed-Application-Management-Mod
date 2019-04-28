package github.tornaco.xposedmoduletest.xposed.service.shell;

import android.os.RemoteException;

import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.IAshmanService;
import github.tornaco.xposedmoduletest.IProcessClearListener;
import github.tornaco.xposedmoduletest.xposed.XAppBuildVar;
import github.tornaco.xposedmoduletest.xposed.bean.OpLog;
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

            case "lock-screen":
                mService.injectPowerEvent();
                break;
            case "clear-processes":
                final CountDownLatch latch = new CountDownLatch(1);
                mService.clearProcess(new IProcessClearListener.Stub() {
                    @Override
                    public void onPrepareClearing() throws RemoteException {

                    }

                    @Override
                    public void onStartClearing(int plan) throws RemoteException {

                    }

                    @Override
                    public void onClearingPkg(String pkg) throws RemoteException {

                    }

                    @Override
                    public void onClearedPkg(String pkg) throws RemoteException {
                        final PrintWriter pw = getOutPrintWriter();
                        pw.println("    Killed: " + pkg);
                    }

                    @Override
                    public void onAllCleared(String[] pkg) throws RemoteException {
                        final PrintWriter pw = getOutPrintWriter();
                        pw.println("    DONE!");
                        latch.countDown();
                    }

                    @Override
                    public void onIgnoredPkg(String pkg, String reason) throws RemoteException {

                    }
                }, false, false);
                try {
                    latch.await();
                } catch (InterruptedException ignored) {

                }
                break;

            case "reset":
                mService.restoreDefaultSettings();
                break;

            case "dump-op-log":
                String code = getNextArg();
                List<OpLog> opLogs = mService.getOpLogForOp(Integer.parseInt(code));
                final PrintWriter pw = getOutPrintWriter();
                pw.println("    OP LOG FOR OP: " + code);
                github.tornaco.android.common.Collections.consumeRemaining(opLogs, new Consumer<OpLog>() {
                    @Override
                    public void accept(OpLog log) {
                        pw.println("    " + log);
                    }
                });
                pw.println("");
                break;
            case "dump-pkg-op-log":
                String pkg = getNextArg();
                opLogs = mService.getOpLogForPackage(pkg);
                final PrintWriter pw2 = getOutPrintWriter();
                pw2.println("    OP LOG FOR PKG: " + pkg);
                github.tornaco.android.common.Collections.consumeRemaining(opLogs, new Consumer<OpLog>() {
                    @Override
                    public void accept(OpLog log) {
                        pw2.println("    " + log);
                    }
                });
                pw2.println("");
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

        pw.println("    lock-screen");
        pw.println("        Lock screen now");
        pw.println("");

        pw.println("    clear-processes");
        pw.println("        Clear processes now");
        pw.println("");

        pw.println("    reset");
        pw.println("        Rest all settings to default");
        pw.println("");

        pw.println("    dump-op-log [code]");
        pw.println("        Print all operation log by op");
        pw.println("");

        pw.println("    dump-pkg-op-log [package]");
        pw.println("        Print all operation log by package");
        pw.println("");
    }
}
