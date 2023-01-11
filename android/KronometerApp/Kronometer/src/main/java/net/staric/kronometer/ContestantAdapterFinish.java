package net.staric.kronometer;

import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

class ContestantAdapterFinish extends ContestantAdapter {

    public ContestantAdapterFinish(Context context, boolean highlight, boolean addPlaceholder) {
        super(context, highlight, addPlaceholder);
    }

    @Override
    protected String getExtra(Cursor cursor) {
        int startTime = cursor.getColumnIndex(KronometerContract.Bikers.START_TIME);
        int endTime = cursor.getColumnIndex(KronometerContract.Bikers.END_TIME);
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
        return !cursor.isNull(cursor.getColumnIndex(KronometerContract.Bikers.END_TIME));
    }
}
