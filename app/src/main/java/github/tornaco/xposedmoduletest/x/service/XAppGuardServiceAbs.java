package github.tornaco.xposedmoduletest.x.service;

import android.content.Context;
import android.os.Bundle;

import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.IAppGuardService;
import github.tornaco.xposedmoduletest.x.XStatus;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
public abstract class XAppGuardServiceAbs extends IAppGuardService.Stub {

    String commitDate() {
        return XAppGuardServiceAbsGithubCommitSha.LATEST_SHA_DATE;
    }

    public void attachContext(Context context) {

    }

    public void publish() {

    }

    public void systemReady() {

    }

    public void publishFeature(String f) {

    }

    public void setStatus(XStatus xStatus) {

    }

    public void shutdown() {

    }

    public boolean passed(String pkg) {
        return true;
    }

    public boolean interruptPackageRemoval(String pkg) {
        return false;
    }

    public void verify(Bundle options, String pkg, int uid, int pid, VerifyListener listener) {
    }

    public void onUserLeaving() {
    }

    public boolean isBlurForPkg(String pkg) {
        return false;
    }

    public Context getContext() {
        return null;
    }
}
