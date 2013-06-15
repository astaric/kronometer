package net.staric.kronometer.utils;

import android.os.AsyncTask;

import net.staric.kronometer.ContestantBackend;
import net.staric.kronometer.Update;

public class PushUpdates extends AsyncTask<Update, Integer, Void> {
    @Override
    protected Void doInBackground(Update... updates) {
        int all = updates.length, i = 0;
        ContestantBackend backend = ContestantBackend.getInstance();

        for (Update update: updates) {
            publishProgress(i++, all);
            backend.push(update);
        }
        return null;
    }
}