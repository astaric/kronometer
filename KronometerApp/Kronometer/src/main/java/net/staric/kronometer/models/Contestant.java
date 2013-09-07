package net.staric.kronometer.models;

import net.staric.kronometer.backend.ContestantBackend;
import net.staric.kronometer.backend.EndTimeUpdate;
import net.staric.kronometer.backend.KronometerService;
import net.staric.kronometer.backend.StartTimeUpdate;
import net.staric.kronometer.backend.Update;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class Contestant implements Comparable<Contestant> {
    public int id;
    public String name;
    public String surname;
    public Category category;
    public boolean domestic;
    private Date startTime;
    private Date endTime;

    public boolean dummy=false;

    public Contestant() {
        dummy=true;
        id=999999;
    }

    public Contestant(int id, String name, String surname) {
        this(id, name, surname, null, false, null, null);
    }

    public Contestant(int id, String name, String surname, Category category, boolean domestic) {
        this(id, name, surname, category, domestic, null, null);
    }

    private Contestant(int id, String name, String surname, Category category, boolean domestic, Date startTime, Date endTime) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.category = category;
        this.domestic = domestic;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getFullName() {
        return String.format("%s %s", this.name, this.surname);
    }

    public Date getStartTime() {
        return startTime;
    }

    public Update setStartTime(Date startTime) {
        if (dummy)
            return null;
        this.startTime = startTime;
        Update update = new StartTimeUpdate(this);
        ContestantBackend.getInstance().addUpdate(update);
        return update;
    }

    public Date getEndTime() {
        return endTime;
    }

    public Update setEndTime(Date endTime) {
        this.endTime = endTime;
        return new EndTimeUpdate(this);
    }

    @Override
    public String toString() {
        return getFullName();
    }

    public static Contestant fromJson(JSONObject jsonObject) {
        try {
            JSONObject fields = jsonObject.getJSONObject("fields");
            int id = fields.getInt("number");
            String name = fields.getString("name");
            String surname = fields.getString("surname");
            Date startTime = parseDate(fields.getString("start_time"));
            Date endTime = parseDate(fields.getString("end_time"));

            return new Contestant(id, name, surname, null, false, startTime, endTime);
        } catch (JSONException exc) {
            return null;
        }
    }

    private static Date parseDate(String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ss.SSS'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public int compareTo(Contestant other) {
        return ((Integer)this.id).compareTo(other.id);
    }

    public boolean update(Contestant contestant) {
        boolean modified = false;
        if (contestant.name != this.name) {
            this.name = contestant.name;
            modified = true;
        }
        if (contestant.surname != this.surname) {
            this.surname = contestant.surname;
            modified = true;
        }
        if (contestant.startTime != this.startTime && contestant.startTime != null) {
            this.startTime = contestant.startTime;
            modified = true;
        }
        if (contestant.endTime != this.endTime && contestant.endTime != null) {
            this.endTime = contestant.endTime;
            modified = true;
        }

        return modified;
    }
}



