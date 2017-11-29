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

import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.bean.RFKillPackage;
import github.tornaco.xposedmoduletest.bean.RFKillPackageDao;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */

public class RFKillPackageProvider extends ContentProvider {

    public static final Uri CONTENT_URI =
            Uri.parse("content://github.tornaco.xposedmoduletest.rf_kill_package_provider/pkgs");

    private static final UriMatcher MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    private static final int PKGS = 1;

    static {
        MATCHER.addURI("github.tornaco.xposedmoduletest.rf_kill_package_provider", "pkgs", PKGS);
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
                RFKillPackageDao dao = daoSession.getRFKillPackageDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                int count = db.delete(RFKillPackageDao.TABLENAME, selection, selectionArgs);
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
                RFKillPackageDao dao = daoSession.getRFKillPackageDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                long rowid = db.insert(RFKillPackageDao.TABLENAME, null, values);
                Uri insertUri = ContentUris.withAppendedId(uri, rowid);
                ContentResolver resolver = resolverChecked();
                if (resolver != null) resolver.notifyChange(uri, null);
                return insertUri;
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    public static Uri insert(Context context, RFKillPackage RFKillPackage) {
        ContentValues values = new ContentValues();
        values.put(RFKillPackageDao.Properties.PkgName.columnName, RFKillPackage.getPkgName());
        values.put(RFKillPackageDao.Properties.Kill.columnName, RFKillPackage.getKill());
        values.put(RFKillPackageDao.Properties.AppName.columnName, RFKillPackage.getAppName());
        return context.getContentResolver().insert(CONTENT_URI, values);
    }

    public static int delete(Context context, RFKillPackage RFKillPackage) {
        return context.getContentResolver().delete(CONTENT_URI,
                RFKillPackageDao.Properties.PkgName.columnName + "=?",
                new String[]{RFKillPackage.getPkgName()});
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
                RFKillPackageDao dao = daoSession.getRFKillPackageDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                return db.query(RFKillPackageDao.TABLENAME, projection, selection, selectionArgs,
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
                RFKillPackageDao dao = daoSession.getRFKillPackageDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                count = db.update(RFKillPackageDao.TABLENAME, values, selection, selectionArgs);
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
