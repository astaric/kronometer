package net.staric.kronometer.backend;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.util.SparseArray;

import net.staric.kronometer.models.Category;
import net.staric.kronometer.models.Contestant;
import net.staric.kronometer.KronometerContract;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContestantBackend {
    private static ContestantBackend instance = null;
    protected ContestantBackend() {
        contestants.add(new Contestant());
    }
    public static ContestantBackend getInstance() {
        if(instance == null) {
            instance = new ContestantBackend();
        }
        return instance;
    }

    private ArrayList<Contestant> contestants = new ArrayList<Contestant>();
    private SparseArray<Contestant> contestantMap = new SparseArray<Contestant>();

    private ArrayList<Category> categories = new ArrayList<Category>();
    private SparseArray<Category> categoryMap = new SparseArray<Category>();

    private ArrayList<Update> pendingUpdates = new ArrayList<Update>();

    public List<Contestant> getContestants() {
        return contestants;
    }

    public SparseArray<Contestant> getContestantMap() {
        return contestantMap;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public int getNumberOfPendingContestants() {
        return pendingUpdates.size();
    }

    public List<Update> getPendingUpdates() {
        return new ArrayList<Update>(pendingUpdates);
    }

    public void pull(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        contentResolver.delete(KronometerContract.Bikers.CONTENT_URI, null, null);
        try {
            JSONArray contestantList = new JSONArray(downloadContestantList());
            for(int i=0; i<contestantList.length(); i++) {
                Contestant newContestant = Contestant.fromJson(contestantList.getJSONObject(i));
                ContentValues contentValues = new ContentValues();
                contentValues.put(KronometerContract.Bikers._ID, newContestant.id);
                contentValues.put(KronometerContract.Bikers.NAME, newContestant.getFullName());
                contentResolver.insert(KronometerContract.Bikers.CONTENT_URI, contentValues);
            }
        } catch (JSONException exc) {
            System.out.println(exc.getMessage());
        }
        try {
            JSONArray categoryList = new JSONArray(downloadCategoryList());
            for(int i=0; i<categoryList.length(); i++) {
                Category newCategory = Category.fromJson(categoryList.getJSONObject(i));
                Category existingCategory;
                if ((existingCategory = categoryMap.get(newCategory.id, null)) != null) {
                    existingCategory.name = newCategory.name;
                } else {
                    categories.add(newCategory);
                    categoryMap.put(newCategory.id, newCategory);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.sort(contestants);
    }

    private String downloadContestantList() {
        return downloadList("https://kronometer.herokuapp.com/biker/list");
    }

    private String downloadCategoryList() {
        return downloadList("https://kronometer.herokuapp.com/category/list");
    }

    private String downloadList(String url) {
        BufferedReader in = null;
        try {
            HttpClient client = new DefaultHttpClient();

            HttpGet request = new HttpGet();
            request.setHeader("Content-Type", "text/plain; charset=utf-8");
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder sb = new StringBuilder("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            in.close();
            return sb.toString();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException e) {}
            }
        }
        return "[]";
    }

    public void push(Update update) {
        boolean success = update.push();
        if (success) {
            pendingUpdates.remove(update);
        }
    }

    public Update createContestantUpdate(Contestant contestant) throws Exception {
        if (contestantMap.get(contestant.id, null) != null)
            throw new Exception("Contestant with id already exists.");

        return new NewContestantUpdate(contestant);
    }

    public void addUpdate(Update update) {
        pendingUpdates.add(update);
    }
}
