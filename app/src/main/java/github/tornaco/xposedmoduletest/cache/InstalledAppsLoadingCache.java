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
import github.tornaco.xposedmoduletest.loader.ComponentLoader;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.XAPMApplication;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by guohao4 on 2018/3/6.
 * Email: Tornaco@163.com
 */

public class InstalledAppsLoadingCache {

    @AllArgsConstructor
    @Getter
    public static class InstalledAppsData {
        List<CommonPackageInfo> list;
        int appCount;
    }

    private static final String KEY_DEFAULT = UUID.randomUUID().toString();

    private static final Singleton<InstalledAppsLoadingCache>
            sMe = new Singleton<InstalledAppsLoadingCache>() {
        @Override
        protected InstalledAppsLoadingCache create() {
            return new InstalledAppsLoadingCache();
        }
    };

    public static InstalledAppsLoadingCache getInstance() {
        return sMe.get();
    }

    private InstalledAppsLoadingCache() {
    }

    private LoadingCache<String, InstalledAppsData> mCache
            = CacheBuilder.newBuilder().maximumSize(1024)
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build(new CacheLoader<String, InstalledAppsData>() {
                @Override
                public InstalledAppsData load(@NonNull String key) throws Exception {
                    try {
                        List<CommonPackageInfo> infoList =
                                ComponentLoader.Impl.create(XAPMApplication.getApp().getApplicationContext())
                                        .loadInstalledApps(false, ComponentLoader.Sort.byState(),
                                                CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS);
                        int appCount = infoList.size();
                        return new InstalledAppsData(infoList, appCount);
                    } finally {
                        EventBus.from().publish(new Event(XAPMApplication.EVENT_INSTALLED_APPS_CACHE_UPDATE));
                    }
                }
            });

    public InstalledAppsData getInstalledAppsCache() {
        return getInstalledAppsCache(KEY_DEFAULT);
    }

    public InstalledAppsData getInstalledAppsCache(String key) {
        try {
            return mCache.get(key);
        } catch (ExecutionException e) {
            return new InstalledAppsData(new ArrayList<CommonPackageInfo>(0), 0);
        }
    }

    @Nullable
    public InstalledAppsData getIfPresent() {
        return getIfPresent(KEY_DEFAULT);
    }

    @Nullable
    public InstalledAppsData getIfPresent(Object key) {
        return mCache.getIfPresent(key);
    }

    public void refresh() {
        refresh(KEY_DEFAULT);
    }

    public void refresh(String key) {
        mCache.refresh(key);
    }
}
