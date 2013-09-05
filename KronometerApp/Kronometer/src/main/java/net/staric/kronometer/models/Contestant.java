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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Contestant implements Comparable<Contestant> {
    public int id;
    public String name;
    public String surname;
    public Category category;
    public boolean domestic;
    private Date startTime;
    private Date endTime;

    public String syncStatus;

    public boolean dummy=false;

    public Contestant() {
        dummy=true;
        id=999999;
    }

    public Contestant(int id, String name, String surname) {
        this(id, name, surname, null, false);
    }

    public Contestant(int id, String name, String surname, Category category, boolean domestic) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.category = category;
        this.domestic = domestic;
        this.syncStatus = "";
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

            return new Contestant(id, name, surname);
        } catch (JSONException exc) {
            return null;
        }
    }

    public int compareTo(Contestant other) {
        return ((Integer)this.id).compareTo(other.id);
    }

    public boolean update(Contestant contestant) {
        return false;
    }
}



