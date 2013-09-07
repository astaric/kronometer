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
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

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

        try {
            pushUpdates(false);
            while (!isInterrupted()) {
                try {
                    pullContestants();
                    //pullCategories();
                    updateServiceStatus();
                } catch (IOException e) {
                    e.printStackTrace();
                    kronometerService.setSyncStatus("Could not access server");
                } catch (JSONException e) {
                    kronometerService.setSyncStatus("Server returned invalid JSON");
                }

                pushUpdates(true);
                updateServiceStatus();
            }
        } catch (InterruptedException e) {
            return;
        }
    }

    private void updateServiceStatus() {
        if (kronometerService.getUpdates().size() > 0) {
            kronometerService.setSyncStatus(String.format("%d updates pending", kronometerService.getUpdates().size()));
        } else {
            kronometerService.setSyncStatus("Synchronized");
        }
    }

    private void pushUpdates(boolean blocking) throws InterruptedException {
        List<Update> failedUpdates = new ArrayList<Update>();
        BlockingQueue<Update> updateQueue = kronometerService.getUpdates();
        Update update;
        int timeout = blocking ? 60 : 0;
        while ((update = updateQueue.poll(timeout, TimeUnit.SECONDS)) != null) {
            kronometerService.setSyncStatus(String.format("Uploading (%d)", updateQueue.size() + 1));
            if (!update.push())
                failedUpdates.add(update);
        }
        for (Update update2 : failedUpdates) {
            kronometerService.addUpdate(update2);
        }
    }

    private void pullCategories() throws JSONException, IOException {
        ArrayList<Category> categories = new Deserializer<Category>() {
            @Override
            Category fromJson(JSONObject jsonObject) {
                return Category.fromJson(jsonObject);
            }
        }.deserialize(download(categoryListRequest));
        kronometerService.updateCategories(categories);
    }

    private void pullContestants() throws JSONException, IOException {
        kronometerService.setSyncStatus("Synchronizing");
        ArrayList<Contestant> contestants = new Deserializer<Contestant>() {
            @Override
            Contestant fromJson(JSONObject jsonObject) {
                return Contestant.fromJson(jsonObject);
            }
        }.deserialize(download(contestantListRequest));
        kronometerService.updateContestants(contestants);
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
                } catch (IOException e) {
                }
            }
        }
    }
}