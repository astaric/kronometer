package net.staric.kronometer;

import java.util.Date;

public class CountdownBackend {
    private static CountdownBackend instance = null;
    protected CountdownBackend() {}
    public static CountdownBackend getInstance() {
        if(instance == null) {
            instance = new CountdownBackend();
        }
        return instance;
    }

    private Date lastReset;

    public void resetCountdown() {
        lastReset = new Date();
    }

    public int getCountdownValue() {
        if (lastReset == null)
            return 0;

        Date currentTime = new Date();
        int countdownValue = (int)(lastReset.getTime() + 30000 - currentTime.getTime()) / 1000;
        return countdownValue > 0 ? countdownValue : 0;
    }
}
