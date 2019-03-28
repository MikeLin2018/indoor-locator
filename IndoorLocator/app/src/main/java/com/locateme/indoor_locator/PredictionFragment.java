package com.locateme.indoor_locator;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

    private TextView predict_text;
    private TextView predict_message;
    private Button predict_button;

    private double longitude;
    private double latitude;

    WifiManager wifiManager;
    private List<List> mScansList;
    private List<ScanResult> mScanList;
    private int scanCount;
    private int scanCountTarget;
    private int failCount;
    private int failCountMax;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS_SCAN = 0;


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

        // Initialize Variables
        scanCount = 0;
        scanCountTarget = 1;
        failCount = 0;
        failCountMax = 10;
        mScansList = new ArrayList<>();
        mScanList = new ArrayList<>();
        // Initialize wifiManager
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);


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
        // Request Location
        getLocation();
    }

    public Location getLastKnownLocation() {

        // Get Last Known GPS Location
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Multiple Attempts to get last known location
        if (location == null) {
            location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null) {
            location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        }
        return location;

    }

    public void getLocation() {
        if (hasLocationPermission()) {
            lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            Location location;

            location = getLastKnownLocation();
            if (location == null) {
                Log.d(TAG, "Requesting Location");
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
            } else {
                Log.d(TAG, "Get Location From Last Known Location.");
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                predict_button.setEnabled(true);
                predict_message.setText("Location Service Request Successfully.\nYou can click the button below to predict your room.");
            }
        } else {
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS_SCAN);
        }
    }

    private void requestPrediction() {
        // Create JSON
        JSONObject json = new JSONObject();
        try {
            JSONArray scans = new JSONArray();
            for (int i = 0; i < mScansList.size(); i++) {
                JSONArray scan = new JSONArray();
                for (Object result : mScansList.get(i)) {
                    ScanResult scanResult = (ScanResult) result;
                    JSONObject ApData = new JSONObject();
                    ApData.put("BSSID", scanResult.BSSID);
                    ApData.put("SSID", scanResult.SSID);
                    ApData.put("quality", scanResult.level);
                    scan.put(ApData);
                }
                scans.put(scan);
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
                .url(getString(R.string.URL_PREDICT))
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
                                    predict_message.setText(String.format("Prediction:\nRoom Name: %s\nFloor: %s\nProbability: %s\n\n", roomName, String.valueOf(floor), String.valueOf(probability)));
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
                getScanResults();
                break;
        }
    }

    private boolean hasLocationPermission() {
        int result = ContextCompat
                .checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS_SCAN) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        predict_button.setEnabled(true);
        predict_message.setText("Location Service Request Successfully.\nYou can click the button below to predict your room.");
        Log.d(TAG, "Get Location From Network Provider.");
        lm.removeUpdates(this);

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

    public void getScanResults() {
        // Check WIFI is enabled
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getActivity(), "WiFi is disabled, please allow us to turn it on for scanning the access point data.", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        scan();
    }


    public void scan() {
        mScanList.clear();
        Toast.makeText(getActivity(), "Scanning WiFi ...", Toast.LENGTH_SHORT).show();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getActivity().registerReceiver(wifiScanReceiver, intentFilter);
        boolean success = wifiManager.startScan();
        if (!success) {
            scanFailure();
        }

    }

    BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent intent) {
            boolean success = intent.getBooleanExtra(
                    WifiManager.EXTRA_RESULTS_UPDATED, false);
            if (success) {
                scanSuccess();
            } else {
                scanFailure();
            }
        }
    };

    private void scanSuccess() {
        mScanList.clear();
        for (ScanResult scanResult : wifiManager.getScanResults()) {
            mScanList.add(scanResult);
        }
        mScansList.add(mScanList);
        scanCount += 1;
        Toast.makeText(getActivity(), String.format("Scan Success: %s/%s", String.valueOf(scanCount), String.valueOf(scanCountTarget)), Toast.LENGTH_SHORT).show();
        if (scanCount < scanCountTarget) {
            boolean success = wifiManager.startScan();
            if (!success) {
                scanFailure();
            }
        } else {
            requestPrediction();
        }
    }

    private void scanFailure() {
        Toast.makeText(getActivity(), "Scan Fail.", Toast.LENGTH_SHORT).show();
        if (failCount < failCountMax) {
            boolean success = wifiManager.startScan();
            if (!success) {
                scanFailure();
            }
        }
    }
}
