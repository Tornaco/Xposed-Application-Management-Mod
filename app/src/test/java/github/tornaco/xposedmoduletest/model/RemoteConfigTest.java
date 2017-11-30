package github.tornaco.xposedmoduletest.model;

import com.google.gson.Gson;

import org.junit.Test;
import org.newstand.logger.Logger;

/**
 * Created by guohao4 on 2017/11/30.
 * Email: Tornaco@163.com
 */
public class RemoteConfigTest {
    @Test
    public void foo() {
        RemoteConfig config = RemoteConfig.builder().donate(true)
                .shutdown(false)
                .build();
        String js = new Gson().toJson(config);
        Logger.d("js: " + js);
    }
}