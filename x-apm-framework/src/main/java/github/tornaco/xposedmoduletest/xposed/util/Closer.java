package github.tornaco.xposedmoduletest.xposed.util;

import java.io.Closeable;

/**
 * Created by Nick@NewStand.org on 2017/3/9 13:38
 * E-Mail: NewStand@163.com
 * All right reserved.
 */

public abstract class Closer {
    public static void closeQuietly(Closeable closeable) {
        if (closeable == null) return;
        try {
            closeable.close();
        } catch (Exception ignore) {
        }
    }
}
