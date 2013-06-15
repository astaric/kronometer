package net.staric.kronometer.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Category {
    public int id;
    public String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Category fromJson(JSONObject jsonObject)
    {
        try {
            int id = jsonObject.getInt("pk");
            JSONObject fields = jsonObject.getJSONObject("fields");
            String name = fields.getString("name");

            return new Category(id, name);
        } catch (JSONException exc) {
            return null;
        }
    }

    public String toString() {
        return this.name;
    }
}
