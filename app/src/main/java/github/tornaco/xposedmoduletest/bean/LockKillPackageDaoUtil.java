package github.tornaco.xposedmoduletest.bean;

import android.database.Cursor;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */

public class LockKillPackageDaoUtil {

    /**
     * Must be sync with {@link LockKillPackageDao#readEntity(Cursor, int)}
     */
    public static LockKillPackage readEntity(Cursor cursor, int offset) {
        LockKillPackage entity = new LockKillPackage( //
                cursor.isNull(offset + 0) ? null : cursor.getInt(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // pkgName
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // appName
                cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0 // kill
        );
        return entity;
    }
}
