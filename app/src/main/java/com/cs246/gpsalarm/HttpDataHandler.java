package com.cs246.gpsalarm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * HTTP DATA HANDLER
 * @author Hernan Yupanqui
 * @version 1.2
 * @since 2020-03-20
 * <p>
 * This class is used to manage the request of coordinates to the API.
 * We use this only for receiving the coordinates when the user enters a desired area.
 */
public class HttpDataHandler {

    // Constructor
    public HttpDataHandler() {

    }
     /**
      * This function is called each time there is a request to get the coordinates.
      *
      * @param requestURL is a reference to a request made to get the URL location.
      */
    public String getHTTPData(String requestURL) {
        URL url;
        String response = " ";
        try {
            url = new URL(requestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlendcode");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null)
                    response += line;
            } else {
                response = " ";
            }
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
