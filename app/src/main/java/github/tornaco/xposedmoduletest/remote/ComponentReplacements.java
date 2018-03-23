package github.tornaco.xposedmoduletest.remote;

import org.newstand.logger.Logger;

import java.io.IOException;

import github.tornaco.xposedmoduletest.bean.ComponentReplacementList;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.util.XExecutor;
import lombok.Synchronized;

/**
 * Created by guohao4 on 2017/11/30.
 * Email: Tornaco@163.com
 */

public class ComponentReplacements {

    private static final Singleton<ComponentReplacements> sMe = new Singleton<ComponentReplacements>() {
        @Override
        protected ComponentReplacements create() {
            return new ComponentReplacements();
        }
    };

    public static ComponentReplacements getSingleton() {
        return sMe.get();
    }

    @Synchronized
    public ComponentReplacementList load() throws IOException {
        Logger.d("Reading ComponentReplacements from remote...");
        ComponentReplacementService componentReplacementService = ComponentReplacementService.Factory.create();
        return new ComponentReplacementList(componentReplacementService.get().execute().body());
    }

    public void loadAsync(final LoaderListener loaderListener) {
        XExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    loaderListener.onStartLoading();
                    loaderListener.onLoadingComplete(load());
                } catch (Throwable e) {
                    Logger.e("loadAsync: " + Logger.getStackTraceString(e));
                    loaderListener.onError(e);
                }
            }
        });
    }

    public interface LoaderListener {
        void onStartLoading();

        void onLoadingComplete(ComponentReplacementList list);

        void onError(Throwable e);
    }
}
