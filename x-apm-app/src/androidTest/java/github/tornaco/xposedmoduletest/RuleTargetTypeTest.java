package github.tornaco.xposedmoduletest;

import org.junit.Assert;
import org.junit.Test;
import org.newstand.logger.Logger;

import java.util.Arrays;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.service.rule.RuleParser;

/**
 * Created by guohao4 on 2018/2/7.
 * Email: Tornaco@163.com
 */

public class RuleTargetTypeTest {

    // ALLOW tornaco.github.abc *
    // ALLOW * com.tencent.xxx

    public static final String[] TESTS = new String[]{
            "ALLOW com.a.b.c *",
            "DENY com.a.b.c *",
            "DENY com.a.b.c c.a.c",
            "DEN com.a.b.c *",
            "DENY com.a.b,c abc.c",
            "com.a?.b,c",
            "com.a_.b,c",
            "com.1.b,c",
            "com",
            "124.88",
            "124/88",
            "abc/ddff",
            "abc/ddff_8",
            "",
            "*",
            "* ",
            " "
    };

    @Test
    public void testRules() {
        RuleParser p = RuleParser.Factory.newParser();
        for (String t : TESTS) {
            Logger.d("testRules rule %s", t, p.parse(t));
            boolean added = XAPMManager.get().addOrRemoveStartRules(t, true);
            Logger.d("Add rule %s", added);

            String[] rules = XAPMManager.get().getStartRules();
            Logger.d("All rules %s", Arrays.toString(rules));

            if (added) {
                Assert.assertTrue(XAPMManager.get().addOrRemoveStartRules(t, false));

                rules = XAPMManager.get().getStartRules();
                Logger.d("All rules %s", Arrays.toString(rules));

            }
        }

        XAPMManager.get().addOrRemoveStartRules("DENY * com.sina.weibo", true);

    }
}
