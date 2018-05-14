package github.tornaco.xposedmoduletest.xposed.service.rule;

import java.util.regex.Pattern;

/**
 * Created by guohao4 on 2018/2/7.
 * Email: Tornaco@163.com
 */

public enum RuleTargetType implements RuleMatcher {

    ALL {
        @Override
        public boolean match(String target) {
            return "*".equals(target);
        }
    },
    PKG {
        @Override
        public boolean match(String target) {
            return target != null && PrebuiltPatterns.PATTERN_PACKAGE_NAME.matcher(target).matches();
        }
    },
    COMPONENT {
        @Override
        public boolean match(String target) {
            return target != null && target.contains("/") && !target.contains(" ");
        }
    };

    public static RuleTargetType from(String target) {
        if (target == null) return null;
        for (RuleTargetType r : values()) {
            if (r.match(target.trim())) return r;
        }
        return null;
    }
}

interface RuleMatcher {
    boolean match(String target);
}

class PrebuiltPatterns {
    static final Pattern PATTERN_PACKAGE_NAME = Pattern.compile("[a-zA-Z]+[0-9a-zA-Z_]*(\\.[a-zA-Z]+[0-9a-zA-Z_]*)*");
}
