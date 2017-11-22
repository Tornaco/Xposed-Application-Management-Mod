package github.tornaco.xposedmoduletest.xposed.service;

import android.content.Context;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import github.tornaco.apigen.GithubCommitSha;
import github.tornaco.xposedmoduletest.IAshmanService;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by guohao4 on 2017/10/27.
 * Email: Tornaco@163.com
 */
@GithubCommitSha
abstract class XAshmanServiceAbs extends IAshmanService.Stub
        implements IIntentFirewallBridge {

    @Getter
    @Setter
    private Context context;

    @Override
    public void attachContext(Context context) {
        setContext(context);
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter fout, String[] args) {
        super.dump(fd, fout, args);
    }
}
