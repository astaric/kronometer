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

public class KronometerContentProvider extends android.content.ContentProvider {
    private SQLiteOpenHelper helper;
    private final ThreadLocal<Boolean> mIsInBatchMode = new ThreadLocal<Boolean>();

    /*
         * Always return true, indicating that the
         * provider loaded correctly.
         */
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

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            if (!isInBatchMode()) {
                getContext().getContentResolver().notifyChange(itemUri, null);
            }
            return itemUri;
        } else {
            throw new SQLException("Problem while inserting into uri: " + uri);
        }
    }

    /*
     * delete() always returns "no rows affected" (0)
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
    /*
     * update() always returns "no rows affected" (0)
     */
    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {
        return 0;
    }

    private boolean isInBatchMode() {
        return mIsInBatchMode.get() != null && mIsInBatchMode.get();
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
