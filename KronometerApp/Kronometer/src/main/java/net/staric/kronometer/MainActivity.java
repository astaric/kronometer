package net.staric.kronometer;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
    ArrayList<Contestant> listItems=new ArrayList<Contestant>();
    ArrayAdapter<Contestant> adapter;

    Timer timer;
    Spinner contestants;
    TextView countdown;

    class updateCountdown extends TimerTask {
        @Override
        public void run() {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Calendar now = Calendar.getInstance();
                    int seconds = now.get(Calendar.SECOND);
                    countdown.setText(String.format("00:%02d", 29-(seconds % 30)));

                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adapter = new ContestantAdapter(this, R.layout.contestant_listitem, listItems);
        //adapter = new ArrayAdapter<Contestant>(this, android.R.layout.simple_list_item_1, listItems);
        contestants = (Spinner)findViewById(R.id.contestants);
        contestants.setAdapter(adapter);
        //this.fillContestants();

        countdown = (TextView)findViewById(R.id.countdown);
        timer = new Timer();
        timer.schedule(new updateCountdown(), 0, 500);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh_bikers:
                updateBikers();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateBikers() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadBikerListTask().execute("https://kronometer.herokuapp.com/biker/list");
        } else {
            new AlertDialog.Builder(this).setTitle("Ne dela").setMessage(":(").setNeutralButton("Close", null).show();
        }
    }

    private class DownloadBikerListTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return (new Communicator()).executeHttpGet(urls[0]);
            } catch (Exception e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            try {
                JSONArray jsonArray = new JSONArray(result);
                jsonArray.length();
                for(int i=0; i<jsonArray.length(); i++) {
                    JSONObject biker = jsonArray.getJSONObject(i);
                    int biker_id = biker.getInt("pk");
                    String biker_name = biker.getJSONObject("fields").getString("name");
                    String biker_surname = biker.getJSONObject("fields").getString("surname");

                    Contestant contestant = null;
                    for (int j=0; j<listItems.size(); j++) {
                        Contestant candidate = listItems.get(j);
                        if (candidate.id == biker_id) {
                            contestant = candidate;
                            break;
                        }
                    }
                    if (contestant != null) {
                        contestant.name = biker_name;
                        contestant.surname = biker_surname;
                    } else {
                        contestant = new Contestant(biker_id, biker_name, biker_surname);
                        listItems.add(contestant);
                    }

                }
                adapter.notifyDataSetChanged();
            } catch (JSONException exc) {
                System.out.println(exc.getMessage());
            }
        }
    }

    private class Communicator {
        public String executeHttpGet(String URL) throws Exception
        {
            BufferedReader in = null;
            try
            {
                HttpClient client = new DefaultHttpClient();

                client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");
                HttpGet request = new HttpGet();
                request.setHeader("Content-Type", "text/plain; charset=utf-8");
                request.setURI(new URI(URL));
                HttpResponse response = client.execute(request);
                in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                StringBuffer sb = new StringBuffer("");
                String line = "";

                String NL = System.getProperty("line.separator");
                while ((line = in.readLine()) != null)
                {
                    sb.append(line + NL);
                }
                in.close();
                String page = sb.toString();
                //System.out.println(page);
                return page;
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        Log.d("BBB", e.toString());
                    }
                }
            }
        }
    }

    public void fillContestants() {
        for (int i=0; i<30; i++) {
            listItems.add(new Contestant(i, "Anze", "Staric"));
        }
        adapter.notifyDataSetChanged();
    }

    public void clickStart(View view) {
        int index = contestants.getSelectedItemPosition();
        Contestant biker = (Contestant)contestants.getSelectedItem();
        biker.startTime = new Date();
        adapter.notifyDataSetChanged();
        if (index < contestants.getCount() - 1) {
            contestants.setSelection(index+1);
        }

    }
}

class Contestant {
    int id;
    String name;
    String surname;
    Date startTime;
    Date endTime;

    Contestant(int id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
    }

    String getFullName() {
        return String.format("%s %s", this.name, this.surname);
    }

    @Override
    public String toString() {
        return getFullName();
    }
}

class ContestantAdapter extends ArrayAdapter<Contestant>{

    Context context;
    int layoutResourceId;
    ArrayList<Contestant> data = null;

    public ContestantAdapter(Context context, int layoutResourceId, ArrayList<Contestant> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ContestantHolder holder = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ContestantHolder();
            holder.txtId = (TextView)row.findViewById(R.id.cid);
            holder.txtName = (TextView)row.findViewById(R.id.name);
            holder.txtStartTime = (TextView)row.findViewById(R.id.startTime);
            holder.txtSyncStatus = (TextView)row.findViewById(R.id.syncStatus);

            row.setTag(holder);
        }
        else
        {
            holder = (ContestantHolder)row.getTag();
        }

        Contestant contestant = data.get(position);
        holder.txtId.setText(((Integer) contestant.id).toString());
        holder.txtName.setText(contestant.getFullName());
        holder.txtStartTime.setText((contestant.startTime == null) ? "" : contestant.startTime.toString());
        holder.txtSyncStatus.setText("OK");

        return row;
    }

    static class ContestantHolder
    {
        TextView txtId;
        TextView txtName;
        TextView txtStartTime;
        TextView txtSyncStatus;
    }
}