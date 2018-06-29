package github.tornaco.xposedmoduletest.github.tornaco.xposedmoduletest.xposed.app;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.UUID;

import github.tornaco.xposedmoduletest.compat.os.XAppOpsManager;
import github.tornaco.xposedmoduletest.util.StopWatch;
import github.tornaco.xposedmoduletest.xposed.app.XAPMManager;
import github.tornaco.xposedmoduletest.xposed.bean.AppOpsTemplate;
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

    private static final String SCRIPT_TOAST_PKG = "var top = getTopPackage(); toast(top);";

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

        StopWatch stopWatch = StopWatch.start("");
        for (int i = 0; i < 1000; i++) {
            js.setId(UUID.randomUUID().toString());
            js.setAlias("JS-" + i);
            xapmManager.saveJs(js);
        }
        stopWatch.split("Save JS");

        javaScripts = xapmManager.getSavedJses();
        stopWatch.split("Get JS");

        stopWatch.stop();
    }

    @Test
    public void testAppFocus() {
        String newId = UUID.randomUUID().toString();
        JavaScript js = new JavaScript();
        js.setAlias("NickName");
        js.setCreatedAt(System.currentTimeMillis());
        js.setId(newId);
        js.setScript(SCRIPT_TOAST_PKG);
        XAPMManager.get().saveJs(js);
        Assert.assertTrue(XAPMManager.get().getSavedJs(newId) != null);
    }

    @Test
    public void testAppOpsTemplate() {
        AppOpsTemplate template = new AppOpsTemplate();
        template.setMode(XAppOpsManager.OP_ACCESS_NOTIFICATIONS, XAppOpsManager.MODE_IGNORED);
        Assert.assertTrue(template.getMode(XAppOpsManager.OP_ACCESS_NOTIFICATIONS) == XAppOpsManager.MODE_IGNORED);

        template.setMode(XAppOpsManager.OP_ACCESS_NOTIFICATIONS, XAppOpsManager.MODE_ALLOWED);
        Assert.assertTrue(template.getMode(XAppOpsManager.OP_ACCESS_NOTIFICATIONS) == XAppOpsManager.MODE_ALLOWED);

        XAPMManager.get().addAppOpsTemplate(template);
        List<AppOpsTemplate> appOpsTemplates = XAPMManager.get().getAppOpsTemplates();
        Assert.assertNotNull(appOpsTemplates);
        Assert.assertTrue(appOpsTemplates.contains(template));
    }
}
