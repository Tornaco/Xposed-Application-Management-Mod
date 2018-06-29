package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;
import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public interface AppOpsTemplateLoader {

    @NonNull
    List<AppOpsTemplate> loadAll();

    @AllArgsConstructor
    class Impl implements AppOpsTemplateLoader {

        private Context context;

        public static AppOpsTemplateLoader create(Context context) {
            return new Impl(context);
        }

        @NonNull
        @Override
        public List<AppOpsTemplate> loadAll() {
            if (!XAPMManager.get().isServiceAvailable()) {
                return new ArrayList<>(0);
            }
            XAPMManager ag = XAPMManager.get();
            List<AppOpsTemplate> res = ag.getAppOpsTemplates();
            Collections.sort(res, new TemplateComparator());
            return res;
        }
    }


    class TemplateComparator implements Comparator<AppOpsTemplate> {
        public int compare(AppOpsTemplate o1, AppOpsTemplate o2) {
            return Long.compare(o2.getCreatedAtMills(), o1.getCreatedAtMills());
        }
    }
}
