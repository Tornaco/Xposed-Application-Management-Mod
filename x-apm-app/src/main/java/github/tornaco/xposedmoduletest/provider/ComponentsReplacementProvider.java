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

import github.tornaco.xposedmoduletest.bean.ComponentReplacement;
import github.tornaco.xposedmoduletest.bean.ComponentReplacementDao;
import github.tornaco.xposedmoduletest.bean.ComponentReplacementDaoUtil;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */
@Deprecated
public class ComponentsReplacementProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://github.tornaco.xposedmoduletest.components_replacement_provider/pkgs");

    private static final UriMatcher MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    private static final int PKGS = 1;

    static {
        MATCHER.addURI("github.tornaco.xposedmoduletest.components_replacement_provider", "pkgs", PKGS);
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
                ComponentReplacementDao dao = daoSession.getComponentReplacementDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                int count = db.delete(ComponentReplacementDao.TABLENAME, selection, selectionArgs);
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
                ComponentReplacementDao dao = daoSession.getComponentReplacementDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                long rowid = db.insert(ComponentReplacementDao.TABLENAME, null, values);
                Uri insertUri = ContentUris.withAppendedId(uri, rowid);
                ContentResolver resolver = resolverChecked();
                if (resolver != null) resolver.notifyChange(uri, null);
                return insertUri;
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    public static Uri insertOrUpdate(Context context, ComponentReplacement componentSettings) {
        // Query exists.
        ComponentReplacement ex = deleteIfExists(context, componentSettings);
        if (ex != null) {
            Logger.d("Found exists componentSettings: " + ex);
        }

        ContentValues values = new ContentValues();
        values.put(ComponentReplacementDao.Properties.CompFromPackageName.columnName, componentSettings.getCompFromPackageName());
        values.put(ComponentReplacementDao.Properties.CompFromClassName.columnName, componentSettings.getCompFromClassName());
        values.put(ComponentReplacementDao.Properties.CompToClassName.columnName, componentSettings.getCompToClassName());
        values.put(ComponentReplacementDao.Properties.CompToPackageName.columnName, componentSettings.getCompToPackageName());
        return context.getContentResolver().insert(CONTENT_URI, values);
    }

    public static int delete(Context context, ComponentReplacement componentSettings) {
        Logger.w("DELETE@ " + componentSettings);
        return context.getContentResolver().delete(CONTENT_URI,
                ComponentReplacementDao.Properties.CompFromClassName.columnName + "=? and "
                        + ComponentReplacementDao.Properties.CompFromPackageName.columnName + "=?",
                new String[]{componentSettings.getCompFromClassName(),
                        componentSettings.getCompFromPackageName()});
    }

    public static ComponentReplacement deleteIfExists(Context context, ComponentReplacement componentSettings) {
        Cursor c = null;
        try {
            c = context.getContentResolver().query(CONTENT_URI,
                    null,
                    ComponentReplacementDao.Properties.CompFromPackageName.columnName + "=? and "
                            + ComponentReplacementDao.Properties.CompFromClassName.columnName + "=?",
                    new String[]{componentSettings.getCompFromPackageName(),
                            componentSettings.getCompFromClassName()},
                    null);
            if (c != null && c.getCount() > 0) {
                c.moveToFirst();
                int cnt = c.getCount();
                if (cnt > 1) {
                    Logger.e("Found more than 1 ComponentReplacement records for: " + componentSettings);
                }
                return ComponentReplacementDaoUtil.readEntity(c, 0);
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
                ComponentReplacementDao dao = daoSession.getComponentReplacementDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                return db.query(ComponentReplacementDao.TABLENAME, projection, selection, selectionArgs,
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
                ComponentReplacementDao dao = daoSession.getComponentReplacementDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                count = db.update(ComponentReplacementDao.TABLENAME, values, selection, selectionArgs);
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
