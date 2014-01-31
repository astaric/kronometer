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
        Uri itemUri = getUriForId(id, uri);

        notifyUriChanged(itemUri);
        return itemUri;
    }

    private Uri getUriForId(long id, Uri uri) {
        if (id > 0) {
            return ContentUris.withAppendedId(uri, id);
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
        builder.setTables(DbSchema.TBL_BIKERS);
        switch (URI_MATCHER.match(uri)) {
            case BIKER_ID:
                selection = addBikerIdToSelection(uri, selection);
                break;
            case BIKER_LIST:
                if (TextUtils.isEmpty(sortOrder))
                    sortOrder = KronometerContract.Bikers.SORT_ORDER_DEFAULT;
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported URI: " + uri);
        }
        Cursor cursor = builder.query(
                db,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        subscribeToNotifications(cursor, uri);
        return cursor;
    }

    private void subscribeToNotifications(Cursor cursor, Uri uri) {
        cursor.setNotificationUri(
                getContext().getContentResolver(),
                uri);
    }

    public int update(
            Uri uri,
            ContentValues values,
            String selection,
            String[] selectionArgs) {

            SQLiteDatabase db = helper.getWritableDatabase();
            int updateCount = 0;
            switch (URI_MATCHER.match(uri)) {
                case BIKER_ID:
                    selection = addBikerIdToSelection(uri, selection);
                case BIKER_LIST:
                    updateCount = db.update(
                            DbSchema.TBL_BIKERS,
                            values,
                            selection,
                            selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
            }

            if (updateCount > 0) {
                notifyUriChanged(uri);
            }
            return updateCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
            SQLiteDatabase db = helper.getWritableDatabase();
            int delCount = 0;
            switch (URI_MATCHER.match(uri)) {
                case BIKER_ID:
                    selection = addBikerIdToSelection(uri, selection);
                case BIKER_LIST:
                    delCount = db.delete(
                            DbSchema.TBL_BIKERS,
                            selection,
                            selectionArgs);
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported URI: " + uri);
            }

            if (delCount > 0) {
                notifyUriChanged(uri);
            }
            return delCount;
    }

    private void notifyUriChanged(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }

    private String addBikerIdToSelection(Uri uri, String selection) {
        String idStr = uri.getLastPathSegment();
        String where = KronometerContract.Bikers._ID + " = " + idStr;
        if (!TextUtils.isEmpty(selection)) {
            where += " AND " + selection;
        }
        return where;
    }
}
