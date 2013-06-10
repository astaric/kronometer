package net.staric.kronometer;

import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

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
        this.fillContestants();

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