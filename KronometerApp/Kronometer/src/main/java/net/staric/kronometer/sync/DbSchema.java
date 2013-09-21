package net.staric.kronometer.sync;

import android.provider.BaseColumns;

/* package private */ interface DbSchema {

    String DB_NAME = "kronometer.db";

    String TBL_BIKERS = "bikers";

    String COL_ID = BaseColumns._ID;
    String COL_NAME = KronometerContract.Bikers.NAME;
    String COL_SURNAME = KronometerContract.Bikers.SURNAME;
    String COL_START_TIME = KronometerContract.Bikers.START_TIME;
    String COL_END_TIME = KronometerContract.Bikers.END_TIME;

    String DDL_CREATE_TBL_BIKERS =
            "CREATE TABLE bikers (" +
                    "_id          INTEGER  PRIMARY KEY AUTOINCREMENT, \n" +
                    "name         TEXT,\n" +
                    "surname      TEXT, \n" +
                    "start_time   INTEGER, \n" +
                    "end_time     INTEGER \n" +
                    ")";


    String DDL_DROP_TBL_BIKERS =
            "DROP TABLE IF EXISTS bikers";

    String DML_WHERE_ID_CLAUSE = "_id = ?";

    String DEFAULT_TBL_ITEMS_SORT_ORDER = "_id ASC";

}
