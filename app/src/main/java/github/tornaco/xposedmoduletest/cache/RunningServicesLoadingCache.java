package github.tornaco.xposedmoduletest.cache;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import dev.nick.eventbus.Event;
import dev.nick.eventbus.EventBus;
import github.tornaco.xposedmoduletest.ui.activity.helper.RunningState;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.XApp;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by guohao4 on 2018/3/6.
 * Email: Tornaco@163.com
 */

public class RunningServicesLoadingCache {

    @AllArgsConstructor
    @Getter
    public static class RunningServicesData {
        List<RunningState.MergedItem> list;
        int serviceCount;
        int appCount;
    }

    private static final String KEY_DEFAULT = UUID.randomUUID().toString();

    private static final Singleton<RunningServicesLoadingCache> sMe = new Singleton<RunningServicesLoadingCache>() {
        @Override
        protected RunningServicesLoadingCache create() {
            return new RunningServicesLoadingCache();
        }
    };

    public static RunningServicesLoadingCache getInstance() {
        return sMe.get();
    }

    private RunningServicesLoadingCache() {
    }

    private LoadingCache<String, RunningServicesData> mCache
            = CacheBuilder.newBuilder().maximumSize(1024)
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build(new CacheLoader<String, RunningServicesData>() {
                @Override
                public RunningServicesData load(@NonNull String key) throws Exception {
                    try {
                        RunningState state = RunningState.getInstance(XApp.getApp().getApplicationContext());
                        state.updateNow();
                        List<RunningState.MergedItem> data = state.getCurrentMergedItems();
                        if (data == null) {
                            return new RunningServicesData(new ArrayList<RunningState.MergedItem>(0), 0, 0);
                        }
                        int appCount = data.size() - 1;
                        int serviceCount = 0;
                        for (RunningState.MergedItem item : data) {
                            serviceCount += item.serviceCount();
                        }
                        return new RunningServicesData(data, serviceCount, appCount);
                    } finally {
                        EventBus.from().publish(new Event(XApp.EVENT_RUNNING_SERVICE_CACHE_UPDATE));
                    }
                }
            });

    public RunningServicesData getRunningServiceCache() {
        return getRunningServiceCache(KEY_DEFAULT);
    }

    public RunningServicesData getRunningServiceCache(String key) {
        try {
            return mCache.get(key);
        } catch (ExecutionException e) {
            return new RunningServicesData(new ArrayList<RunningState.MergedItem>(0), 0, 0);
        }
    }

    @Nullable
    public RunningServicesData getIfPresent() {
        return getIfPresent(KEY_DEFAULT);
    }

    @Nullable
    public RunningServicesData getIfPresent(Object key) {
        return mCache.getIfPresent(key);
    }

    public void refresh() {
        refresh(KEY_DEFAULT);
    }

    public void refresh(String key) {
        mCache.refresh(key);
    }
}
