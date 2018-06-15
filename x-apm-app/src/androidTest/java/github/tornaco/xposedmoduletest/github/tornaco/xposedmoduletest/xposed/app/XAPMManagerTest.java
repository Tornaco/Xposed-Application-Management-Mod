package github.tornaco.xposedmoduletest.github.tornaco.xposedmoduletest.xposed.app;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.JavaScript;

/**
 * Created by Tornaco on 2018/6/15 12:52.
 * This file is writen for project X-APM at host guohao4.
 */
public class XAPMManagerTest {

    private static final String SCRIPT
            = "home();\n" +
            "back();\n" +
            "menu();\n" +
            "toast(\"Hello!!!\");\n" +
            "threadWait(3000);\n" +
            "gps(true);\n" +
            "wifi(false);\n" +
            "bt(false);\n" +
            "var top = getTopPackage();\n" +
            "toast(top);\n";

    @Test
    public void testJsStore() {
        XAPMManager xapmManager = XAPMManager.get();
        Assert.assertNotNull(xapmManager);

        String newId = UUID.randomUUID().toString();
        Assert.assertTrue(xapmManager.getSavedJs(newId) == null);

        // Insert.
        JavaScript js = new JavaScript();
        js.setAlias("NickName");
        js.setCreatedAt(System.currentTimeMillis());
        js.setId(newId);
        js.setScript(SCRIPT);
        xapmManager.saveJs(js);
        Assert.assertTrue(xapmManager.getSavedJs(newId) != null);

        List<JavaScript> javaScripts = xapmManager.getSavedJses();
        Assert.assertTrue(javaScripts != null && javaScripts.size() > 0);

        // execute!
        xapmManager.evaluateJsString(new String[]{js.getScript()});
    }
}
