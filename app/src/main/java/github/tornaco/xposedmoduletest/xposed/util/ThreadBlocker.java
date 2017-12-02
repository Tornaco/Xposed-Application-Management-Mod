package github.tornaco.xposedmoduletest.xposed.util;

/**
 * Created by guohao4 on 2017/12/2.
 * Email: Tornaco@163.com
 */

public abstract class ThreadBlocker {

    public static void block1s() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {

        }
    }

    public static void block2s() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {

        }
    }
}
