package github.tornaco.xposedmoduletest.bean;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.util.Singleton;

import static github.tornaco.xposedmoduletest.bean.DaoMaster.dropAllTables;


/**
 * Created by guohao4 on 2017/10/18.
 * Email: Tornaco@163.com
 */

public class DaoManager {

    private static final String DB_NAME = "guard_db";

    private static final Singleton<DaoManager> sManager
            = new Singleton<DaoManager>() {
        @Override
        protected DaoManager create() {
            return new DaoManager();
        }
    };

    private DaoManager() {

    }

    private DaoSession session;

    public static DaoManager getInstance() {
        return sManager.get();
    }

    private void init(Context context) {
        try {
            MyOpenHelper openHelper = new MyOpenHelper(context, DB_NAME, null);
            DaoMaster daoMaster = new DaoMaster(openHelper.getWritableDatabase());
            session = daoMaster.newSession(IdentityScopeType.None);
        } catch (Throwable e) {
            Logger.e("Fail init session:" + Logger.getStackTraceString(e));
        }
    }

    public
    @Nullable
    synchronized DaoSession getSession(Context context) {
        if (session == null) init(context);
        return session;
    }

    /**
     * WARNING: Drops all table on Upgrade! Use only during development.
     */
    public static class MyOpenHelper extends DaoMaster.OpenHelper {

        MyOpenHelper(Context context, String name) {
            super(context, name);
        }

        MyOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            Logger.w("greenDAO Upgrading schema from version "
                    + oldVersion + " to "
                    + newVersion);
            if (oldVersion == 1001) {
            } else if (oldVersion == 1002) {
                ComponentReplacementDao.createTable(db, true);
            } else if (oldVersion == 1004) {
                RecentTileDao.createTable(db, true);
            } else {
                dropAllTables(db, true);
                onCreate(db);
            }
        }
    }
}
