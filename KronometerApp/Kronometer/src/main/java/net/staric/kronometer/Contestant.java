package net.staric.kronometer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static net.staric.kronometer.KronometerContract.Bikers;


public class Contestant {
    private static final String TAG = "Kronometer.sync";
    private final Uri uri;
    private final ContentResolver contentResolver;
    private final Context context;

    public Long id;
    public String name;
    public Long startTime;
    public Long endTime;

    public Contestant(Context context, Long id) {
        this.context = context;
        this.contentResolver = context.getContentResolver();
        this.id = id;
        this.uri = ContentUris.withAppendedId(Bikers.CONTENT_URI, id);
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

        context.getContentResolver().insert(Bikers.CONTENT_URI, contentValues);
        return new Contestant(context, id);
    }

    public void setEndTime(Date endTime) {
        setEndTime(endTime.getTime());
    }

    public void setEndTime(long timestamp) {
        try {
            ContentValues contentValues = new ContentValues(2);
            contentValues.put(Bikers.END_TIME, timestamp);
            contentValues.put(Bikers.UPLOADED, 0);
            contentResolver.update(uri, contentValues, null, null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void setFinishTime(Date date) {
        setFinishTime(date.getTime());
    }

    public void setFinishTime(Long timestamp) {
        try {
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(Bikers.ON_FINISH, timestamp);
            contentResolver.update(uri, contentValues, null, null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void setStartTime(Long startTime) {
        try {
            ContentValues contentValues = new ContentValues(2);
            contentValues.put(Bikers.START_TIME, startTime);
            contentValues.put(Bikers.UPLOADED, 0);

            contentResolver.update(uri, contentValues, null, null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public static Contestant fromJSON(Context context, JSONObject jsonObject) throws JSONException {
        JSONObject fields = jsonObject.getJSONObject("fields");

        Contestant c = new Contestant(context, fields.getLong("number"));
        c.name = fields.getString("name") + " " + fields.getString("surname");
        c.startTime = parseDate(fields.getString("start_time"));
        c.endTime = parseDate(fields.getString("end_time"));

        return c;
    }

    public static Contestant fromCursor(Context context, Cursor cursor) {
        Contestant c = new Contestant(context, cursor.getLong(cursor.getColumnIndex(Bikers._ID)));

        c.name = cursor.getString(cursor.getColumnIndex(Bikers.NAME));
        c.startTime = cursor.getLong(cursor.getColumnIndex(Bikers.START_TIME));
        c.endTime = cursor.getLong(cursor.getColumnIndex(Bikers.END_TIME));

        return c;
    }

    protected static Long parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return format.parse(dateString).getTime();
        } catch (ParseException e) {
            return null;
        }
    }

    public boolean mergeWith(Contestant other) {
        boolean merged = false;

        ContentValues contentValues = new ContentValues(0);
        if ((other.name != null) && !name.equals(other.name)) {
            name = other.name;
            contentValues.put(Bikers.NAME, other.name);
            merged = true;
        }
        if ((other.startTime != null) && (startTime < other.startTime)) {
            startTime = other.startTime;
            contentValues.put(Bikers.START_TIME, other.startTime);
            merged = true;
        }
        if ((other.endTime != null) && (endTime < other.endTime)) {
            endTime = other.endTime;
            contentValues.put(Bikers.END_TIME, endTime);
            merged = true;
        }

        if (merged) {
            Log.i(TAG, String.format("Merging contestant %s", this.name));
            contentValues.put(Bikers.UPLOADED, 1);
            contentResolver.update(uri, contentValues, null, null);
        }
        return merged;
    }

    public void insert() {
        ContentValues contentValues = new ContentValues(4);
        contentValues.put(Bikers._ID, id);
        contentValues.put(Bikers.NAME, name);
        if (startTime != null)
            contentValues.put(Bikers.START_TIME, startTime);
        if (endTime != null)
            contentValues.put(Bikers.END_TIME, endTime);
        contentValues.put(Bikers.UPLOADED, 1);
        contentResolver.insert(Bikers.CONTENT_URI, contentValues);
    }

    public void markUploaded() {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(Bikers.UPLOADED, 1);
        contentResolver.update(uri, contentValues, null, null);
    }

    public List<NameValuePair> toListOfNameValuePairs() {
        List<NameValuePair> params = new ArrayList<NameValuePair>(3);
        params.add(new BasicNameValuePair("number", "" + id));
        if (startTime != null) {
            params.add(new BasicNameValuePair("start_time", "" + startTime));
        }
        if (endTime != null) {
            params.add(new BasicNameValuePair("end_time", "" + endTime));
        }
        return params;
    }
}
