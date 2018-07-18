package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.util.LruCache;

import org.newstand.logger.Logger;

import java.util.Collections;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.util.Singleton;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/12/18.
 * Email: Tornaco@163.com
 */

public class LoaderUtil {

    // Extra payload flags.
    public static final int FLAG_INCLUDE_IME_INFO = 0x00000001;
    public static final int FLAG_INCLUDE_TENCENT_INFO = 0x00000002;
    public static final int FLAG_INCLUDE_BAIDU_INFO = 0x00000004;
    public static final int FLAG_INCLUDE_LAUNCHER_INFO = 0x00000008;
    public static final int FLAG_INCLUDE_APP_IDLE_INFO = 0x00000010;

    public static final int FLAG_NONE = 0;

    public static CommonPackageInfo constructCommonPackageInfo(Context context, String pkg) {
        return constructCommonPackageInfo(context, pkg, FLAG_NONE);
    }

    public static CommonPackageInfo constructCommonPackageInfo(Context context, String pkg, int flag) {
        if (!PkgUtil.isPkgInstalled(context, pkg)) return null;

        // Retrieve from cache.
        String key = constructKeyForPackageAndFlags(pkg, flag);
        CommonPackageInfo cached = LoaderCache.getInstance().get(key);
        if (cached != null) {
            // Reset selection state.
            CommonPackageInfo dup = CommonPackageInfo.duplicate(cached);
            dup.setChecked(false);
            dup.setAppIdle(XAPMManager.get().isAppInactive(pkg, UserHandle.USER_CURRENT));
            // Force read GCM state.
            dup.setGCMSupport(XAPMManager.get().isServiceAvailable() && XAPMManager.get().isGCMSupportPackage(pkg));
            dup.setMIPushSupport(XAPMManager.get().isServiceAvailable() && XAPMManager.get().isMiPushSupportPackage(pkg));
            inflateEnableState(dup);
            return dup;
        }

        String name = String.valueOf(PkgUtil.loadNameByPkgName(context, pkg));
        CommonPackageInfo p = new CommonPackageInfo();
        p.setAppName(name);
        p.setPkgName(pkg);

        // p.setInstalledTime(PkgUtil.loadInstalledTimeByPkgName(context, pkg));
        p.setAppLevel(XAPMManager.get().getAppLevel(pkg));
        p.setSystemApp(PkgUtil.isSystemApp(context, pkg));

        p.setGCMSupport(XAPMManager.get().isServiceAvailable() && XAPMManager.get().isGCMSupportPackage(pkg));
        p.setMIPushSupport(XAPMManager.get().isServiceAvailable() && XAPMManager.get().isMiPushSupportPackage(pkg));

        inflateEnableState(p);

        if ((flag & FLAG_INCLUDE_IME_INFO) != 0) {
            // Check if it is IME.
            p.setIME(PkgUtil.isInputMethodApp(context, pkg));
            Logger.i("FLAG_INCLUDE_IME_INFO");
        }

        if ((flag & FLAG_INCLUDE_LAUNCHER_INFO) != 0) {
            p.setLauncher(PkgUtil.isHomeApp(context, pkg));
            Logger.i("FLAG_INCLUDE_LAUNCHER_INFO");
        }

        if ((flag & FLAG_INCLUDE_TENCENT_INFO) != 0) {
            p.setTencent(pkg != null && pkg.contains("tencent"));
            Logger.i("FLAG_INCLUDE_TENCENT_INFO");
        }

        if ((flag & FLAG_INCLUDE_BAIDU_INFO) != 0) {
            p.setBaidu(pkg != null && pkg.contains("baidu"));
            Logger.i("FLAG_INCLUDE_BAIDU_INFO");
        }

        if (true) {
            p.setAppIdle(XAPMManager.get().isAppInactive(pkg, UserHandle.USER_CURRENT));
            Logger.i("FLAG_INCLUDE_APP_IDLE_INFO");
        }

        LoaderCache.getInstance().put(key, p);

        return p;
    }

    private static void inflateEnableState(CommonPackageInfo p) {
        if (XAPMManager.get().isServiceAvailable()) {
            int state = XAPMManager.get().getApplicationEnabledSetting(p.getPkgName());
            boolean disabled = state != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    && state != PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
            p.setDisabled(disabled);
        }
    }

    private static String constructKeyForPackageAndFlags(String pkg, int flag) {
        return pkg + "-" + flag;
    }

    public static void stateSort(List<CommonPackageInfo> commonPackageInfos) {
        final PinyinComparator pinyinComparator = new PinyinComparator();

        Collections.sort(commonPackageInfos, (o1, o2) -> {
            // Check if this app is disabled.
            boolean o1D = o1.isDisabled();
            boolean o2D = o2.isDisabled();
            if (o1D != o2D) {
                return o1D ? -1 : 1;
            }

            boolean o1S = o1.isSystemApp();
            boolean o2S = o2.isSystemApp();
            if (o1S != o2S) {
                return o1S ? 1 : -1;
            }
            return pinyinComparator.compare(o1.getAppName(), o2.getAppName());
        });
    }

    public static void opSort(List<CommonPackageInfo> commonPackageInfos) {
        final PinyinComparator pinyinComparator = new PinyinComparator();

        Collections.sort(commonPackageInfos, (o1, o2) -> {

            int o1O = o1.getOpDistance();
            int o2O = o2.getOpDistance();
            if (o1O != o2O) {
                return o1O > o2O ? 1 : -1;
            }

            boolean o1S = o1.isSystemApp();
            boolean o2S = o2.isSystemApp();
            if (o1S != o2S) {
                return o1S ? 1 : -1;
            }
            return pinyinComparator.compare(o1.getAppName(), o2.getAppName());
        });
    }

    public static void commonSort(List<? extends CommonPackageInfo> commonPackageInfos) {
        final PinyinComparator pinyinComparator = new PinyinComparator();

        Collections.sort(commonPackageInfos, (o1, o2) -> {
            boolean o1S = o1.isSystemApp();
            boolean o2S = o2.isSystemApp();
            if (o1S != o2S) {
                return o1S ? 1 : -1;
            }
            return pinyinComparator.compare(o1.getAppName(), o2.getAppName());
        });
    }

    private static class LoaderCache {

        private LruCache<String, CommonPackageInfo> mCache;

        private static final Singleton<LoaderCache> sMe = new Singleton<LoaderCache>() {
            @Override
            protected LoaderCache create() {
                return new LoaderCache();
            }
        };

        public static LoaderCache getInstance() {
            return sMe.get();
        }

        private LoaderCache() {
            int maxMemory = (int) (Runtime.getRuntime().maxMemory());
            int cacheSize = maxMemory / 32;
            mCache = new LruCache<String, CommonPackageInfo>(cacheSize) {
                @Override
                protected int sizeOf(String key, CommonPackageInfo value) {
                    return 512; // Assume 512 per info.
                }
            };
        }

        public void put(String key, CommonPackageInfo info) {
            if (key != null && info != null) {
                mCache.put(key, info);
            }
        }

        public CommonPackageInfo get(String key) {
            return key == null ? null : mCache.get(key);
        }
    }
}
