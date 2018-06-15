package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import org.newstand.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.JavaScript;
import lombok.AllArgsConstructor;

/**
 * Created by guohao4 on 2017/11/17.
 * Email: Tornaco@163.com
 */

public interface JsLoader {

    @NonNull
    List<JavaScript> loadAll();

    @AllArgsConstructor
    class Impl implements JsLoader {

        private Context context;

        public static JsLoader create(Context context) {
            return new Impl(context);
        }

        @NonNull
        @Override
        public List<JavaScript> loadAll() {
            if (!XAPMManager.get().isServiceAvailable()) {
                return new ArrayList<>(0);
            }
            XAPMManager ag = XAPMManager.get();
            List<JavaScript> res = ag.getSavedJses();
            Logger.i("JsLoader: " + res.size());
            Collections.sort(res, new JSComparator());
            return res;
        }
    }


    class JSComparator implements Comparator<JavaScript> {
        public int compare(JavaScript o1, JavaScript o2) {
            return Long.compare(o2.getCreatedAt(), o1.getCreatedAt());
        }
    }
}
