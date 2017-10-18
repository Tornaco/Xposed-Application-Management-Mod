package dev.tornaco.vangogh;

import android.support.test.InstrumentationRegistry;

import org.junit.Test;

/**
 * Created by guohao4 on 2017/8/25.
 * Email: Tornaco@163.com
 */
public class VangoghTest {
    @Test
    public void from() throws Exception {
        Vangogh.with(InstrumentationRegistry.getContext());
    }

}