package net.staric.kronometer.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import static net.staric.kronometer.KronometerContract.Bikers;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "Kronometer.sync";

    private static final String SERVER_ENDPOINT = "https://kronometer.herokuapp.com/";

    private static String getBikerListEndpoint() {
        return SERVER_ENDPOINT + "biker/list";
    }

    ContentResolver contentResolver;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        contentResolver = context.getContentResolver();
    }

    /**
     * Compatibility with Android 3.0
     */
    public SyncAdapter(
            Context context,
            boolean autoInitialize,
            boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        contentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle bundle,
            String s,
            ContentProviderClient contentProviderClient,
            SyncResult syncResult) {
        Log.i(TAG, "Performing sync");
        getContestants();
    }

    private void getContestants() {
        HttpClient httpClient = new DefaultHttpClient();
        int created = 0;
        int updated = 0;
        try {
            HttpGet request = new HttpGet();
            request.setHeader("Content-Type", "text/plain; charset=utf-8");
            request.setURI(new URI(getBikerListEndpoint()));

            String response = readResponse(httpClient.execute(request));
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject fields = jsonObject.getJSONObject("fields");

                int id = fields.getInt("number");
                String name = fields.getString("name");
                String surname = fields.getString("surname");
                Long startTime = parseDate(fields.getString("start_time"));
                Long endTime = parseDate(fields.getString("end_time"));

                Uri contestantUri = ContentUris.withAppendedId(Bikers.CONTENT_URI, id);
                Cursor cursor = contentResolver.query(contestantUri,
                        new String[]{}, "", new String[]{}, "");
                if (cursor != null && cursor.moveToFirst()) {
                    ContentValues contentValues = new ContentValues(0);
                    if (!cursor.getString(cursor.getColumnIndex(Bikers.NAME))
                            .equals(name + " " + surname)) {
                        contentValues.put(Bikers.NAME, name + " " + surname);
                    }
                    if (cursor.getLong(cursor.getColumnIndex(Bikers.START_TIME))
                            < (long) startTime) {
                        contentValues.put(Bikers.START_TIME, startTime);
                    }
                    if (cursor.getLong(cursor.getColumnIndex(Bikers.END_TIME))
                            < (long) endTime) {
                        contentValues.put(Bikers.END_TIME, endTime);
                    }

                    if (contentValues.size() > 0) {
                        updated += 1;
                        contentResolver.update(contestantUri, contentValues, null, null);
                    }
                } else {
                    created += 1;
                    ContentValues contentValues = new ContentValues(4);
                    contentValues.put(Bikers._ID, id);
                    contentValues.put(Bikers.NAME, name + " " + surname);
                    contentValues.put(Bikers.START_TIME, startTime);
                    contentValues.put(Bikers.END_TIME, endTime);
                    contentResolver.insert(Bikers.CONTENT_URI, contentValues);
                }
            }
            Log.i(TAG, "Created " + created + " , updated " + updated + " contestants.");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String readResponse(HttpResponse response) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder sb = new StringBuilder("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            in.close();
            return sb.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static long parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return format.parse(dateString).getTime();
        } catch (ParseException e) {
            return 0;
        }
    }
}

abstract class Deserializer<T> {
    abstract T fromJson(JSONObject jsonObject);

    ArrayList<T> deserialize(String jsonEncodedObject) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonEncodedObject);
        ArrayList<T> deserializedArray = new ArrayList<T>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            deserializedArray.add((T) fromJson(jsonArray.getJSONObject(i)));
        }
        return deserializedArray;
    }
}