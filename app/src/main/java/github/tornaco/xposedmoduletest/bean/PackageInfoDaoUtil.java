package github.tornaco.xposedmoduletest.bean;

import android.database.Cursor;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */

public class PackageInfoDaoUtil {

    /**
     * Must be sync with {@link PackageInfoDao#readEntity(Cursor, int)}
     */
    public static PackageInfo readEntity(Cursor cursor, int offset) {
        return new PackageInfo( //
                cursor.isNull(offset + 0) ? null : cursor.getInt(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // pkgName
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // appName
                cursor.isNull(offset + 3) ? null : cursor.getLong(offset + 3), // addAt
                cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // versionCode
                cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // ext
                cursor.isNull(offset + 6) ? null : cursor.getShort(offset + 6) != 0, // guard
                cursor.isNull(offset + 7) ? null : (byte) cursor.getShort(offset + 7) // flags
        );
    }
}
