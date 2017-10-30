package github.tornaco.xposedmoduletest.x;

import android.content.Context;
import android.os.Bundle;

import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.IAppGuardService;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
abstract class XAppGuardServiceAbs extends IAppGuardService.Stub {

    String commitDate() {
        return XAppGuardServiceAbsGithubCommitSha.LATEST_SHA_DATE;
    }

    void attachContext(Context context) {

    }

    void publish() {

    }

    void systemReady() {

    }

    void publishFeature(String f) {

    }

    void setStatus(XStatus xStatus) {

    }

    void shutdown() {

    }

    boolean passed(String pkg) {
        return true;
    }

    boolean interruptPackageRemoval(String pkg) {
        return false;
    }

    void verify(Bundle options, String pkg, int uid, int pid, XAppGuardServiceImpl.VerifyListener listener) {
    }

    void onUserLeaving() {
    }

    boolean isBlurForPkg(String pkg) {
        return false;
    }

    public Context getContext() {
        return null;
    }
}
