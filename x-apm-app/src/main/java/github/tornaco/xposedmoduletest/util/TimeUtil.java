package github.tornaco.xposedmoduletest.util;

/**
 * Created by guohao4 on 2018/1/4.
 * Email: Tornaco@163.com
 */

public class TimeUtil {

    public static String formatDuration(long time) {
        return android.text.format.DateUtils.formatElapsedTime(time / 1000);
    }

}
