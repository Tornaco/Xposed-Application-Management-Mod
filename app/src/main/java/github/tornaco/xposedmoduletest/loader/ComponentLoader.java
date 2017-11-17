package github.tornaco.xposedmoduletest.loader;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ServiceInfo;
import android.support.annotation.NonNull;

import com.google.common.collect.Lists;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.bean.ComponentSettings;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.model.ReceiverInfoSettings;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettings;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public interface ComponentLoader {

    @NonNull
    List<ReceiverInfoSettings> loadReceiverSettings(String pkg);

    @NonNull
    List<ServiceInfoSettings> loadServiceSettings(String pkg);

    @AllArgsConstructor
    class Impl implements ComponentLoader {

        private Context context;

        public static ComponentLoader create(Context context) {
            return new Impl(context);
        }

        @NonNull
        @Override
        public List<ReceiverInfoSettings> loadReceiverSettings(String pkg) {
            final DaoSession daoSession = DaoManager.getInstance().getSession(context);
            final ContentResolver resolver = context.getContentResolver();
            if (daoSession == null || resolver == null) return Lists.newArrayListWithCapacity(0);
            List<ActivityInfo> activityInfos = ComponentUtil.getBroadcasts(context, pkg);
            if (Collections.isNullOrEmpty(activityInfos)) return Lists.newArrayListWithCapacity(0);
            final List<ReceiverInfoSettings> out = Lists.newArrayList();
            final List<ComponentSettings> csa = daoSession.getComponentSettingsDao().loadAll();
            Collections.consumeRemaining(activityInfos, new Consumer<ActivityInfo>() {
                @Override
                public void accept(ActivityInfo activityInfo) {

                    ReceiverInfoSettings settings = new ReceiverInfoSettings();
                    settings.setActivityInfo(activityInfo);

                    // Check if this comp is allowed.
                    ComponentSettings cs = findComponentSettingsFrom(csa,
                            ComponentUtil.getComponentName(activityInfo));

                    settings.setAllowed(cs == null || cs.getAllow());

                    out.add(settings);
                }
            });
            return out;
        }

        @NonNull
        @Override
        public List<ServiceInfoSettings> loadServiceSettings(String pkg) {
            final DaoSession daoSession = DaoManager.getInstance().getSession(context);
            final ContentResolver resolver = context.getContentResolver();
            if (daoSession == null || resolver == null) return Lists.newArrayListWithCapacity(0);
            List<ServiceInfo> serviceInfos = ComponentUtil.getServices(context, pkg);
            if (Collections.isNullOrEmpty(serviceInfos)) return Lists.newArrayListWithCapacity(0);
            final List<ServiceInfoSettings> out = Lists.newArrayList();
            final List<ComponentSettings> csa = daoSession.getComponentSettingsDao().loadAll();
            Collections.consumeRemaining(serviceInfos, new Consumer<ServiceInfo>() {
                @Override
                public void accept(ServiceInfo serviceInfo) {

                    ServiceInfoSettings settings = new ServiceInfoSettings();
                    settings.setServiceInfo(serviceInfo);

                    // Check if this comp is allowed.
                    ComponentSettings cs = findComponentSettingsFrom(csa,
                            ComponentUtil.getComponentName(serviceInfo));

                    settings.setAllowed(cs == null || cs.getAllow());

                    out.add(settings);
                }
            });
            return out;
        }

        private static ComponentSettings findComponentSettingsFrom(List<ComponentSettings> settings,
                                                                   final ComponentName target) {
            for (ComponentSettings cs : settings) {
                if (cs.is(target)) return cs;
            }
            return null;
        }
    }
}
