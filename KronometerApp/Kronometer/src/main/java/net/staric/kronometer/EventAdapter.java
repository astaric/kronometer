package net.staric.kronometer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.models.Event;

import java.text.SimpleDateFormat;
import java.util.List;

public class EventAdapter extends ArrayAdapter<Event> {

    Context context;
    int layoutResourceId;
    List<Event> data = null;

    public EventAdapter(Context context, int layoutResourceId, List<Event> data) {
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
            holder.txtEndTime = (TextView)row.findViewById(R.id.endTime);
            holder.btnMerge = (Button)row.findViewById(R.id.btnMerge);

            row.setTag(holder);
        }
        else
        {
            holder = (ContestantHolder)row.getTag();
        }

        Event event = data.get(position);
        SimpleDateFormat outFmt = new SimpleDateFormat("HH:mm:ss.SSS");
        holder.txtEndTime.setText(outFmt.format(event.getTime()));

        if (event.isSelected()) {
            holder.btnMerge.setVisibility(View.VISIBLE);
        } else {
            holder.btnMerge.setVisibility(View.INVISIBLE);
        }

        return row;
    }

    static class ContestantHolder
    {
        RelativeLayout frame;
        TextView txtEndTime;
        Button btnMerge;
    }
}
