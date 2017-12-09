package github.tornaco.xposedmoduletest.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.newstand.logger.Logger;

import github.tornaco.xposedmoduletest.bean.CongfigurationSetting;
import github.tornaco.xposedmoduletest.bean.CongfigurationSettingDao;
import github.tornaco.xposedmoduletest.bean.CongfigurationSettingDaoUtil;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */

public class ConfigurationSettingProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://github.tornaco.xposedmoduletest.config_setting_provider/pkgs");

    private static final UriMatcher MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    private static final int PKGS = 1;

    static {
        MATCHER.addURI("github.tornaco.xposedmoduletest.config_setting_provider", "pkgs", PKGS);
    }

    @Getter
    private DaoSession daoSession;

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (daoSession == null) {
            Logger.e("Dao session is null!!!");
            return 0;
        }
        switch (MATCHER.match(uri)) {
            case PKGS:
                CongfigurationSettingDao dao = daoSession.getCongfigurationSettingDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                int count = db.delete(CongfigurationSettingDao.TABLENAME, selection, selectionArgs);
                ContentResolver resolver = resolverChecked();
                if (resolver != null) resolver.notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (MATCHER.match(uri)) {
            case PKGS:
                return "vnd.android.cursor.dir/pkg";
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        if (daoSession == null) {
            Logger.e("Dao session is null!!!");
            return null;
        }
        switch (MATCHER.match(uri)) {
            case PKGS:
                CongfigurationSettingDao dao = daoSession.getCongfigurationSettingDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                long rowid = db.insert(CongfigurationSettingDao.TABLENAME, null, values);
                Uri insertUri = ContentUris.withAppendedId(uri, rowid);
                ContentResolver resolver = resolverChecked();
                if (resolver != null) resolver.notifyChange(uri, null);
                return insertUri;
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    public static Uri insertOrUpdate(Context context, CongfigurationSetting CongfigurationSetting) {
        // Query exists.
        CongfigurationSetting ex = deleteIfExists(context, CongfigurationSetting);
        if (ex != null) {
            Logger.d("Found exists CongfigurationSetting: " + ex);
        }

        ContentValues values = new ContentValues();
        values.put(CongfigurationSettingDao.Properties.PackageName.columnName, CongfigurationSetting.getPackageName());
        values.put(CongfigurationSettingDao.Properties.DensityDpi.columnName, CongfigurationSetting.getDensityDpi());
        values.put(CongfigurationSettingDao.Properties.FontScale.columnName, CongfigurationSetting.getFontScale());
        values.put(CongfigurationSettingDao.Properties.ExcludeFromRecent.columnName, CongfigurationSetting.getExcludeFromRecent());

        return context.getContentResolver().insert(CONTENT_URI, values);
    }

    public static int delete(Context context, CongfigurationSetting CongfigurationSetting) {
        return context.getContentResolver().delete(CONTENT_URI,
                CongfigurationSettingDao.Properties.PackageName.columnName + "=?",
                new String[]{CongfigurationSetting.getPackageName()});
    }

    public static CongfigurationSetting deleteIfExists(Context context, CongfigurationSetting CongfigurationSetting) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(CONTENT_URI,
                    null,
                    CongfigurationSettingDao.Properties.PackageName.columnName + "=?",
                    new String[]{CongfigurationSetting.getPackageName()},
                    null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                int cnt = c.getCount();
                if (cnt > 1) {
                    Logger.e("Found more than 1 CongfigurationSetting records for: " + CongfigurationSetting);
                }
                return CongfigurationSettingDaoUtil.readEntity(c, 0);
            }
            return null;
        } finally {
            Closer.closeQuietly(c);
            delete(context, CongfigurationSetting);
        }
    }

    @Override
    public boolean onCreate() {
        daoSession = DaoManager.getInstance().getSession(getContext());
        Logger.d("BootPackageProvider, onCreate:" + daoSession);
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        if (daoSession == null) {
            Logger.e("Dao session is null!!!");
            return null;
        }
        switch (MATCHER.match(uri)) {
            case PKGS:
                CongfigurationSettingDao dao = daoSession.getCongfigurationSettingDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                return db.query(CongfigurationSettingDao.TABLENAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (daoSession == null) {
            Logger.e("Dao session is null!!!");
            return 0;
        }
        switch (MATCHER.match(uri)) {
            case PKGS:
                int count;
                CongfigurationSettingDao dao = daoSession.getCongfigurationSettingDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                count = db.update(CongfigurationSettingDao.TABLENAME, values, selection, selectionArgs);
                ContentResolver resolver = resolverChecked();
                if (resolver != null) resolver.notifyChange(uri, null);
                return count;
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    private ContentResolver resolverChecked() {
        Context c = getContext();
        if (c != null) {
            return c.getContentResolver();
        }
        return null;
    }
}
