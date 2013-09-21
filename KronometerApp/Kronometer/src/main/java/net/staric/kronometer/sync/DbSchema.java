package net.staric.kronometer.sync;

import android.provider.BaseColumns;

/* package private */ interface DbSchema {

    String DB_NAME = "kronometer.db";

    String TBL_BIKERS = "bikers";

    String COL_ID = BaseColumns._ID;
    String COL_NAME = "name";
    String COL_SURNAME = "surname";
    String COL_START_TIME = "start_time";
    String COL_END_TIME = "end_time";

    String DDL_CREATE_TBL_BIKERS =
            "CREATE TABLE items (" +
                    "_id          INTEGER  PRIMARY KEY AUTOINCREMENT, \n" +
                    "name         TEXT,\n" +
                    "surname      TEXT \n" +
                    "start_time   TEXT \n" +
                    "end_time     TEXT \n" +
                    ")";


    String DDL_DROP_TBL_BIKERS =
            "DROP TABLE IF EXISTS bikers";

    String DML_WHERE_ID_CLAUSE = "_id = ?";

    String DEFAULT_TBL_ITEMS_SORT_ORDER = "_id ASC";

}
