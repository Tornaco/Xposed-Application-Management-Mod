package tornaco.dao;


import org.greenrobot.greendao.generator.Entity;
import org.greenrobot.greendao.generator.Schema;

public class DaoGenerator {

    private static final int VERSION = 2;

    public static void main(String[] args) throws Exception {
        Schema sch = new Schema(VERSION, "github.tornaco.xposedmoduletest.bean");
        sch.setDefaultJavaPackageDao("github.tornaco.xposedmoduletest.bean");

        sch.enableKeepSectionsByDefault();

        createPackageInfo(sch);
        createAccessInfo(sch);
        createBootCompletePackage(sch);
        createAutoStartPackage(sch);
        createLockClearPackage(sch);
        createRFClearPackage(sch);
        createBlockRecord(sch);
        createComponentSettings(sch);

        createComponentReplacement(sch);
        createCongfigurationHook(sch);

        createRecentTile(sch);

        new org.greenrobot.greendao.generator.DaoGenerator().generateAll(sch, "../app/src/main/java");
    }

    private static void createPackageInfo(Schema sch) {
        Entity pkgInfo = sch.addEntity("PackageInfo");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("pkgName");
        pkgInfo.addStringProperty("appName");
        pkgInfo.addLongProperty("addAt");
        pkgInfo.addIntProperty("versionCode");
        pkgInfo.addStringProperty("ext");
        pkgInfo.addBooleanProperty("guard");
        pkgInfo.addByteProperty("flags");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createAccessInfo(Schema sch) {
        Entity accessInfo = sch.addEntity("AccessInfo");
        accessInfo.addIntProperty("id").primaryKey();
        accessInfo.addLongProperty("when");
        accessInfo.addStringProperty("url");
        accessInfo.addStringProperty("appName");
        accessInfo.addStringProperty("pkgName");
        accessInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        accessInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        accessInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createBootCompletePackage(Schema sch) {
        Entity pkgInfo = sch.addEntity("BootCompletePackage");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("pkgName");
        pkgInfo.addStringProperty("appName");
        pkgInfo.addBooleanProperty("allow");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createAutoStartPackage(Schema sch) {
        Entity pkgInfo = sch.addEntity("AutoStartPackage");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("pkgName");
        pkgInfo.addStringProperty("appName");
        pkgInfo.addBooleanProperty("allow");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createLockClearPackage(Schema sch) {
        Entity pkgInfo = sch.addEntity("LockKillPackage");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("pkgName");
        pkgInfo.addStringProperty("appName");
        pkgInfo.addBooleanProperty("kill");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createRFClearPackage(Schema sch) {
        Entity pkgInfo = sch.addEntity("RFKillPackage");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("pkgName");
        pkgInfo.addStringProperty("appName");
        pkgInfo.addBooleanProperty("kill");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createBlockRecord(Schema sch) {
        Entity pkgInfo = sch.addEntity("BlockRecord");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("pkgName");
        pkgInfo.addStringProperty("appName");
        pkgInfo.addLongProperty("timeWhen");
        pkgInfo.addLongProperty("howManyTimes");
        pkgInfo.addBooleanProperty("allow");
        pkgInfo.addStringProperty("description");
        pkgInfo.addStringProperty("why");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createComponentSettings(Schema sch) {
        Entity pkgInfo = sch.addEntity("ComponentSettings");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("packageName");
        pkgInfo.addStringProperty("className");
        pkgInfo.addBooleanProperty("allow");
//        import org.greenrobot.greendao.annotation.Entity;
//        import org.greenrobot.greendao.annotation.Generated;
//        import org.greenrobot.greendao.annotation.Id;
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createComponentReplacement(Schema sch) {
        Entity pkgInfo = sch.addEntity("ComponentReplacement");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("appPackageName");
        pkgInfo.addStringProperty("compFromPackageName");
        pkgInfo.addStringProperty("compFromClassName");
        pkgInfo.addStringProperty("compToPackageName");
        pkgInfo.addStringProperty("compToClassName");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createCongfigurationHook(Schema sch) {
        Entity pkgInfo = sch.addEntity("CongfigurationSetting");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("packageName");
        pkgInfo.addIntProperty("densityDpi");
        pkgInfo.addFloatProperty("fontScale");
        pkgInfo.addIntProperty("orientation");
        pkgInfo.addIntProperty("screenHeightDp");
        pkgInfo.addIntProperty("screenWidthDp");
        pkgInfo.addBooleanProperty("excludeFromRecent");
        pkgInfo.addBooleanProperty("uiMode");

        // Some extra fields, in-case we add later.
        for (int i = 0; i < 5; i++) {
            pkgInfo.addIntProperty("intArg" + i);
            pkgInfo.addStringProperty("stringArg" + i);
            pkgInfo.addFloatProperty("floatArg" + i);
            pkgInfo.addLongProperty("longArg" + i);
            pkgInfo.addBooleanProperty("boolArg" + i);
        }

        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }

    private static void createRecentTile(Schema sch) {
        Entity pkgInfo = sch.addEntity("RecentTile");
        pkgInfo.addIntProperty("id").primaryKey();
        pkgInfo.addStringProperty("tileKey");
        pkgInfo.addLongProperty("lastUsed");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Entity");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Generated");
        pkgInfo.addImport("org.greenrobot.greendao.annotation.Id");
    }
}

