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
import java.util.Date;

public class MainActivity extends Activity {
    ArrayList<Contestant> listItems=new ArrayList<Contestant>();
    ArrayAdapter<Contestant> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView contestants = (ListView) findViewById(R.id.contestantsList);
        adapter=new ContestantAdapter(this,
                R.layout.contestant_listitem,
                listItems);
        contestants.setAdapter(adapter);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setAdapter(adapter);
        final MainActivity xxx = this;
        contestants.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                xxx.selectPlayer(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    int i = 1;
    public void clickStart(View view) {
        listItems.add(new Contestant(i++, "Anze", "Staric"));
        adapter.notifyDataSetChanged();
        ListView contestants = (ListView) findViewById(R.id.contestantsList);
        contestants.smoothScrollToPosition(0);
    }

    public void selectPlayer(int index) {
        Contestant player = listItems.get(index);
        player.startTime = new Date();
        adapter.notifyDataSetChanged();
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

        return row;
    }

    static class ContestantHolder
    {
        TextView txtId;
        TextView txtName;
        TextView txtStartTime;
    }
}