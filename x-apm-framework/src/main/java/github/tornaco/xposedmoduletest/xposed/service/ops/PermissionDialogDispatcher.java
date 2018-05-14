package github.tornaco.xposedmoduletest.xposed.service.ops;

import java.util.concurrent.ExecutorService;
import java.util.logging.Handler;

import github.tornaco.xposedmoduletest.util.Singleton;

/**
 * Created by guohao4 on 2018/2/2.
 * Email: Tornaco@163.com
 */

public class PermissionDialogDispatcher {

    public static final Singleton<PermissionDialogDispatcher> sDispatcher
            = new Singleton<PermissionDialogDispatcher>() {
        @Override
        protected PermissionDialogDispatcher create() {
            return new PermissionDialogDispatcher();
        }
    };

    private ExecutorService mDispatchService;
    private Handler mUiThreadHandler;
}
