package github.tornaco.xposedmoduletest.loader;

import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.common.collect.Lists;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import github.tornaco.android.common.Consumer;
import github.tornaco.xposedmoduletest.R;
import github.tornaco.xposedmoduletest.bean.ComponentReplacement;
import github.tornaco.xposedmoduletest.bean.ComponentReplacementList;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.util.PinyinComparator;
import github.tornaco.xposedmoduletest.xposed.app.XAppLockManager;
import github.tornaco.xposedmoduletest.xposed.util.PkgUtil;
import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public interface ComponentReplacementsLoader {

    @NonNull
    List<ComponentReplacement> loadAllFromProvider();

    @NonNull
    List<ComponentReplacement> loadAllFromAPMS();

    @NonNull
    List<ComponentReplacement> parseJson(String json);

    @NonNull
    List<ComponentReplacement> parseFromAssets(String path);

    @AllArgsConstructor
    class Impl implements ComponentReplacementsLoader {

        private Context context;

        public static ComponentReplacementsLoader create(Context context) {
            return new Impl(context);
        }

        @NonNull
        @Override
        public List<ComponentReplacement> loadAllFromProvider() {
            DaoManager daoManager = DaoManager.getInstance();
            DaoSession session = daoManager.getSession(context);
            if (session == null) return new ArrayList<>(0);
            List<ComponentReplacement> all = session.getComponentReplacementDao().loadAll();
            if (all == null) return new ArrayList<>(0);
            github.tornaco.android.common.Collections.consumeRemaining(all,
                    new Consumer<ComponentReplacement>() {
                        @Override
                        public void accept(ComponentReplacement componentReplacement) {
                            componentReplacement.setAppPackageName(componentReplacement.getCompFromPackageName());
                            componentReplacement.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(context,
                                    componentReplacement.getAppPackageName())));
                        }
                    });
            Collections.sort(all, new SComparator());

            try {
                Logger.d(new ComponentReplacementList(all).toJson());
            } catch (Throwable ignored) {
            }

            return all;
        }

        @NonNull
        @Override
        public List<ComponentReplacement> loadAllFromAPMS() {
            if (!XAppLockManager.get().isServiceAvailable()) {
                return new ArrayList<>(0);
            }
            XAppLockManager ag = XAppLockManager.get();
            Map replacements = ag.getComponentReplacements();
            List<ComponentReplacement> res = new ArrayList<>();
            for (Object k : replacements.keySet()) {
                String key = k.toString();
                ComponentName from = ComponentName.unflattenFromString(key);
                if (from == null) continue;
                ComponentReplacement cr = new ComponentReplacement();
                cr.setCompFromPackageName(from.getPackageName());
                cr.setCompFromClassName(from.getClassName());
                cr.setAppPackageName(from.getPackageName());
                cr.setAppName(String.valueOf(PkgUtil.loadNameByPkgName(context, from.getPackageName())));

                if (!PkgUtil.isPkgInstalled(context, from.getPackageName())) {
                    cr.setAppName(context.getString(R.string.title_from_no_install));
                }

                // Inflate to.
                Object v = replacements.get(k);
                ComponentName to = v == null ? null : ComponentName.unflattenFromString(v.toString());
                if (to != null) {
                    cr.setCompToPackageName(to.getPackageName());
                    cr.setCompToClassName(to.getClassName());

                    if (!PkgUtil.isPkgInstalled(context, to.getPackageName())) {
                        cr.setAppName(cr.getAppName() + " " + context.getString(R.string.title_to_no_install));
                    }
                }

                res.add(cr);
            }
            return res;
        }

        @NonNull
        @Override
        public List<ComponentReplacement> parseJson(String json) {
            try {
                ComponentReplacementList componentReplacementList = ComponentReplacementList.fromJson(json);
                if (componentReplacementList != null && componentReplacementList.getList() != null)
                    return componentReplacementList.getList();
            } catch (Throwable ignored) {

            }
            try {
                ComponentReplacement componentReplacement = ComponentReplacement.fromJson(json);
                if (componentReplacement != null) {
                    return Lists.newArrayList(componentReplacement);
                }
            } catch (Throwable ignored) {

            }
            return new ArrayList<>(0);
        }

        @NonNull
        @Override
        public List<ComponentReplacement> parseFromAssets(String path) {
            return null;
        }
    }


    class SComparator implements Comparator<ComponentReplacement> {
        public int compare(ComponentReplacement o1, ComponentReplacement o2) {
            return new PinyinComparator().compare(o1.getAppName(), o2.getAppName());
        }
    }
}
