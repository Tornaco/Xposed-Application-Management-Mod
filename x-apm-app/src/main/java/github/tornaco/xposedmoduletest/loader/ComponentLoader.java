package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.model.ActivityInfoSettings;
import github.tornaco.xposedmoduletest.model.ActivityInfoSettingsList;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettings;
import github.tornaco.xposedmoduletest.model.ServiceInfoSettingsList;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.util.ComponentUtil;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public interface ComponentLoader {

    abstract class Sort {

        public abstract void performSort(List<CommonPackageInfo> commonPackageInfos);

        public static Sort byName() {
            return new Sort() {
                @Override
                public void performSort(List<CommonPackageInfo> commonPackageInfos) {
                    LoaderUtil.commonSort(commonPackageInfos);
                }
            };
        }

        public static Sort byOp() {
            return new Sort() {
                @Override
                public void performSort(List<CommonPackageInfo> commonPackageInfos) {
                    LoaderUtil.opSort(commonPackageInfos);
                }
            };
        }

        public static Sort byState() {
            return new Sort() {
                @Override
                public void performSort(List<CommonPackageInfo> commonPackageInfos) {
                    LoaderUtil.stateSort(commonPackageInfos);
                }
            };
        }
    }

    List<CommonPackageInfo> loadInstalledApps(boolean showSystem, Sort sort, int filterOption);

    List<CommonPackageInfo> loadInstalledAppsWithOp(boolean showSystem, Sort sort, int filterOption);

    @NonNull
    List<ActivityInfoSettings> loadActivitySettings(String pkg);

    @NonNull
    List<ActivityInfoSettings> loadReceiverSettings(String pkg);

    @NonNull
    List<ServiceInfoSettings> loadServiceSettings(String pkg);

    @Nullable
    String formatServiceSettings(String pkg);

    @Nullable
    String formatReceiverSettings(String pkg);

    @Nullable
    String formatActivitySettings(String pkg);

    @AllArgsConstructor
    class Impl implements ComponentLoader {

        private Context context;

        public static ComponentLoader create(Context context) {
            return new Impl(context);
        }

        @Override
        public List<CommonPackageInfo> loadInstalledApps(boolean showSystem, Sort sort, int filterOption) {
            String[] packages = XAPMManager.get().getInstalledApps(
                    showSystem ? XAPMManager.FLAG_SHOW_SYSTEM_APP : XAPMManager.FLAG_NONE);
            List<CommonPackageInfo> res = new ArrayList<>();

            for (String p : packages) {
                int flag = LoaderUtil.FLAG_NONE;
                flag |= LoaderUtil.FLAG_INCLUDE_APP_IDLE_INFO;

                if (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_IME_APPS) {
                    flag |= LoaderUtil.FLAG_INCLUDE_IME_INFO;
                }

                if (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_TENCENT_APPS) {
                    flag |= LoaderUtil.FLAG_INCLUDE_TENCENT_INFO;
                }

                if (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_BAIDU_APPS) {
                    flag |= LoaderUtil.FLAG_INCLUDE_BAIDU_INFO;
                }

                if (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_LAUNCHER_APPS) {
                    flag |= LoaderUtil.FLAG_INCLUDE_LAUNCHER_INFO;
                }

                CommonPackageInfo packageInfo = LoaderUtil.constructCommonPackageInfo(context, p, flag);
                if (packageInfo == null) continue;

                boolean match = (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_DISABLED_APPS && packageInfo.isDisabled())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ENABLED_APPS && !packageInfo.isDisabled())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS && !packageInfo.isSystemApp())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS && packageInfo.isSystemApp())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_IME_APPS && packageInfo.isIME())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_GCM_APPS && packageInfo.isGCMSupport())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_MIPUSH_APPS && packageInfo.isMIPushSupport())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_LAUNCHER_APPS && packageInfo.isLauncher())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_TENCENT_APPS && packageInfo.isTencent())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_BAIDU_APPS && packageInfo.isBaidu())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS);
                if (match) {
                    res.add(packageInfo);
                }

                if (false && BuildConfig.DEBUG) {
                    Logger.d("loadInstalledApps: " + packageInfo + ", gcm support: " + packageInfo.isGCMSupport());
                }
            }

            // Add me for debug.
            if (true || BuildConfig.DEBUG) {
                CommonPackageInfo packageInfo = LoaderUtil.constructCommonPackageInfo(context, BuildConfig.APPLICATION_ID);
                if (packageInfo != null) {
                    boolean match = (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_DISABLED_APPS && packageInfo.isDisabled())
                            || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ENABLED_APPS && !packageInfo.isDisabled())
                            || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS && !packageInfo.isSystemApp())
                            || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS && packageInfo.isSystemApp())
                            || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS);
                    if (match) {
                        res.add(packageInfo);
                    }
                }
            }

            sort.performSort(res);
            return res;
        }

        @Override
        public List<CommonPackageInfo> loadInstalledAppsWithOp(boolean showSystem, Sort sort, int filterOption) {
            String[] packages = XAPMManager.get().getInstalledApps(
                    showSystem ? XAPMManager.FLAG_SHOW_SYSTEM_APP_WITHOUT_CORE_APP : XAPMManager.FLAG_NONE);
            List<CommonPackageInfo> res = new ArrayList<>();
            for (String p : packages) {
                CommonPackageInfo packageInfo = LoaderUtil.constructCommonPackageInfo(context, p);
                if (packageInfo == null) continue;

                boolean match = filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS && !packageInfo.isSystemApp())
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS && packageInfo.isSystemApp());
                if (!match) continue;

                updateOpState(packageInfo);
                res.add(packageInfo);
            }
            sort.performSort(res);
            return res;
        }

        private static void updateOpState(CommonPackageInfo info) {
            int modeService = XAPMManager.get()
                    .getPermissionControlBlockModeForPkg(
                            XAppOpsManager.OP_START_SERVICE, info.getPkgName(),
                            false);
            int modeWakelock = XAPMManager.get()
                    .getPermissionControlBlockModeForPkg(
                            XAppOpsManager.OP_WAKE_LOCK, info.getPkgName(),
                            false);
            int modeAlarm = XAPMManager.get()
                    .getPermissionControlBlockModeForPkg(
                            XAppOpsManager.OP_SET_ALARM, info.getPkgName(),
                            false);
            info.setServiceOpAllowed(modeService == XAppOpsManager.MODE_ALLOWED);
            info.setAlarmOpAllowed(modeAlarm == XAppOpsManager.MODE_ALLOWED);
            info.setWakelockOpAllowed(modeWakelock == XAppOpsManager.MODE_ALLOWED);
        }

        @NonNull
        @Override
        public List<ActivityInfoSettings> loadActivitySettings(String pkg) {
            final PackageManager pm = context.getPackageManager();
            final XAPMManager xAshmanManager = XAPMManager.get();

            if (!xAshmanManager.isServiceAvailable()) {
                return Lists.newArrayListWithCapacity(0);
            }

            try {
                Thread.sleep(2000);
                Logger.w("Sleep to give om a little rest");
            } catch (InterruptedException ignored) {

            }

            List<ActivityInfo> activityInfos = ComponentUtil.getActivities(context, pkg);
            if (Collections.isNullOrEmpty(activityInfos)) return Lists.newArrayListWithCapacity(0);

            final List<ActivityInfoSettings> out = Lists.newArrayList();
            Collections.consumeRemaining(activityInfos, new Consumer<ActivityInfo>() {
                @Override
                public void accept(ActivityInfo activityInfo) {
                    try {

                        ActivityInfoSettings settings = new ActivityInfoSettings();
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

            java.util.Collections.sort(out, new AComparator());

            return out;
        }

        @NonNull
        @Override
        public List<ActivityInfoSettings> loadReceiverSettings(String pkg) {
            final PackageManager pm = context.getPackageManager();
            final XAPMManager xAshmanManager = XAPMManager.get();

            if (xAshmanManager == null)
                return Lists.newArrayListWithCapacity(0);

            List<ActivityInfo> activityInfos = ComponentUtil.getBroadcasts(context, pkg);
            if (Collections.isNullOrEmpty(activityInfos)) return Lists.newArrayListWithCapacity(0);

            final List<ActivityInfoSettings> out = Lists.newArrayList();
            Collections.consumeRemaining(activityInfos, new Consumer<ActivityInfo>() {
                @Override
                public void accept(ActivityInfo activityInfo) {
                    try {

                        ActivityInfoSettings settings = new ActivityInfoSettings();
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

            java.util.Collections.sort(out, new AComparator());

            return out;
        }

        @NonNull
        @Override
        public List<ServiceInfoSettings> loadServiceSettings(String pkg) {
            final PackageManager pm = context.getPackageManager();
            final XAPMManager xAshmanManager = XAPMManager.get();

            if (xAshmanManager == null)
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

            java.util.Collections.sort(out, new SComparator());

            return out;
        }

        @Override
        @Nullable
        public String formatServiceSettings(String pkg) {
            List<ServiceInfoSettings> serviceInfoSettings = loadServiceSettings(pkg);
            if (Collections.isNullOrEmpty(serviceInfoSettings)) return null;
            final List<ServiceInfoSettings.Export> exports = Lists.newArrayList();
            Collections.consumeRemaining(serviceInfoSettings, new Consumer<ServiceInfoSettings>() {
                @Override
                public void accept(ServiceInfoSettings serviceInfoSettings) {
                    exports.add(new ServiceInfoSettings.Export(serviceInfoSettings.isAllowed(),
                            ComponentUtil.getComponentName(serviceInfoSettings.getServiceInfo())));
                }
            });
            ServiceInfoSettingsList serviceInfoSettingsList = new ServiceInfoSettingsList(
                    PkgUtil.loadVersionCodeByPkgName(context, pkg), pkg, exports);
            return serviceInfoSettingsList.toJson();
        }

        @Nullable
        @Override
        public String formatReceiverSettings(String pkg) {
            List<ActivityInfoSettings> activityInfoSettingsList = loadReceiverSettings(pkg);
            if (Collections.isNullOrEmpty(activityInfoSettingsList)) return null;
            final List<ActivityInfoSettings.Export> exports = Lists.newArrayList();
            Collections.consumeRemaining(activityInfoSettingsList,
                    new Consumer<ActivityInfoSettings>() {
                        @Override
                        public void accept(ActivityInfoSettings settings) {
                            exports.add(new ActivityInfoSettings.Export(settings.isAllowed(),
                                    ComponentUtil.getComponentName(settings.getActivityInfo())));
                        }
                    });
            ActivityInfoSettingsList list = new ActivityInfoSettingsList(PkgUtil.loadVersionCodeByPkgName(context, pkg), pkg, exports);
            return list.toJson();
        }

        @Nullable
        @Override
        public String formatActivitySettings(String pkg) {
            List<ActivityInfoSettings> activityInfoSettingsList = loadActivitySettings(pkg);
            if (Collections.isNullOrEmpty(activityInfoSettingsList)) return null;
            final List<ActivityInfoSettings.Export> exports = Lists.newArrayList();
            Collections.consumeRemaining(activityInfoSettingsList,
                    new Consumer<ActivityInfoSettings>() {
                        @Override
                        public void accept(ActivityInfoSettings settings) {
                            exports.add(new ActivityInfoSettings.Export(settings.isAllowed(),
                                    ComponentUtil.getComponentName(settings.getActivityInfo())));
                        }
                    });
            ActivityInfoSettingsList list = new ActivityInfoSettingsList(PkgUtil.loadVersionCodeByPkgName(context, pkg), pkg, exports);
            return list.toJson();
        }
    }

    class AComparator implements Comparator<ActivityInfoSettings> {
        public int compare(ActivityInfoSettings o1, ActivityInfoSettings o2) {
            if (o1.isAllowed() != o2.isAllowed()) {
                return o1.isAllowed() ? 1 : -1;
            }
            return new PinyinComparator().compare(o1.simpleName(), o2.simpleName());
        }
    }

    class SComparator implements Comparator<ServiceInfoSettings> {
        public int compare(ServiceInfoSettings o1, ServiceInfoSettings o2) {
            if (o1.isAllowed() != o2.isAllowed()) {
                return o1.isAllowed() ? 1 : -1;
            }
            return new PinyinComparator().compare(o1.simpleName(), o2.simpleName());
        }
    }
}
