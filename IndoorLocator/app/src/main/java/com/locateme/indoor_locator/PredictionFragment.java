package com.locateme.indoor_locator;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PredictionFragment extends Fragment implements View.OnClickListener, LocationListener {

    private final String TAG = getClass().getSimpleName();
    private OkHttpClient client = new OkHttpClient();
    private LocationManager lm;
    private List<Scan> mScanList;

    private TextView predict_text;
    private TextView predict_message;
    private Button predict_button;

    private double longitude;
    private double latitude;


    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_prediction, container, false);
        predict_text = v.findViewById(R.id.fragment_prediction_text);
        predict_message = v.findViewById(R.id.fragment_prediction_message);
        predict_button = v.findViewById(R.id.fragment_prediction_button);

        predict_text.setText("");
        predict_message.setText("Message: Wait for Location Signal.");
        predict_button.setEnabled(false);
        predict_button.setOnClickListener(this);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        // Set actions bar, borrowed from Mr. Champions' TicTacToe android app
        try {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setSubtitle("Prediction");
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Could not set subtitle");
        }
    }

    public Location getLastKnownLocation(){
        // Get Last Known GPS Location
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Multiple Attempts to get last known location
        if (location == null){
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null){
            location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }

        return location;
    }

    public void getLocation(){
        lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
        Location location;

        location = getLastKnownLocation();
        if (location == null){
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
        }else{
            longitude = location.getLongitude();
            latitude = location.getLatitude();
            predict_button.setEnabled(true);
            predict_message.setText("Location Service Request Successfully.\nYou can click the button below to predict your room.");
        }
    }

    private void requestPrediction() {
        // Create JSON
        JSONObject json = new JSONObject();
        try {
            JSONArray scans = new JSONArray();
            for (int i = 0; i < mScanList.size(); i++) {
                JSONArray scan = new JSONArray();
                // TODO: Add scan to JSONArray

                // TODO: Add scan to scans
            }
            json.put("longitude", longitude);
            json.put("latitude", latitude);
            json.put("scans", scans);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Construct Request Body
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

        // Construct Post Request
        Request request = new Request.Builder()
                .url(getString(R.string.URL_SCAN))
                .post(body)
                .build();

        // Make async request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d(TAG, "Post prediction call failure");
                Log.d(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Create and parse responseJSON
                final String responseText = response.body().string();
                JSONObject responseJSON = null;
                try {
                    responseJSON = new JSONObject(responseText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject finalResponseJSON = responseJSON;

                // Update UI by JSON response
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResponseJSON != null) {
                            try {
                                if (finalResponseJSON.getBoolean("success")) {
                                    // If server resposne "success"
                                    JSONObject data = finalResponseJSON.getJSONObject("data");
                                    String roomName = data.getString("name");
                                    int floor = data.getInt("floor");
                                    double probability = data.getDouble("probability");
                                    predict_message.setText(String.format("Prediction:\nRoom Name: %s\nFloor: %s\nProbability: %s\n\n",roomName,String.valueOf(floor),String.valueOf(probability)));
                                } else {
                                    // If server response "fail"
                                    JSONArray message = finalResponseJSON.getJSONArray("messages");
                                    Toast.makeText(getActivity(), message.getString(0), Toast.LENGTH_SHORT).show();
                                }
                                Log.d(TAG, responseText);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_prediction_button:
                
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        lm.removeUpdates(this);
        predict_button.setEnabled(true);
        predict_message.setText("Location Service Request Successfully.\nYou can click the button below to predict your room.");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
