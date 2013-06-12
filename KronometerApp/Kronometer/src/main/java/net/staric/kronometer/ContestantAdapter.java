package net.staric.kronometer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContestantAdapter extends ArrayAdapter<Contestant> {

    Context context;
    int layoutResourceId;
    List<Contestant> data = null;

    public ContestantAdapter(Context context, int layoutResourceId, List<Contestant> data) {
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
