package github.tornaco.xposedmoduletest.loader;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import github.tornaco.xposedmoduletest.model.CommonPackageInfo;

/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public interface FocusedAppLoader {

    @NonNull
    List<CommonPackageInfo> load();

    class Impl implements FocusedAppLoader {

        public static FocusedAppLoader create(Context context) {
            return new Impl(context);
        }

        private Context context;

        private Impl(Context context) {
            this.context = context;
        }

        @NonNull
        @Override
        public List<CommonPackageInfo> load() {
            List<CommonPackageInfo> out = new ArrayList<>();
            return out;
        }
    }
}
