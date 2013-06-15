package net.staric.kronometer;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Contestant implements Comparable<Contestant> {
    int id;
    String name;
    String surname;
    Date startTime;
    Date endTime;
    String syncStatus;

    Contestant(int id, String name, String surname) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.syncStatus = "";
    }

    String getFullName() {
        return String.format("%s %s", this.name, this.surname);
    }

    @Override
    public String toString() {
        return getFullName();
    }

    static Contestant fromJson(JSONObject jsonObject) {
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
}
