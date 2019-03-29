package com.locateme.indoor_locator;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpGetRequest extends AsyncTask<String, Void, String> {
    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;
    public static final String BASE_URL = "http://localhost:5000/";

    @Override
    protected String doInBackground(String... params){
        String fname = "initial";
        String stringUrl = BASE_URL + params[0];
        try {
            //Create a URL object holding our url
            URL myUrl = new URL(stringUrl);
            //Create a connection
            HttpURLConnection connection =(HttpURLConnection)
                    myUrl.openConnection();

            //Set methods and timeouts
            connection.setRequestMethod(REQUEST_METHOD);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setConnectTimeout(CONNECTION_TIMEOUT);

            //Add parameters to GET
            connection.setRequestProperty("email",params[1]);
            connection.setRequestProperty("password",params[2]);

            //Connect to our url
            //connection.connect();
            Log.d("CONNECTION","CONNECTED");

            fname = "connected";

        } catch(IOException e){
            e.printStackTrace();
            fname = "erorr";
            Log.d("CONNECTION","NOT CONNECTED");

        }
        return fname;
    }
    @Override
    protected void onPostExecute(String result){
        super.onPostExecute(result);
    }
}