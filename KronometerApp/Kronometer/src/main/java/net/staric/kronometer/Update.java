package net.staric.kronometer;

import net.staric.kronometer.utils.Utils;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

public abstract class Update {
    public boolean push() {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(this.getUpdateUrl());
        try {
            List<NameValuePair> nameValuePairs = this.getUpdateParameters();
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                String error = Utils.responseToString(response);
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    abstract protected String getUpdateUrl();
    abstract protected List<NameValuePair> getUpdateParameters();
}
