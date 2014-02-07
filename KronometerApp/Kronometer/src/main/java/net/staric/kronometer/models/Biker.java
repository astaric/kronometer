package net.staric.kronometer.models;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import net.staric.kronometer.KronometerContract;

import java.util.Date;


public class Biker {
    private final Context context;
    private final Uri uri;

    public Biker(Context context, Long id) {
        this.context = context;
        this.uri = ContentUris.withAppendedId(KronometerContract.Bikers.CONTENT_URI, id);
    }

    public void setEndTime(Date endTime) {
        setEndTime(endTime.getTime());
    }

    public void setEndTime(long timestamp) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(KronometerContract.Bikers.END_TIME, timestamp);
        context.getContentResolver().update(uri, contentValues, null, null);
    }
}
