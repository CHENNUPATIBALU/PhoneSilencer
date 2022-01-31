package com.phonesilencer;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class FetchLocation extends AsyncTask<String, Void, String> {

    Context context;
    public FetchLocation(Context context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... strings) {
        StringBuffer response;
        try {
            String[] coordinates = Arrays.toString(strings).split(",");
            String latitude = coordinates[0].split("\\[")[1];
            String longitude = coordinates[1].split("]")[0];
            String link = "https://us1.locationiq.com/v1/reverse.php?key=pk.ee8cd2b83d80f7e3764379d409b2aeba&lat="+latitude+"&lon="+longitude+"&format=json";
            URL url = new URL(link);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            // By default it is GET request
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            System.out.println("Response code : " + responseCode);

            // Reading response from input Stream
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String output;
            response = new StringBuffer();

            while ((output = in.readLine()) != null) {
                response.append(output);
            }
            in.close();

            //printing result from response
            String locationData = response.toString();
            System.out.println(locationData);
            JSONObject jsonObject = new JSONObject(locationData);
            return jsonObject.getString("address")+";"+latitude+","+longitude;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String s) {
        new SharedPreferencesHelper(context).setLocationAddress(s.split(";")[0],s.split(";")[1]);
        super.onPostExecute(s);
    }
}