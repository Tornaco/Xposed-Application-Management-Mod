package github.tornaco.xposedmoduletest.bean;

import android.database.Cursor;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */

public class CongfigurationSettingDaoUtil {

    /**
     * Must be sync with {@link CongfigurationSettingDao#readEntity(Cursor, int)}
     */
    public static CongfigurationSetting readEntity(Cursor cursor, int offset) {
        CongfigurationSetting entity = new CongfigurationSetting( //
                cursor.isNull(offset + 0) ? null : cursor.getInt(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // packageName
                cursor.isNull(offset + 2) ? null : cursor.getInt(offset + 2), // densityDpi
                cursor.isNull(offset + 3) ? null : cursor.getFloat(offset + 3), // fontScale
                cursor.isNull(offset + 4) ? null : cursor.getInt(offset + 4), // orientation
                cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // screenHeightDp
                cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6), // screenWidthDp
                cursor.isNull(offset + 7) ? null : cursor.getShort(offset + 7) != 0, // excludeFromRecent
                cursor.isNull(offset + 8) ? null : cursor.getShort(offset + 8) != 0, // uiMode
                cursor.isNull(offset + 9) ? null : cursor.getInt(offset + 9), // intArg0
                cursor.isNull(offset + 10) ? null : cursor.getString(offset + 10), // stringArg0
                cursor.isNull(offset + 11) ? null : cursor.getFloat(offset + 11), // floatArg0
                cursor.isNull(offset + 12) ? null : cursor.getLong(offset + 12), // longArg0
                cursor.isNull(offset + 13) ? null : cursor.getShort(offset + 13) != 0, // boolArg0
                cursor.isNull(offset + 14) ? null : cursor.getInt(offset + 14), // intArg1
                cursor.isNull(offset + 15) ? null : cursor.getString(offset + 15), // stringArg1
                cursor.isNull(offset + 16) ? null : cursor.getFloat(offset + 16), // floatArg1
                cursor.isNull(offset + 17) ? null : cursor.getLong(offset + 17), // longArg1
                cursor.isNull(offset + 18) ? null : cursor.getShort(offset + 18) != 0, // boolArg1
                cursor.isNull(offset + 19) ? null : cursor.getInt(offset + 19), // intArg2
                cursor.isNull(offset + 20) ? null : cursor.getString(offset + 20), // stringArg2
                cursor.isNull(offset + 21) ? null : cursor.getFloat(offset + 21), // floatArg2
                cursor.isNull(offset + 22) ? null : cursor.getLong(offset + 22), // longArg2
                cursor.isNull(offset + 23) ? null : cursor.getShort(offset + 23) != 0, // boolArg2
                cursor.isNull(offset + 24) ? null : cursor.getInt(offset + 24), // intArg3
                cursor.isNull(offset + 25) ? null : cursor.getString(offset + 25), // stringArg3
                cursor.isNull(offset + 26) ? null : cursor.getFloat(offset + 26), // floatArg3
                cursor.isNull(offset + 27) ? null : cursor.getLong(offset + 27), // longArg3
                cursor.isNull(offset + 28) ? null : cursor.getShort(offset + 28) != 0, // boolArg3
                cursor.isNull(offset + 29) ? null : cursor.getInt(offset + 29), // intArg4
                cursor.isNull(offset + 30) ? null : cursor.getString(offset + 30), // stringArg4
                cursor.isNull(offset + 31) ? null : cursor.getFloat(offset + 31), // floatArg4
                cursor.isNull(offset + 32) ? null : cursor.getLong(offset + 32), // longArg4
                cursor.isNull(offset + 33) ? null : cursor.getShort(offset + 33) != 0 // boolArg4
        );
        return entity;
    }
}
