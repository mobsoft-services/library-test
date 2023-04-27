package com.mobsoft.library;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class HttpConnect extends AsyncTask<String, Void, String> {

    protected String doInBackground(String urlConnect) {
        try {
            URL url = new URL(urlConnect);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // Build JSON request body
            JSONObject json = new JSONObject();
            json.put("name", "John");
            json.put("age", 30);
            String requestBody = json.toString();

            // Send request body
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            os.close();

            // Read response
            InputStream inputStream = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();
            inputStream.close();
            System.out.println(response.toString());
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected String doInBackground(String... strings) {
        return null;
    }

    protected void onPostExecute(String result) {
        // Handle response here
    }
}
