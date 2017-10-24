package github.tornaco.xposedmoduletest.x;

/**
 * Created by guohao4 on 2017/10/19.
 * Email: Tornaco@163.com
 */

public enum XStatus {
    GOOD, ERROR, WITH_WARN, UNKNOWN;

    public static XStatus valueOf(int ord) {
        for (XStatus x : values()) {
            if (x.ordinal() == ord) return x;
        }
        return UNKNOWN;
    }
}
