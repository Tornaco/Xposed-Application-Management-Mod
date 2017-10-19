package github.tornaco.xposedmoduletest.x;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

public interface XMode {
    /**
     * Passed.
     */
    int MODE_ALLOWED = 0;
    /**
     * By passed.
     */
    int MODE_IGNORED = 1;
    /**
     * Canceled or Wrong pwd.
     */
    int MODE_DENIED = -1;
}
