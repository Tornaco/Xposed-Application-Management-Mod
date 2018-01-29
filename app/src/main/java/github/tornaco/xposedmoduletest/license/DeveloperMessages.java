package github.tornaco.xposedmoduletest.license;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.xposedmoduletest.util.XExecutor;

/**
 * Created by guohao4 on 2017/10/17.
 * Email: Tornaco@163.com
 */

public class DeveloperMessages {

    public interface Callback {
        void onError(Throwable e);

        void onSuccess(List<DeveloperMessage> messages);
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
            List<DeveloperMessage> developerMessages = developerMessageService.all().execute().body();
            if (developerMessages == null)
                developerMessages = Lists.newArrayListWithCapacity(0);// Avoid npe.
            Logger.d("DeveloperMessages loading complete " + developerMessages);
            callback.onSuccess(developerMessages);
        } catch (Exception e) {
            Logger.e("DeveloperMessageService reload error: " + e);
            callback.onError(e);
        }
    }
}
