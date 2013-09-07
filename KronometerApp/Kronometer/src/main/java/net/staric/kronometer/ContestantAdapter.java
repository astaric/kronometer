package net.staric.kronometer;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.staric.kronometer.models.Contestant;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
            holder.txtTime = (TextView)row.findViewById(R.id.startTime);

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
            holder.txtTime.setText("");
        } else {
            Resources resources = context.getResources();
            if (contestant.getStartTime() != null) {
                holder.frame.setBackgroundColor(resources.getColor(android.R.color.background_light));
                holder.txtId.setTextColor(resources.getColor(android.R.color.primary_text_light));
                holder.txtName.setTextColor(resources.getColor(android.R.color.primary_text_light));
                holder.txtTime.setTextColor(resources.getColor(android.R.color.primary_text_light));
            } else {
                holder.frame.setBackgroundColor(resources.getColor(android.R.color.transparent));
                holder.txtId.setTextColor(resources.getColor(android.R.color.primary_text_dark));
                holder.txtName.setTextColor(resources.getColor(android.R.color.primary_text_dark));
                holder.txtTime.setTextColor(resources.getColor(android.R.color.primary_text_dark));
            }

            holder.txtId.setText(((Integer) contestant.id).toString());
            holder.txtName.setText(contestant.getFullName());
            holder.txtTime.setText(formatTime(contestant));
        }

        return row;
    }

    private String formatTime(Contestant contestant) {
        if (contestant.getStartTime() != null) {
            if (contestant.getEndTime() != null) {
                long duration = contestant.getEndTime().getTime() - contestant.getStartTime().getTime();
                long seconds = duration / 1000 % 60;
                long minutes = duration / 60000;
                return String.format("%02d:%02d", minutes, seconds);
            } else {
                SimpleDateFormat format = new SimpleDateFormat("'started 'HH:mm:ss.SSS");
                return format.format(contestant.getStartTime());
            }
        } else {
            return "";
        }
    }

    static class ContestantHolder
    {
        RelativeLayout frame;
        TextView txtId;
        TextView txtName;
        TextView txtTime;
    }
}
