package github.tornaco.xposedmoduletest.util;

import com.google.gson.Gson;

/**
 * Created by guohao4 on 2017/12/28.
 * Email: Tornaco@163.com
 */

public abstract class GsonUtil {

    private static final Singleton<Gson> sGson = new Singleton<Gson>() {
        @Override
        protected Gson create() {
            return new Gson();
        }
    };

    public static Gson getGson() {
        return sGson.get();
    }
}
