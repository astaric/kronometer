package net.staric.kronometer;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class KronometerContract {

    // Custom Intents
    public static final String SENSOR_EVENT_ACTION = "net.staric.kronometer.sensor_event";
    public static final String SENSOR_STATUS_CHANGED_ACTION = "net.staric.kronometer.sensor_status_changed";
    public static final String SENSOR_STATUS = "sensor_status";

    // Content Provider
    public static final String AUTHORITY = "net.staric.kronometer";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final class Bikers implements BaseColumns {
        public static final String NAME = "name";
        public static final String SURNAME = "surname";
        public static final String START_TIME = "start_time";
        public static final String ON_FINISH = "on_finish";
        public static final String END_TIME = "end_time";
        public static final String UPLOADED = "uploaded";

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
                {_ID, NAME, SURNAME, START_TIME, ON_FINISH, END_TIME};
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
