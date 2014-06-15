package net.staric.kronometer;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static net.staric.kronometer.KronometerContract.Bikers;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "Kronometer.sync";

    private static final String SERVER_ENDPOINT = "https://kronometer.herokuapp.com/";

    private static String getBikerListEndpoint() {
        return SERVER_ENDPOINT + "biker/list";
    }
    private static String getUpdateBikerEndpoint() { return SERVER_ENDPOINT + "biker/update"; }

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
        this.contentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(
            Account account,
            Bundle bundle,
            String s,
            ContentProviderClient contentProviderClient,
            SyncResult syncResult) {
        Log.i(TAG, "Performing sync");
        uploadPendingChanges();
        syncContestants();
    }

    private void uploadPendingChanges() {
        String selection = "(" + Bikers.UPLOADED + " = 0)";
        Cursor cursor = contentResolver.query(Bikers.CONTENT_URI, null, selection, null, null);
        while (cursor != null && cursor.moveToNext()) {
            Contestant contestant = Contestant.fromCursor(getContext(), cursor);

            if (uploadContestant(contestant)) {
                contestant.markUploaded();
            }
        }
        cursor.close();
    }

    private boolean uploadContestant(Contestant contestant) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(getUpdateBikerEndpoint());
        try {
            List<NameValuePair> nameValuePairs = contestant.toListOfNameValuePairs();
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == 200) {
                Log.i(TAG, String.format("Uploaded contestant %s", contestant.name));
                return true;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, String.format("Error uploading contestant %s", contestant.name));
        return false;
    }

    protected void syncContestants() {
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
                Contestant receivedContestant = Contestant.fromJSON(getContext(),
                                                                    jsonArray.getJSONObject(i));

                Uri contestantUri = ContentUris.withAppendedId(Bikers.CONTENT_URI,
                        receivedContestant.id);
                Cursor cursor = contentResolver.query(contestantUri,
                        new String[]{}, "", new String[]{}, "");
                if (cursor != null && cursor.moveToFirst()) {
                    Contestant dbContestant = Contestant.fromCursor(getContext(), cursor);

                    if (dbContestant.mergeWith(receivedContestant)) {
                        updated += 1;
                    }
                } else {
                    created += 1;

                    receivedContestant.insert();
                }
                cursor.close();
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

    protected String readResponse(HttpResponse response) throws IOException {
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
                    e.printStackTrace();
                }
            }
        }
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
}