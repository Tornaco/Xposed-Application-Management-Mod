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

import github.tornaco.xposedmoduletest.BuildConfig;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.compat.os.XAppOpsManagerRes;
import github.tornaco.xposedmoduletest.model.CommonPackageInfo;
import github.tornaco.xposedmoduletest.model.Permission;
import github.tornaco.xposedmoduletest.ui.activity.common.CommonPackageInfoListActivity;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

import static github.tornaco.xposedmoduletest.compat.os.XAppOpsManager._NUM_OP_DEF;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface PermissionLoader {

    @NonNull
    List<Permission> load(String pkg, int category, int filterOption);

    @NonNull
    List<CommonPackageInfo> loadByOp(int op, int category, int filterOption);

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
        public List<Permission> load(final String pkg, final int category, int filterOption) {

            if (!XAPMManager.get().isServiceAvailable()) {
                return new ArrayList<>(0);
            }

            String[] decleared = pkg == null ? null : PkgUtil.getAllDeclaredPermissions(context, pkg);

            Set<String> permSet = Sets.newHashSet(decleared == null ? new String[0] : decleared);

            int OP_SIZE = _NUM_OP_DEF;

            final List<Permission> permissions = new ArrayList<>();

            boolean loadDefaultOp = filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_OP
                    || filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_DEFAULT_OP;
            if (loadDefaultOp) for (int code = 0; code < OP_SIZE; code++) {

                String s = XAppOpsManager.opToPermission(code);

                // Here we check if this is dummy one.
                boolean isDummy = pkg == null || XAPMManager.APPOPS_WORKAROUND_DUMMY_PACKAGE_NAME.equals(pkg);

                if (!isDummy && (s != null && !permSet.contains(s))) {
                    continue;
                }

                Permission p = new Permission();
                p.setPkgName(pkg);
                p.setPermission(s);

                if (code == XAppOpsManager.OP_NONE) {
                    Logger.w("Un-support per control: " + s);
                    continue;
                }


                if (code == XAppOpsManager.OP_WAKE_LOCK) {
                    Logger.w("Tem skip per control: " + s);
                    continue;
                }

//                if (!BuildConfig.DEBUG && code == XAppOpsManager.OP_POST_NOTIFICATION) {
//                    Logger.w("Tem skip per control: " + s);
//                    continue;
//                }

                p.setCode(code);

                String name = XAppOpsManagerRes.getOpLabel(context, code);
                p.setName(name);

                String summary = XAppOpsManagerRes.getOpSummary(context, code);
                p.setSummary(summary);

                p.setIconRes(XAppOpsManagerRes.opToIconRes(code));

                if (pkg != null) {
                    p.setMode(XAPMManager.get().getPermissionControlBlockModeForPkg(code, pkg, false));
                } else {
                    p.setMode(XAppOpsManager.MODE_ALLOWED);
                }

                Logger.d("Add perm: " + p);

                permissions.add(p);
            }

            java.util.Collections.sort(permissions, new PermComparator());

            boolean loadExtOp = filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_OP
                    || filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_EXT_OP;
            // Add our extra permissions.
            if (loadExtOp) for (int ecode : XAppOpsManager.EXTRA_OPS) {
                Permission p = new Permission();
                p.setPkgName(pkg);
                p.setPermission(null);
                p.setCode(ecode);
                p.setCategory(XAppOpsManager.CATEGORY_EXTRA);
                String name = XAppOpsManagerRes.getOpLabel(context, ecode);
                p.setName(name);
                String summary = XAppOpsManagerRes.getOpSummary(context, ecode);
                p.setSummary(summary);
                p.setIconRes(XAppOpsManagerRes.opToIconRes(ecode));
                p.setMode(XAPMManager.get().getPermissionControlBlockModeForPkg(ecode, pkg, false));
                Logger.d("Add perm: " + p);
                permissions.add(p);
            }

            java.util.Collections.reverse(permissions);

            return permissions;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadByOp(int op, int category, int filterOption) {
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
                Set<String> decleared = Sets.newHashSet(PkgUtil.getAllDeclaredPermissions(context, info.packageName));
                String permission = XAppOpsManager.opToPermission(op);
                if (BuildConfig.DEBUG) {
                    Logger.v("decleared perm: " + Arrays.toString(decleared.toArray()));
                    Logger.v("ops perm: " + permission);
                }
                if (permission == null || decleared.contains(permission)) {
                    CommonPackageInfo c = new CommonPackageInfo();
                    c.setPkgName(info.packageName);
                    c.setVersion(XAPMManager.get().getPermissionControlBlockModeForPkg(op, c.getPkgName(), false));
                    c.setSystemApp(PkgUtil.isSystemApp(context, info.packageName));

                    boolean match = (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_ALL_APPS)
                            || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_3RD_APPS && !c.isSystemApp())
                            || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_SYSTEM_APPS && c.isSystemApp());

                    if (match) {
                        c.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(context, info.packageName)));
                        c.setAppLevel(XAPMManager.get().getAppLevel(info.packageName));
                        res.add(c);
                    }
                }
            }
            return res;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> loadOps(int filterOption) {
            int opCount = XAppOpsManager._NUM_OP;
            List<CommonPackageInfo> res = new ArrayList<>(opCount);
            for (int i = 0; i < opCount; i++) {
                CommonPackageInfo c = new CommonPackageInfo();
                c.setAppName(XAppOpsManagerRes.getOpLabel(context, i));
                c.setPayload(new String[]{XAppOpsManagerRes.getOpSummary(context, i)});
                c.setPkgName(null);
                c.setChecked(false);
                c.setVersion(i);

                boolean match = (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_DEFAULT_OP && i < _NUM_OP_DEF)
                        || (filterOption == CommonPackageInfoListActivity.FilterOption.OPTION_EXT_OP && i >= _NUM_OP_DEF)
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
            if (o1.getCode() == o2.getCode()) {
                return 0;
            }
            return o1.getCode() > o2.getCode() ? -1 : 1;
        }
    }
}
