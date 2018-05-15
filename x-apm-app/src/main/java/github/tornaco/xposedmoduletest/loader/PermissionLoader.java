package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;

import com.google.common.collect.Sets;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.model.Permission;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface PermissionLoader {

    @NonNull
    List<Permission> load(String pkg, int category);

    @NonNull
    List<CommonPackageInfo> loadByOp(int op, int category, boolean showSystem);

    @NonNull
    List<CommonPackageInfo> loadOps(int filterOption);

    class Impl implements PermissionLoader {

        public static PermissionLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<Permission> load(final String pkg, final int category) {

            if (!XAPMManager.get().isServiceAvailable()) {
                return new ArrayList<>(0);
            }

            String[] decleared = PkgUtil.getAllDeclaredPermissions(context, pkg);

            Set<String> permSet = Sets.newHashSet(decleared == null ? new String[0] : decleared);

            int OP_SIZE = AppOpsManagerCompat._NUM_OP_DEF;

            final List<Permission> permissions = new ArrayList<>();

            for (int code = 0; code < OP_SIZE; code++) {

                String s = AppOpsManagerCompat.opToPermission(code);

                // Here we check if this is dummy one.
                boolean isDummy = XAPMManager.APPOPS_WORKAROUND_DUMMY_PACKAGE_NAME.equals(pkg);

                if (!isDummy && (s != null && !permSet.contains(s))) {
                    continue;
                }

                Permission p = new Permission();
                p.setPkgName(pkg);
                p.setPermission(s);

                if (code == AppOpsManagerCompat.OP_NONE) {
                    Logger.w("Un-support per control: " + s);
                    continue;
                }


                if (code == AppOpsManagerCompat.OP_WAKE_LOCK) {
                    Logger.w("Tem skip per control: " + s);
                    continue;
                }

//                if (!BuildConfig.DEBUG && code == AppOpsManagerCompat.OP_POST_NOTIFICATION) {
//                    Logger.w("Tem skip per control: " + s);
//                    continue;
//                }

                p.setCode(code);

                String name = AppOpsManagerCompat.getOpLabel(context, code);
                p.setName(name);

                String summary = AppOpsManagerCompat.getOpSummary(context, code);
                p.setSummary(summary);

                p.setIconRes(AppOpsManagerCompat.opToIconRes(code));

                p.setMode(XAPMManager.get().getPermissionControlBlockModeForPkg(code, pkg, false));

                Logger.d("Add perm: " + p);

                permissions.add(p);
            }

            java.util.Collections.sort(permissions, new PermComparator());

            // Add our extra permissions.
            for (int ecode : AppOpsManagerCompat.EXTRA_OPS) {
                Permission p = new Permission();
                p.setPkgName(pkg);
                p.setPermission(null);
                p.setCode(ecode);
                p.setCategory(AppOpsManagerCompat.CATEGORY_EXTRA);
                String name = AppOpsManagerCompat.getOpLabel(context, ecode);
                p.setName(name);
                String summary = AppOpsManagerCompat.getOpSummary(context, ecode);
                p.setSummary(summary);
                p.setIconRes(AppOpsManagerCompat.opToIconRes(ecode));
                p.setMode(XAPMManager.get().getPermissionControlBlockModeForPkg(ecode, pkg, false));
                Logger.d("Add perm: " + p);
                permissions.add(p);
            }

            java.util.Collections.reverse(permissions);

            return permissions;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadByOp(int op, int category, boolean showSystem) {
            if (!XAPMManager.get().isServiceAvailable()) {
                return new ArrayList<>(0);
            }
            final PackageManager pm = this.context.getPackageManager();
            // Filter all apps.
            List<ApplicationInfo> applicationInfos =
                    android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N ?
                            pm.getInstalledApplications(android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES)
                            : pm.getInstalledApplications(android.content.pm.PackageManager.GET_UNINSTALLED_PACKAGES);

            List<CommonPackageInfo> res = new ArrayList<>();

            for (ApplicationInfo info : applicationInfos) {
                String[] decleared = PkgUtil.getAllDeclaredPermissions(context, info.packageName);
                if (decleared == null || decleared.length == 0) {
                    continue;
                }
                String permission = AppOpsManagerCompat.opToPermission(op);
                if (permission == null || (Arrays.binarySearch(decleared, permission) >= 0)) {
                    CommonPackageInfo c = new CommonPackageInfo();
                    c.setPkgName(info.packageName);
                    c.setVersion(XAPMManager.get().getPermissionControlBlockModeForPkg(op, c.getPkgName(), false));
                    c.setSystemApp(PkgUtil.isSystemApp(context, info.packageName));

                    if (c.isSystemApp() && !showSystem) {
                        continue;
                    }

                    c.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(context, info.packageName)));
                    c.setAppLevel(XAPMManager.get().getAppLevel(info.packageName));
                    res.add(c);
                }
            }
            return res;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadOps(int filterOption) {
            int opCount = AppOpsManagerCompat._NUM_OP;
            List<CommonPackageInfo> res = new ArrayList<>(opCount);
            for (int i = 0; i < opCount; i++) {
                CommonPackageInfo c = new CommonPackageInfo();
                c.setAppName(AppOpsManagerCompat.getOpLabel(context, i));
                c.setPayload(new String[]{AppOpsManagerCompat.getOpSummary(context, i)});
                c.setPkgName(null);
                c.setChecked(false);
                c.setVersion(i);

                boolean match = (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_DEFAULT_OP && i < 70)
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_EXT_OP && i >= 70)
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_OP);

                if (match) {
                    res.add(c);
                }
            }
            return res;
        }
    }

    class PermComparator implements Comparator<Permission> {
        public int compare(Permission o1, Permission o2) {
            return o1.getCode() > o2.getCode() ? -1 : 1;
        }
    }
}
