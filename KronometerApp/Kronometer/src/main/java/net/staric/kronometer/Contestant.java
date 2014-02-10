package net.staric.kronometer;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import java.util.Date;

import static net.staric.kronometer.KronometerContract.Bikers;


public class Contestant {
    private final Context context;
    private final Uri uri;

    public Contestant(Context context, Long id) {
        this(context, ContentUris.withAppendedId(Bikers.CONTENT_URI, id));
    }

    public Contestant(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
    }

    public static Contestant create(Context context, Long id, String name, Long startTime,
                               Long endTime) {
        ContentValues contentValues = new ContentValues(4);
        contentValues.put(Bikers._ID, id);
        contentValues.put(Bikers.NAME, name);
        if (startTime != null)
            contentValues.put(Bikers.START_TIME, startTime);
        if (endTime != null)
            contentValues.put(Bikers.END_TIME, endTime);

        return new Contestant(context,
                context.getContentResolver().insert(Bikers.CONTENT_URI, contentValues));
    }

    public void setEndTime(Date endTime) {
        setEndTime(endTime.getTime());
    }

    public void setEndTime(long timestamp) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(Bikers.END_TIME, timestamp);
        context.getContentResolver().update(uri, contentValues, null, null);
    }

    public void setFinishTime(Date date) {
        setFinishTime(date.getTime());
    }

    public void setFinishTime(Long timestamp) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(Bikers.ON_FINISH, timestamp);
        context.getContentResolver().update(uri, contentValues, null, null);
    }

    public void setStartTime(Long startTime) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(KronometerContract.Bikers.START_TIME, startTime);
        context.getContentResolver().update(uri, contentValues, null, null);
    }
}
