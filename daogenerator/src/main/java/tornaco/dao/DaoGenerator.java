package tornaco.dao;

import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DaoGenerator {

    private static final int VERSION = 1;

    public static void main(String[] args) throws Exception {
        Schema sch = new Schema(VERSION, "github.tornaco.xposedmoduletest.bean");
        sch.setDefaultJavaPackageDao("github.tornaco.xposedmoduletest.bean");
        sch.enableKeepSectionsByDefault();
        createPackageInfo(sch);
        new de.greenrobot.daogenerator.DaoGenerator().generateAll(sch, "./app/src/main/java");
    }

    private static void createPackageInfo(Schema sch) {
        Entity pkgInfo = sch.addEntity("PackageInfo");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("pkgName");
        pkgInfo.addStringProperty("appName");
        pkgInfo.addLongProperty("addAt");
        pkgInfo.addBooleanProperty("guard");
        pkgInfo.addByteProperty("flags");
    }

    private static void createAccessInfo(Schema sch) {
        Entity accessInfo = sch.addEntity("AccessInfo");
        accessInfo.addLongProperty("when");
    }
}
