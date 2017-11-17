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

import github.tornaco.xposedmoduletest.bean.ComponentSettings;
import github.tornaco.xposedmoduletest.bean.ComponentSettingsDao;
import github.tornaco.xposedmoduletest.bean.ComponentSettingsDaoUtil;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */

public class ComponentsProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://github.tornaco.xposedmoduletest.components_provider/pkgs");

    private static final UriMatcher MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    private static final int PKGS = 1;

    static {
        MATCHER.addURI("github.tornaco.xposedmoduletest.components_provider", "pkgs", PKGS);
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
                ComponentSettingsDao dao = daoSession.getComponentSettingsDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                int count = db.delete(ComponentSettingsDao.TABLENAME, selection, selectionArgs);
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
                ComponentSettingsDao dao = daoSession.getComponentSettingsDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                long rowid = db.insert(ComponentSettingsDao.TABLENAME, null, values);
                Uri insertUri = ContentUris.withAppendedId(uri, rowid);
                ContentResolver resolver = resolverChecked();
                if (resolver != null) resolver.notifyChange(uri, null);
                return insertUri;
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    public static Uri insertOrUpdate(Context context, ComponentSettings componentSettings) {
        // Query exists.
        ComponentSettings ex = deleteIfExists(context, componentSettings);
        if (ex != null) {
            Logger.d("Found exists componentSettings: " + ex);
        }

        // We will not insert into db, since not-set and allow is the same.
        if (componentSettings.getAllow()) return null;

        ContentValues values = new ContentValues();
        values.put(ComponentSettingsDao.Properties.PackageName.columnName, componentSettings.getPackageName());
        values.put(ComponentSettingsDao.Properties.ClassName.columnName, componentSettings.getClassName());
        values.put(ComponentSettingsDao.Properties.Allow.columnName, componentSettings.getAllow());
        return context.getContentResolver().insert(CONTENT_URI, values);
    }

    public static int delete(Context context, ComponentSettings componentSettings) {
        return context.getContentResolver().delete(CONTENT_URI,
                ComponentSettingsDao.Properties.PackageName.columnName + "=? and "
                        + ComponentSettingsDao.Properties.ClassName.columnName + "=?",
                new String[]{componentSettings.getPackageName(), componentSettings.getClassName()});
    }

    public static ComponentSettings deleteIfExists(Context context, ComponentSettings componentSettings) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(CONTENT_URI,
                    null,
                    ComponentSettingsDao.Properties.PackageName.columnName + "=? and "
                            + ComponentSettingsDao.Properties.ClassName.columnName + "=?",
                    new String[]{componentSettings.getPackageName(), componentSettings.getClassName()},
                    null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                int cnt = c.getCount();
                if (cnt > 1) {
                    Logger.e("Found more than 1 ComponentSettings records for: " + componentSettings);
                }
                return ComponentSettingsDaoUtil.readEntity(c, 0);
            }
            return null;
        } finally {
            Closer.closeQuietly(c);
            delete(context, componentSettings);
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
                ComponentSettingsDao dao = daoSession.getComponentSettingsDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                return db.query(ComponentSettingsDao.TABLENAME, projection, selection, selectionArgs,
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
                ComponentSettingsDao dao = daoSession.getComponentSettingsDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                count = db.update(ComponentSettingsDao.TABLENAME, values, selection, selectionArgs);
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
