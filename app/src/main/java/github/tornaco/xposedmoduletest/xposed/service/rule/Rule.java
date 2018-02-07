package github.tornaco.xposedmoduletest.xposed.service.rule;

import lombok.Data;

/**
 * Created by guohao4 on 2018/2/7.
 * Email: Tornaco@163.com
 */
@Data
public class Rule {
    private RuleAction ruleAction;
    private RuleTargetType[] ruleTargetTypes;
    private String[] ruleTargets;// "XXXXX.XXX XXXX.XXXXXXX"

    public String toInternalPattern() {
        return ruleAction.name().toUpperCase() + " " + ruleTargets[0] + " " + ruleTargets[1];
    }
}
