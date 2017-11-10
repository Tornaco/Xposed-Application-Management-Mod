package github.tornaco.xposedmoduletest.bean;

import android.database.Cursor;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */

public class BootCompletePackageDaoUtil {

    /**
     * Must be sync with {@link BootCompletePackageDao#readEntity(Cursor, int)}
     */
    public static BootCompletePackage readEntity(Cursor cursor, int offset) {
        BootCompletePackage entity = new BootCompletePackage( //
                cursor.isNull(offset + 0) ? null : cursor.getInt(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // pkgName
                cursor.isNull(offset + 2) ? null : cursor.getShort(offset + 2) != 0 // allow
        );
        return entity;
    }
}
