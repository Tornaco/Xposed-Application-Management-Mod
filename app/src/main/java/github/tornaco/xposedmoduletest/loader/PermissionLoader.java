package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.android.common.Collections;
import github.tornaco.android.common.Consumer;
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
        public List<Permission> load(final String pkg, int category) {
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
                    p.setName(XAshmanManager.permToString(context, s));
                    p.setIconRes(XAshmanManager.permToDrawableRes(s));
                    p.setState(XAshmanManager.get().getPermissionControlBlockModeForUid(s, pkg));

                    Logger.d("Add perm: " + p);

                    permissions.add(p);
                }
            });



            return permissions;
        }
    }
}
