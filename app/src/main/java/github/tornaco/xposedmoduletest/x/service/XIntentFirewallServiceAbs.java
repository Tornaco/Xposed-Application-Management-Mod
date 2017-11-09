package github.tornaco.xposedmoduletest.x.service;

import android.content.Context;

import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.IIntentFirewall;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
abstract class XIntentFirewallServiceAbs extends IIntentFirewall.Stub
        implements IIntentFirewallBridge {

    @Getter
    @Setter
    private Context context;

    @Override
    public void attachContext(Context context) {
        setContext(context);
    }
}
