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

import github.tornaco.xposedmoduletest.bean.BlockRecord;
import github.tornaco.xposedmoduletest.bean.BlockRecordDao;
import github.tornaco.xposedmoduletest.bean.DaoManager;
import github.tornaco.xposedmoduletest.bean.DaoSession;
import github.tornaco.xposedmoduletest.xposed.util.Closer;
import lombok.Getter;

/**
 * Created by guohao4 on 2017/11/4.
 * Email: Tornaco@163.com
 */
@Deprecated
public class BlockRecordProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://github.tornaco.xposedmoduletest.block_record_provider/pkgs");

    private static final UriMatcher MATCHER = new UriMatcher(
            UriMatcher.NO_MATCH);

    private static final int PKGS = 1;

    static {
        MATCHER.addURI("github.tornaco.xposedmoduletest.block_record_provider", "pkgs", PKGS);
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
                BlockRecordDao dao = daoSession.getBlockRecordDao();
                // Magic number:)
                if ("*".equals(selection)) {
                    dao.deleteAll();
                    return 0;
                }
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                int count = db.delete(BlockRecordDao.TABLENAME, selection, selectionArgs);
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
                BlockRecordDao dao = daoSession.getBlockRecordDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                Cursor cursor = db.query(BlockRecordDao.TABLENAME, null, null, null, null, null, null);
                int count = cursor == null ? 0 : cursor.getCount();
                if (count >= 1000) {
                    dao.deleteAll();
                }
                Closer.closeQuietly(cursor);
                long rowid = db.insert(BlockRecordDao.TABLENAME, null, values);
                Uri insertUri = ContentUris.withAppendedId(uri, rowid);
                ContentResolver resolver = resolverChecked();
                if (resolver != null) resolver.notifyChange(uri, null);
                return insertUri;
            default:
                throw new IllegalArgumentException("Unknown Uri:" + uri.toString());
        }
    }

    /**
     * @deprecated Do not use provider for record saving fot better battery usage.
     */
    @Deprecated
    public static Uri insert(Context context, BlockRecord blockRecord) {
        ContentValues values = new ContentValues();
        values.put(BlockRecordDao.Properties.PkgName.columnName, blockRecord.getPkgName());
        values.put(BlockRecordDao.Properties.AppName.columnName, blockRecord.getAppName());
        values.put(BlockRecordDao.Properties.TimeWhen.columnName, blockRecord.getTimeWhen());
        values.put(BlockRecordDao.Properties.Allow.columnName, blockRecord.getAllow());
        values.put(BlockRecordDao.Properties.Description.columnName, blockRecord.getDescription());
        values.put(BlockRecordDao.Properties.Why.columnName, blockRecord.getWhy());
        return context.getContentResolver().insert(CONTENT_URI, values);
    }

    public static int delete(Context context, BlockRecord blockRecord) {
        return context.getContentResolver().delete(CONTENT_URI,
                BlockRecordDao.Properties.PkgName.columnName + "=?",
                new String[]{blockRecord.getPkgName()});
    }

    @Override
    public boolean onCreate() {
        daoSession = DaoManager.getInstance().getSession(getContext());
        Logger.d("BlockRecordProvider, onCreate:" + daoSession);
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
                BlockRecordDao dao = daoSession.getBlockRecordDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                return db.query(BlockRecordDao.TABLENAME, projection, selection, selectionArgs,
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
                BlockRecordDao dao = daoSession.getBlockRecordDao();
                SQLiteDatabase db = (SQLiteDatabase) dao.getDatabase().getRawDatabase();
                count = db.update(BlockRecordDao.TABLENAME, values, selection, selectionArgs);
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
