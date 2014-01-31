package net.staric.kronometer;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class KronometerContract {

    public static final String AUTHORITY = "net.staric.kronometer";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Bikers implements BaseColumns {
        public static final String NAME = "name";
        public static final String SURNAME = "surname";
        public static final String START_TIME = "start_time";
        public static final String END_TIME = "end_time";

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(KronometerContract.CONTENT_URI, "bikers");

        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.net.staric.kronometer_bikers";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.net.staric.kronometer_bikers";

        /**
         * A projection of all columns
         * in the items table.
         */
        public static final String[] PROJECTION_ALL =
                {_ID, NAME, SURNAME, START_TIME, END_TIME};
        /**
         * The default sort order for
         * queries containing _ID fields.
         */
        public static final String SORT_ORDER_DEFAULT =
                _ID + " ASC";
    }

    public static final class SensorEvent implements BaseColumns {
        public static final String TIMESTAMP = "timestamp";

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(KronometerContract.CONTENT_URI, "events");

        /**
         * The mime type of a directory of items.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.net.staric.kronometer_events";
        /**
         * The mime type of a single item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.net.staric.kronometer_events";

        /**
         * A projection of all columns
         * in the items table.
         */
        public static final String[] PROJECTION_ALL =
                {_ID, TIMESTAMP};
        /**
         * The default sort order for
         * queries containing timestamp field.
         */
        public static final String SORT_ORDER_DEFAULT =
                TIMESTAMP + " DESC";
    }
}
