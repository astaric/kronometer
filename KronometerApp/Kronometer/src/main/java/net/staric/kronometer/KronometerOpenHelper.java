package net.staric.kronometer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class KronometerOpenHelper extends SQLiteOpenHelper {
    private static final String NAME = DbSchema.DB_NAME;
    private static final int VERSION = 3;


    public KronometerOpenHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbSchema.DDL_CREATE_TBL_BIKERS);
        db.execSQL(DbSchema.DDL_CREATE_TBL_SENSOR_EVENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DbSchema.DDL_DROP_TBL_BIKERS);
        db.execSQL(DbSchema.DDL_DROP_TBL_SENSOR_EVENTS);
        db.execSQL(DbSchema.DDL_CREATE_TBL_BIKERS);
        db.execSQL(DbSchema.DDL_CREATE_TBL_SENSOR_EVENTS);
    }
}
