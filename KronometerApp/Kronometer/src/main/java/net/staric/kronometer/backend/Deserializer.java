package net.staric.kronometer.backend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

abstract class Deserializer<T> {
    abstract T fromJson(JSONObject jsonObject);

    ArrayList<T> deserialize(String jsonEncodedObject) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonEncodedObject);
        ArrayList<T> deserializedArray = new ArrayList<T>(jsonArray.length());
        for(int i=0; i<jsonArray.length(); i++) {
            deserializedArray.add((T) fromJson(jsonArray.getJSONObject(i)));
        }
        return deserializedArray;
    }
}
