package net.staric.kronometer.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {
    public static String responseToString(HttpResponse response) {
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return "";
    }

    public static boolean hasInternetConnection(Activity activity) {
        ConnectivityManager connMgr = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean hasConnection =  networkInfo != null && networkInfo.isConnected();
        if (!hasConnection)
            new AlertDialog.Builder(activity)
                    .setTitle("No connection")
                    .setMessage("Your internet connections is not available.")
                    .setNeutralButton("Close", null)
                    .show();
        return hasConnection;
    }
}
