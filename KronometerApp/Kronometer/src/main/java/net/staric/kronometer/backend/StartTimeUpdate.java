package net.staric.kronometer.backend;

import net.staric.kronometer.models.Contestant;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StartTimeUpdate extends Update {
    int number;
    Date startTime;

    public StartTimeUpdate(Contestant contestant) {
        this.number = contestant.id;
        this.startTime = contestant.getStartTime();
    }


    protected String getUpdateUrl() {
        return "https://kronometer.herokuapp.com/biker/set_start_time";
    }

    protected List<NameValuePair> getUpdateParameters() {
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("number", "" + number));
        params.add(new BasicNameValuePair("start_time", "" + startTime.getTime()));
        return params;
    }
}