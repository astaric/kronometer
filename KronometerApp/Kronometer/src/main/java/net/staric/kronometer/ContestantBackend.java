package net.staric.kronometer;

import android.util.SparseArray;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;

public class ContestantBackend {
    private static ContestantBackend instance = null;
    protected ContestantBackend() {}
    public static ContestantBackend getInstance() {
        if(instance == null) {
            instance = new ContestantBackend();
        }
        return instance;
    }

    private ArrayList<Contestant> contestants = new ArrayList<Contestant>();
    private SparseArray<Contestant> contestantMap = new SparseArray<Contestant>();
    private ArrayList<Contestant> pendingContestants = new ArrayList<Contestant>();

    public List<Contestant> getContestants() {
        return Collections.unmodifiableList(contestants);
    }

    public int getNumberOfPendingContestants() {
        return pendingContestants.size();
    }

    public List<Contestant> getPendingContestants() {
        return new ArrayList<Contestant>(pendingContestants);
    }

    public void pullContestants() {
        String contestantsJson = downloadContestantList();
        try {
            JSONArray jsonArray = new JSONArray(contestantsJson);

            for(int i=0; i<jsonArray.length(); i++) {
                Contestant newContestant = Contestant.fromJson(jsonArray.getJSONObject(i));
                Contestant existingContestant;
                if ((existingContestant = contestantMap.get(newContestant.id, null)) != null) {
                    existingContestant.name = newContestant.name;
                    existingContestant.surname = newContestant.surname;
                } else {
                    contestants.add(newContestant);
                    contestantMap.put(newContestant.id, newContestant);
                }
            }
        } catch (JSONException exc) {
            System.out.println(exc.getMessage());
        }
    }

    private String downloadContestantList() {
        BufferedReader in = null;
        try {
            HttpClient client = new DefaultHttpClient();

            HttpGet request = new HttpGet();
            request.setHeader("Content-Type", "text/plain; charset=utf-8");
            request.setURI(new URI("https://kronometer.herokuapp.com/biker/list"));
            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line + '\n');
            }
            in.close();
            return sb.toString();
        } catch (Exception exc) {
            System.out.println(exc.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {}
            }
        }
        return "[]";
    }

    public void pushContestant(Contestant contestant) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("https://kronometer.herokuapp.com/biker/set_start_time");

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(
                    new BasicNameValuePair("number", "" + contestant.id));
            nameValuePairs.add(
                    new BasicNameValuePair("start_time", "" + contestant.startTime.getTime()));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpclient.execute(httppost);
            pendingContestants.remove(contestant);
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

    }

    public void updateStartTime(Contestant contestant, Date startTime) {
        contestant.startTime = startTime;
        contestant.syncStatus = "SYNC";
        pendingContestants.add(contestant);
    }
}
