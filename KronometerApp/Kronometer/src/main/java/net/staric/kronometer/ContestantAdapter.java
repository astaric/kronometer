package net.staric.kronometer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.staric.kronometer.models.Contestant;

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
            holder.frame = (RelativeLayout)row.findViewById(R.id.frame);
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
        if (contestant.dummy) {
            holder.txtId.setText("");
            holder.txtName.setText("");
            holder.txtStartTime.setText("");
        } else {
            if (contestant.getStartTime() != null)
                holder.frame.setBackgroundColor(0xff630b08);
            else
                holder.frame.setBackgroundColor(0x00000000);
            holder.txtId.setText(((Integer) contestant.id).toString());
            holder.txtName.setText(contestant.getFullName());
            holder.txtStartTime.setText((contestant.getStartTime() == null) ? "" : contestant.getStartTime().toString());
        }

        return row;
    }

    static class ContestantHolder
    {
        RelativeLayout frame;
        TextView txtId;
        TextView txtName;
        TextView txtStartTime;
        TextView txtSyncStatus;
    }
}
