package com.iotaii.card_tracker;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        try {
            // Get the URL from the params
            URL url = new URL(params[0]);

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method
            connection.setRequestMethod("GET");

            // Get response code
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read response
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();
                result = stringBuilder.toString();
            } else {
                result = "Error: " + responseCode;
            }

            // Disconnect connection
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            result = "Error: " + e.getMessage();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        // Here you can handle the result
        // For example, update UI with the result
        System.out.println("Response: " + result);
    }
}
