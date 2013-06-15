package net.staric.kronometer.backend;

import net.staric.kronometer.models.Category;
import net.staric.kronometer.models.Contestant;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class NewContestantUpdate extends Update {
    int number;
    String name;
    String surname;
    Category category;
    boolean domestic;

    protected NewContestantUpdate(Contestant contestant) {
        this.number = contestant.id;
        this.name = contestant.name;
        this.surname = contestant.surname;
        this.category = contestant.category;
        this.domestic = contestant.domestic;
    }

    protected String getUpdateUrl() {
        return "https://kronometer.herokuapp.com/biker/create";
    }

    protected List<NameValuePair> getUpdateParameters() {
        List<NameValuePair> params = new ArrayList<NameValuePair>(6);
        params.add(new BasicNameValuePair("number", "" + number));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("surname", surname));
        if (category != null)
            params.add(new BasicNameValuePair("category", "" + category.id));
        if (domestic)
            params.add(new BasicNameValuePair("domestic", "true"));
        return params;
    }

}