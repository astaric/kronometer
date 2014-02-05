package net.staric.kronometer;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import net.staric.kronometer.R;

import java.text.SimpleDateFormat;

import static net.staric.kronometer.KronometerContract.SensorEvent;


public class EventAdapter extends SimpleCursorAdapter {

    private Context context;
    private int layoutResourceId;
    private Long selectedId = null;

    public Long getSelectedId() {
        return selectedId;
    }

    public void setSelectedId(Long id) {
        selectedId = id;
    }

    public EventAdapter(Context context) {
        this(context, R.layout.listitem_event, null,
                new String[]{SensorEvent._ID, SensorEvent.TIMESTAMP},
                new int[]{R.id.cid, R.id.endTime}, 0);
    }

    private EventAdapter(Context context, int layoutResourceId, Cursor cursor, String[] fields, int[] views, int flags) {
        super(context, layoutResourceId, cursor, fields, views, flags);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
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

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ContestantHolder();
            holder.frame = (RelativeLayout) row.findViewById(R.id.frame);
            holder.txtEndTime = (TextView) row.findViewById(R.id.endTime);
            holder.btnMerge = (Button) row.findViewById(R.id.btnMerge);

            row.setTag(holder);
        } else {
            holder = (ContestantHolder) row.getTag();
        }

        Cursor cursor = getCursor();
        if (cursor != null && cursor.moveToPosition(position)) {
            Long id = cursor.getLong(cursor.getColumnIndex(SensorEvent._ID));
            Long timestamp = cursor.getLong(cursor.getColumnIndex(SensorEvent.TIMESTAMP));

            SimpleDateFormat outFmt = new SimpleDateFormat("HH:mm:ss.SSS");
            holder.txtEndTime.setText(outFmt.format(timestamp));

            if (id == selectedId) {
                holder.btnMerge.setVisibility(View.VISIBLE);
            } else {
                holder.btnMerge.setVisibility(View.INVISIBLE);
            }
        }

        return row;
    }

    static class ContestantHolder {
        RelativeLayout frame;
        TextView txtEndTime;
        Button btnMerge;
    }
}
