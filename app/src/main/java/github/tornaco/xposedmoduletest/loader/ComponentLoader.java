package github.tornaco.xposedmoduletest.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.support.annotation.NonNull;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.model.ReceiverInfoSettings;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettings;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
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
            final PackageManager pm = context.getPackageManager();
            final XAshmanManager xAshmanManager = XAshmanManager.singleInstance();
            final DaoSession daoSession = DaoManager.getInstance().getSession(context);
            final ContentResolver resolver = context.getContentResolver();

            if (daoSession == null || resolver == null || xAshmanManager == null)
                return Lists.newArrayListWithCapacity(0);

            List<ActivityInfo> activityInfos = ComponentUtil.getBroadcasts(context, pkg);
            if (Collections.isNullOrEmpty(activityInfos)) return Lists.newArrayListWithCapacity(0);

            final List<ReceiverInfoSettings> out = Lists.newArrayList();
            Collections.consumeRemaining(activityInfos, new Consumer<ActivityInfo>() {
                @Override
                public void accept(ActivityInfo activityInfo) {
                    try {

                        ReceiverInfoSettings settings = new ReceiverInfoSettings();
                        settings.setActivityInfo(activityInfo);


                        settings.setDisplayName(ComponentUtil.getComponentName(activityInfo)
                                .getShortClassName());

                        CharSequence labelCS = pm.getText(activityInfo.packageName,
                                activityInfo.labelRes, activityInfo.applicationInfo);
                        String serviceLabel = labelCS == null
                                ? settings.getDisplayName()
                                : labelCS.toString();
                        settings.setServiceLabel(serviceLabel);

                        settings.setAllowed(xAshmanManager.getComponentEnabledSetting(
                                ComponentUtil.getComponentName(activityInfo))
                                <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

                        out.add(settings);
                    } catch (Throwable e) {
                        Logger.e("Error handing activityInfo: " + e);
                    }
                }
            });
            return out;
        }

        @NonNull
        @Override
        public List<ServiceInfoSettings> loadServiceSettings(String pkg) {
            final PackageManager pm = context.getPackageManager();
            final XAshmanManager xAshmanManager = XAshmanManager.singleInstance();
            final DaoSession daoSession = DaoManager.getInstance().getSession(context);
            final ContentResolver resolver = context.getContentResolver();

            if (daoSession == null || resolver == null || xAshmanManager == null)
                return Lists.newArrayListWithCapacity(0);

            List<ServiceInfo> serviceInfos = ComponentUtil.getServices(context, pkg);
            if (Collections.isNullOrEmpty(serviceInfos)) return Lists.newArrayListWithCapacity(0);

            final List<ServiceInfoSettings> out = Lists.newArrayList();
            Collections.consumeRemaining(serviceInfos, new Consumer<ServiceInfo>() {
                @Override
                public void accept(ServiceInfo serviceInfo) {

                    try {
                        ServiceInfoSettings settings = new ServiceInfoSettings();
                        settings.setServiceInfo(serviceInfo);

                        settings.setDisplayName(ComponentUtil.getComponentName(serviceInfo)
                                .getShortClassName());

                        CharSequence labelCS = pm.getText(serviceInfo.packageName,
                                serviceInfo.labelRes, serviceInfo.applicationInfo);
                        String serviceLabel = labelCS == null
                                ? settings.getDisplayName()
                                : labelCS.toString();
                        settings.setServiceLabel(serviceLabel);
                        settings.setAllowed(xAshmanManager.getComponentEnabledSetting(
                                ComponentUtil.getComponentName(serviceInfo))
                                <= PackageManager.COMPONENT_ENABLED_STATE_ENABLED);

                        out.add(settings);
                    } catch (Throwable e) {
                        Logger.e("Error handing serviceInfo: " + e);
                    }
                }
            });
            return out;
        }
    }
}
