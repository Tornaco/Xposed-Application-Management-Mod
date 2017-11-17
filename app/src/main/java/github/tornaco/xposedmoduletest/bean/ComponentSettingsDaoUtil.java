package github.tornaco.xposedmoduletest.bean;

import android.database.Cursor;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */

public class ComponentSettingsDaoUtil {

    /**
     * Must be sync with {@link ComponentSettingsDao#readEntity(Cursor, int)}
     */
    public static ComponentSettings readEntity(Cursor cursor, int offset) {
        ComponentSettings entity = new ComponentSettings( //
                cursor.isNull(offset + 0) ? null : cursor.getInt(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // packageName
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // className
                cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0 // allow
        );
        return entity;
    }
}
