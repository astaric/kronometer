package net.staric.kronometer;

import android.content.Context;
import android.database.Cursor;

import java.text.SimpleDateFormat;

class ContestantAdapterStart extends ContestantAdapter {

    public ContestantAdapterStart(Context context, boolean highlight, boolean addPlaceholder) {
        super(context, highlight, addPlaceholder);
    }

    @Override
    protected String getExtra(Cursor cursor) {
        int startTime = cursor.getColumnIndex(KronometerContract.Bikers.START_TIME);
        if (cursor.isNull(startTime)) {
            return "";
        } else {
            SimpleDateFormat outFmt = new SimpleDateFormat("HH:mm:ss.SSS");
            return outFmt.format(cursor.getLong(startTime));
        }
    }

    @Override
    protected boolean isSelected(Cursor cursor) {
        return !cursor.isNull(cursor.getColumnIndex(KronometerContract.Bikers.START_TIME));
    }
}
