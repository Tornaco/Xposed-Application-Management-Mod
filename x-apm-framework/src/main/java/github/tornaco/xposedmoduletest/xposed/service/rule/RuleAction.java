package github.tornaco.xposedmoduletest.xposed.service.rule;

/**
 * Created by guohao4 on 2018/2/7.
 * Email: Tornaco@163.com
 */

public enum RuleAction {

    ALLOW, DENY, KEEP, SKIP, ADDAPP, ALWAYS;

    public static RuleAction from(String action) {
        for (RuleAction a : values()) {
            if (a.name().equalsIgnoreCase(action)) return a;
        }
        return null;
    }
}
