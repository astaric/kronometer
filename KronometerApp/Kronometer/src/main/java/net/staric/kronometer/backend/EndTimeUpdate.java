package net.staric.kronometer.backend;

import net.staric.kronometer.models.Contestant;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EndTimeUpdate extends Update {
    int number;
    Date endTime;

    public EndTimeUpdate(Contestant contestant) {
        this.number = contestant.id;
        this.endTime = contestant.getEndTime();
    }


    protected String getUpdateUrl() {
        return "https://kronometer.herokuapp.com/biker/set_start_time";
    }

    protected List<NameValuePair> getUpdateParameters() {
        List<NameValuePair> params = new ArrayList<NameValuePair>(2);
        params.add(new BasicNameValuePair("number", "" + number));
        params.add(new BasicNameValuePair("start_time", "" + endTime.getTime()));
        return params;
    }
}