package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;

import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.IAppGuardService;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
abstract class XAppGuardServiceAbs extends IAppGuardService.Stub
        implements IAppGuardBridge {

    @Getter
    @Setter
    private Context context;

    @Override
    public void attachContext(Context context) {
        setContext(context);
    }
}
