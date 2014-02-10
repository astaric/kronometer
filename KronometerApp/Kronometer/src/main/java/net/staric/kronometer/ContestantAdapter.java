package net.staric.kronometer;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import static net.staric.kronometer.KronometerContract.Bikers;

abstract class ContestantAdapter extends SimpleCursorAdapter {

    private final LayoutInflater inflater;
    protected boolean highlight = true;
    protected boolean addPlaceholder = false;
    protected Context context;
    protected int layoutResourceId;
    protected final String[] fields;
    private final Resources resources;

    public ContestantAdapter(Context context, boolean highlight, boolean addPlaceholder) {
        super(context, R.layout.listitem_contestant, null, Bikers.PROJECTION_ALL, new int[0], 0);

        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.resources = context.getResources();

        this.layoutResourceId = R.layout.listitem_contestant;
        this.fields = Bikers.PROJECTION_ALL;
        this.addPlaceholder = addPlaceholder;
        this.highlight = highlight;
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
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new ContestantHolder();
            holder.frame = (RelativeLayout) row.findViewById(R.id.frame);
            holder.txtId = (TextView) row.findViewById(R.id.cid);
            holder.txtName = (TextView) row.findViewById(R.id.name);
            holder.txtExtra = (TextView) row.findViewById(R.id.extra);

            row.setTag(holder);
        } else {
            holder = (ContestantHolder) row.getTag();
        }

        Cursor cursor = getCursor();
        boolean validContestant = false;
        if (cursor != null && cursor.moveToPosition(position)) {
            validContestant = true;

            int idField = cursor.getColumnIndex(Bikers._ID);
            String id = cursor.isNull(idField) ? "" : ((Long)cursor.getLong(idField)).toString();
            String name = cursor.getString(cursor.getColumnIndex(Bikers.NAME));

            holder.txtId.setText(id);
            holder.txtName.setText(name);
            holder.txtExtra.setText(getExtra(cursor));
        }

        if (validContestant && highlight && isSelected(cursor)) {
            holder.frame.setBackgroundColor(resources.getColor(android.R.color.background_light));
            holder.txtId.setTextColor(resources.getColor(android.R.color.primary_text_light));
            holder.txtName.setTextColor(resources.getColor(android.R.color.primary_text_light));
            holder.txtExtra.setTextColor(resources.getColor(android.R.color.primary_text_light));
        } else {
            holder.frame.setBackgroundColor(resources.getColor(android.R.color.transparent));
            holder.txtId.setTextColor(resources.getColor(android.R.color.primary_text_dark));
            holder.txtName.setTextColor(resources.getColor(android.R.color.primary_text_dark));
            holder.txtExtra.setTextColor(resources.getColor(android.R.color.primary_text_dark));
        }
        return row;
    }

    protected abstract String getExtra(Cursor cursor);

    protected abstract boolean isSelected(Cursor cursor);

    @Override
    public Cursor swapCursor(Cursor c) {
        if (c != null && c.getCount() > 0 && addPlaceholder) {
            MatrixCursor matrixCursor = new MatrixCursor(fields);
            matrixCursor.addRow(emptyRow());

            c = new MergeCursor(new Cursor[] { c, matrixCursor });
        }
        return super.swapCursor(c);
    }

    protected Object[] emptyRow() {
        Object[] row = new Object[fields.length];
        for (int i=0; i<row.length; i++) {
            row[i] = null;
        }
        return row;
    }

    static class ContestantHolder {
        RelativeLayout frame;
        TextView txtId;
        TextView txtName;
        TextView txtExtra;
    }
}

class StartContestantAdapter extends ContestantAdapter {

    public StartContestantAdapter(Context context, boolean highlight, boolean addPlaceholder) {
        super(context, highlight, addPlaceholder);
    }

    @Override
    protected String getExtra(Cursor cursor) {
        int startTime = cursor.getColumnIndex(Bikers.START_TIME);
        if (cursor.isNull(startTime)) {
            return "";
        } else {
            SimpleDateFormat outFmt = new SimpleDateFormat("HH:mm:ss.SSS");
            return outFmt.format(cursor.getLong(startTime));
        }
    }

    @Override
    protected boolean isSelected(Cursor cursor) {
        return !cursor.isNull(cursor.getColumnIndex(Bikers.START_TIME));
    }
}

class FinishContestantAdapter extends ContestantAdapter {

    public FinishContestantAdapter(Context context, boolean highlight, boolean addPlaceholder) {
        super(context, highlight, addPlaceholder);
    }

    @Override
    protected String getExtra(Cursor cursor) {
        int startTime = cursor.getColumnIndex(Bikers.START_TIME);
        int endTime = cursor.getColumnIndex(Bikers.END_TIME);
        if (cursor.isNull(startTime) || cursor.isNull(endTime)) {
            return "";
        } else {
            SimpleDateFormat outFmt = new SimpleDateFormat("HH:mm:ss.SSS");
            outFmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            return outFmt.format(cursor.getLong(endTime)-cursor.getLong(startTime));
        }
    }

    @Override
    protected boolean isSelected(Cursor cursor) {
        return !cursor.isNull(cursor.getColumnIndex(Bikers.END_TIME));
    }
}