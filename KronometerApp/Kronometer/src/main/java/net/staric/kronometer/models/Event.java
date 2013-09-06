package net.staric.kronometer.models;

import java.util.Date;


public class Event {
    public Event(Date eventTime) {
        time = eventTime;
        contestant = null;
    }

    private Date time;
    public Date getTime() {
        return time;
    }

    private Contestant contestant;
    public Contestant getContestant() {
        return contestant;
    }

    public void setContestant(Contestant contestant) {
        if (this.contestant != null)
            throw new IllegalArgumentException("Event is already connected to a contestant.");
        this.contestant = contestant;
    }

    private boolean selected;
    public boolean isSelected() {
        return selected;
    }public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return time.toString();
    }
}
