package github.tornaco.xposedmoduletest.xposed.service.rule;

/**
 * Created by guohao4 on 2018/2/7.
 * Email: Tornaco@163.com
 */

public interface RuleParser {

    Rule parse(String args);

    String getNextOption();

    String getNextArg();

    String peekNextArg();

    String getNextArgRequired();

    class Factory {
        public static RuleParser newParser() {
            return new RuleParserImpl();
        }
    }
}
