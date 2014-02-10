package net.staric.kronometer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import net.staric.kronometer.KronometerContract;

import java.util.Date;

import static net.staric.kronometer.KronometerContract.SensorEvent;


public class Event {
    private final Context context;
    private final Uri uri;

    public Event(Context context, Long id) {
        this.context = context;
        this.uri = ContentUris.withAppendedId(SensorEvent.CONTENT_URI, id);
    }

    public static void create(Context context, Date date) {
        create(context, date.getTime());
    }

    public static void create(Context context, Long timestamp) {
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SensorEvent.TIMESTAMP, timestamp);
        contentResolver.insert(SensorEvent.CONTENT_URI, contentValues);
    }
}
