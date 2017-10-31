package github.tornaco.xposedmoduletest.x.service;

import android.os.Bundle;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public class VerifyArgs {
    public Bundle bnds;
    public String pkg;
    public int uid;
    public int pid;
    public VerifyListener listener;

    public VerifyArgs(Bundle bnds, String pkg, int uid, int pid, VerifyListener listener) {
        this.bnds = bnds;
        this.pkg = pkg;
        this.uid = uid;
        this.pid = pid;
        this.listener = listener;
    }
}