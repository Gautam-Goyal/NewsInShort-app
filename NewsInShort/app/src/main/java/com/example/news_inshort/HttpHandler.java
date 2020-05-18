package com.example.news_inshort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpHandler {

    public HttpHandler() {

    }

    //makeHttpRequest method which is solely responsible for connection of our app to internet
    public String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.connect();
            inputStream = urlConnection.getInputStream();
            //Calling converyStreamToString method for converting the input stream into string form for future functioning
            jsonResponse = convertStreamToString(inputStream);

        } catch (IOException e) {
            //handle the exception
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            return jsonResponse;
        }
    }

    //converttoStreanToString method for coverting the input raw stream into String form and appending through each line
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {//Catching an exception
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //return string
        return sb.toString();
    }
}
