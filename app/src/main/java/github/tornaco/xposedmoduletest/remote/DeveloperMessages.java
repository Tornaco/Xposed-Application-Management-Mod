package github.tornaco.xposedmoduletest.remote;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.xposedmoduletest.model.PushMessage;
import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public class DeveloperMessages {

    public interface Callback {
        void onError(Throwable e);

        void onSuccess(List<PushMessage> messages);
    }

    public static void loadAsync(final Callback callback) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                load(callback);
            }
        });
    }

    public static void load(Callback callback) {
        Logger.d("DeveloperMessages loading...");
        DeveloperMessageService developerMessageService = DeveloperMessageService.Factory.create();
        try {
            List<PushMessage> pushMessages = developerMessageService.all().execute().body();
            if (pushMessages == null)
                pushMessages = Lists.newArrayListWithCapacity(0);// Avoid npe.
            Logger.d("DeveloperMessages loading complete " + pushMessages);
            callback.onSuccess(pushMessages);
        } catch (Exception e) {
            Logger.e("DeveloperMessageService reload error: " + e);
            callback.onError(e);
        }
    }
}
