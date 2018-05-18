package github.tornaco.xposedmoduletest.bean;

import android.database.Cursor;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */

public class ComponentReplacementDaoUtil {

    /**
     * Must be sync with {@link ComponentReplacementDao#readEntity(Cursor, int)}
     */
    public static ComponentReplacement readEntity(Cursor cursor, int offset) {
        ComponentReplacement entity = new ComponentReplacement( //
                cursor.isNull(offset + 0) ? null : cursor.getInt(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // appPackageName
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // compFromPackageName
                cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // compFromClassName
                cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // compToPackageName
                cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5) // compToClassName
        );
        return entity;
    }
}
