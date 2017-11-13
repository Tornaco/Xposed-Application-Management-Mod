package github.tornaco.xposedmoduletest.xposed.service;

/**
 * Created by guohao4 on 2017/10/31.
 * Email: Tornaco@163.com
 */

public interface VerifyListener {
    void onVerifyRes(String pkg, int uid, int pid, int res);
}
