package net.staric.kronometer.sync;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class KronometerProvider extends android.content.ContentProvider {
    private SQLiteOpenHelper helper;

    @Override
    public boolean onCreate() {
        helper = new KronometerOpenHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case BIKER_LIST:
                return KronometerContract.Bikers.CONTENT_TYPE;
            case BIKER_ID:
                return KronometerContract.Bikers.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (URI_MATCHER.match(uri) != BIKER_LIST) {
            throw new IllegalArgumentException("Unsupported URI for insertion: " + uri);
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(DbSchema.TBL_BIKERS, null, values);
        return getUriForId(id, uri);
    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            getContext().getContentResolver().notifyChange(itemUri, null);
            return itemUri;
        } else {
            throw new SQLException("Problem while inserting into uri: " + uri);
        }
    }

    @Override
    public Cursor query(
            Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        SQLiteDatabase db = helper.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (URI_MATCHER.match(uri)) {
            case BIKER_LIST:
                builder.setTables(DbSchema.TBL_BIKERS);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = KronometerContract.Bikers.SORT_ORDER_DEFAULT;
                }
                break;
            case BIKER_ID:
                builder.setTables(DbSchema.TBL_BIKERS);
                builder.appendWhere(KronometerContract.Bikers._ID + " = " +
                        uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);
        }
        Cursor cursor =
                builder.query(
                        db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

        // if we want to be notified of any changes:
        cursor.setNotificationUri(
                getContext().getContentResolver(),
                uri);
        return cursor;
    }

    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

            SQLiteDatabase db = helper.getWritableDatabase();
            int updateCount = 0;
            switch (URI_MATCHER.match(uri)) {
                case BIKER_LIST:
                    updateCount = db.update(
                            DbSchema.TBL_BIKERS,
                            values,
                            selection,
                            selectionArgs);
                    break;
                case BIKER_ID:
                    String idStr = uri.getLastPathSegment();
                    String where = KronometerContract.Bikers._ID + " = " + idStr;
                    if (!TextUtils.isEmpty(selection)) {
                        where += " AND " + selection;
                    }
                    updateCount = db.update(
                            DbSchema.TBL_BIKERS,
                            values,
                            where,
                            selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
            }

            if (updateCount > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return updateCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
            SQLiteDatabase db = helper.getWritableDatabase();
            int delCount = 0;
            switch (URI_MATCHER.match(uri)) {
                case BIKER_LIST:
                    delCount = db.delete(
                            DbSchema.TBL_BIKERS,
                            selection,
                            selectionArgs);
                    break;
                case BIKER_ID:
                    String idStr = uri.getLastPathSegment();
                    String where = KronometerContract.Bikers._ID + " = " + idStr;
                    if (!TextUtils.isEmpty(selection)) {
                        where += " AND " + selection;
                    }
                    delCount = db.delete(
                            DbSchema.TBL_BIKERS,
                            where,
                            selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
            }

            if (delCount > 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return delCount;
    }

    // helper constants for use with the UriMatcher
    private static final int BIKER_LIST = 1;
    private static final int BIKER_ID = 2;
    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(KronometerContract.AUTHORITY,
                "bikers",
                BIKER_LIST);
        URI_MATCHER.addURI(KronometerContract.AUTHORITY,
                "bikers/#",
                BIKER_ID);
    }
}
