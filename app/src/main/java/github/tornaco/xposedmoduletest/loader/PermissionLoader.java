package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.compat.os.AppOpsManagerCompat;
import github.tornaco.xposedmoduletest.model.Permission;
import github.tornaco.xposedmoduletest.xposed.app.XAshmanManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface PermissionLoader {

    @NonNull
    List<Permission> load(String pkg, int category);

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
            if (!XAshmanManager.get().isServiceAvailable()) {
                return new ArrayList<>(0);
            }

            String[] decleared = PkgUtil.getAllDeclaredPermissions(context, pkg);
            if (decleared == null || decleared.length == 0) return new ArrayList<>(0);

            final List<Permission> permissions = new ArrayList<>();

            Collections.consumeRemaining(decleared, new Consumer<String>() {
                @Override
                public void accept(String s) {

                    Permission p = new Permission();
                    p.setPkgName(pkg);
                    p.setPermission(s);

                    int code = AppOpsManagerCompat.permissionToOpCode(s);

                    if (code == AppOpsManagerCompat.OP_NONE) {
                        Logger.w("Un-support per control: " + s);
                        return;
                    }

                    p.setCode(code);

                    String name = AppOpsManagerCompat.getOpLabel(context, code);
                    p.setName(name);

                    String summary = AppOpsManagerCompat.getOpSummary(context, code);
                    p.setSummary(summary);

                    p.setIconRes(AppOpsManagerCompat.opToIconRes(code));

                    p.setMode(XAshmanManager.get().getPermissionControlBlockModeForPkg(code, pkg));

                    Logger.d("Add perm: " + p);

                    permissions.add(p);
                }
            });

            // Add our extra permissions.
            for (int ecode : AppOpsManagerCompat.EXTRA_OPS) {
                Permission p = new Permission();
                p.setPkgName(pkg);
                p.setPermission(null);
                p.setCode(ecode);
                String name = AppOpsManagerCompat.getOpLabel(context, ecode);
                p.setName(name);
                String summary = AppOpsManagerCompat.getOpSummary(context, ecode);
                p.setSummary(summary);
                p.setIconRes(AppOpsManagerCompat.opToIconRes(ecode));
                p.setMode(XAshmanManager.get().getPermissionControlBlockModeForPkg(ecode, pkg));
                Logger.d("Add perm: " + p);
                permissions.add(p);
            }

            java.util.Collections.sort(permissions, new PermComparator());

            return permissions;
        }
    }

    class PermComparator implements Comparator<Permission> {
        public int compare(Permission o1, Permission o2) {
            return o1.getCode() > o2.getCode() ? -1 : 1;
        }
    }
}
