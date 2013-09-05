package net.staric.kronometer.backend;

import net.staric.kronometer.models.Category;
import net.staric.kronometer.models.Contestant;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

class ContestantSynchronizationThread extends Thread {
    private final KronometerService kronometerService;

    private final HttpClient httpClient;
    private final HttpGet contestantListRequest;
    private final HttpGet categoryListRequest;

    public ContestantSynchronizationThread(String serverEndpoint, KronometerService kronometerService) {
        this.kronometerService = kronometerService;
        if (!serverEndpoint.endsWith("/"))
            serverEndpoint += "/";

        httpClient = new DefaultHttpClient();
        contestantListRequest = createRequest(serverEndpoint + "biker/list");
        categoryListRequest = createRequest(serverEndpoint + "category/list");
    }

    private HttpGet createRequest(String endpoint) {
        try {
            HttpGet request = new HttpGet();
            request.setHeader("Content-Type", "text/plain; charset=utf-8");
            request.setURI(new URI(endpoint));
            return request;
        } catch (URISyntaxException e) {
            kronometerService.setSyncStatus(String.format("Invalid endpoint: %s", endpoint));
        }
        return null;
    }

    @Override
    public void run() {
        if (contestantListRequest == null || categoryListRequest == null)
            return;
        while (!isInterrupted()) {
            try {
                ArrayList<Contestant> contestants = new Deserializer<Contestant>() {
                    @Override
                    Contestant fromJson(JSONObject jsonObject) {
                        return Contestant.fromJson(jsonObject);
                    }
                }.deserialize(download(contestantListRequest));

                ArrayList<Category> categories = new Deserializer<Category>() {
                    @Override
                    Category fromJson(JSONObject jsonObject) {
                        return Category.fromJson(jsonObject);
                    }
                }.deserialize(download(categoryListRequest));

                kronometerService.updateContestants(contestants);
                kronometerService.updateCategories(categories);
            } catch (IOException e) {
                kronometerService.setSyncStatus("Could not access server");
            } catch (JSONException e) {
                kronometerService.setSyncStatus("Server returned invalid JSON");
            }

            kronometerService.setSyncStatus("Contestants synchronized");
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private String download(HttpGet request) throws IOException {
        HttpResponse response = httpClient.execute(request);
        return readResponse(response);
    }

    private String readResponse(HttpResponse response) throws IOException {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuilder sb = new StringBuilder("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            in.close();
            return sb.toString();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {}
            }
        }
    }
}