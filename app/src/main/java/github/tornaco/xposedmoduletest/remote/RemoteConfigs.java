package github.tornaco.xposedmoduletest.remote;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.model.RemoteConfig;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.util.XExecutor;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/11/30.
 * Email: Tornaco@163.com
 */

public class RemoteConfigs {

    private static final RemoteConfig DEFAULT_CONFIG = RemoteConfig
            .builder()
            .donate(true)
            .shutdown(false)
            .build();

    private RemoteConfig config = DEFAULT_CONFIG;

    public RemoteConfig getConfig() {
        return config == null ? DEFAULT_CONFIG : config;
    }

    private RemoteConfigs() {
        initAsync();
    }

    private static final Singleton<RemoteConfigs> sMe = new Singleton<RemoteConfigs>() {
        @Override
        protected RemoteConfigs create() {
            return new RemoteConfigs();
        }
    };

    public static RemoteConfigs getSingleton() {
        return sMe.get();
    }

    private void initAsync() {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    @Synchronized
    private void init() {
        Logger.d("Reading config from remote...");
        RemoteConfigService configService = RemoteConfigService.Factory.create();
        try {
            this.config = configService.get().execute().body();
        } catch (Exception e) {
            Logger.e("RemoteConfig reload error: " + e);
        }
        if (this.config == null) {
            this.config = DEFAULT_CONFIG;
        }
    }
}
