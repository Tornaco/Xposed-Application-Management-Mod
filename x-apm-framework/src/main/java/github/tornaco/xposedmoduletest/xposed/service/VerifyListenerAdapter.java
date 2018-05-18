package github.tornaco.xposedmoduletest.xposed.service;

import github.tornaco.xposedmoduletest.xposed.util.XposedLog;

/**
 * Created by guohao4 on 2017/11/7.
 * Email: Tornaco@163.com
 */

public class VerifyListenerAdapter implements VerifyListener {

    private static VerifyListener sDefault = new VerifyListenerAdapter();

    public static VerifyListener getDefault() {
        return sDefault;
    }

    @Override
    public void onVerifyRes(String pkg, int uid, int pid, int res) {
        XposedLog.verbose("onVerifyRes@" + pkg + ", res: " + res);
    }
}
