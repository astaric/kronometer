package net.staric.kronometer.models;

import java.util.Date;


public class Event {
    private Date time;
    private Contestant contestant;

    public Event(Date eventTime) {
        time = eventTime;
        contestant = null;
    }

    public Date getTime() {
        return time;
    }

    public Contestant getContestant() {
        return contestant;
    }

    public void setContestant(Contestant contestant) {
        if (this.contestant != null)
            throw new IllegalArgumentException("Event is already connected to a contestant.");
        this.contestant = contestant;
    }

    @Override
    public String toString() {
        return time.toString();
    }
}
