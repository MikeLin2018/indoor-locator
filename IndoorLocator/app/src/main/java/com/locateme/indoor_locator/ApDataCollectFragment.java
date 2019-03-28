package com.locateme.indoor_locator;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;

public class ApDataCollectFragment extends Fragment implements View.OnClickListener {


    private int roomID;
    private int buildingID;
    private String email;
    private final String TAG = getClass().getSimpleName();
    private OkHttpClient client = new OkHttpClient();
    private Button collectButton;
    private TextView collectMessage;

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS_SCAN = 0;

    WifiManager wifiManager;
    private List<List> mScansList;
    private List<ScanResult> mScanList;
    private int scanCount;
    private int scanCountTarget;
    private int failCount;
    private int failCountMax;


    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_room_collect, container, false);
        collectButton = v.findViewById(R.id.collect_button);
        collectMessage = v.findViewById(R.id.collect_message);
        collectButton.setOnClickListener(this);

        roomID = getActivity().getIntent().getExtras().getInt("room_id");
        buildingID = getActivity().getIntent().getExtras().getInt("building_id");
        email = "lin.2453@osu.edu";

        // Initialize Variables
        scanCount = 0;
        scanCountTarget = 5;
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
                    actionBar.setSubtitle("Collect data for room");
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Could not set subtitle");
        }
    }

    private void submitScanList() {

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
            json.put("building_id", buildingID);
            json.put("room_id", roomID);
            json.put("email", email);
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
                Log.d(TAG, "Post scans call failure");
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
                                    int scanSubmitCount = data.getInt("count");
                                    Toast.makeText(getActivity(), String.format("%s scans have been successfully submitted.", String.valueOf(scanSubmitCount)), Toast.LENGTH_SHORT).show();
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

    public void getScanResults() {
        if (hasLocationPermission()) {
            // Check WIFI is enabled
            if (!wifiManager.isWifiEnabled()) {
                Toast.makeText(getActivity(), "WiFi is disabled, please allow us to turn it on for scanning the access point data.", Toast.LENGTH_LONG).show();
                wifiManager.setWifiEnabled(true);
            }
            scan();
        } else {
            requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS_SCAN);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.collect_button:
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
                getScanResults();
            }
        }
    }


    public void scan() {
        collectMessage.setText(String.format("Scanning: %s/%s", String.valueOf(scanCount + 1), String.valueOf(scanCountTarget)));
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
            collectMessage.setText(String.format("Scanning: %s/%s", String.valueOf(scanCount + 1), String.valueOf(scanCountTarget)));
            boolean success = wifiManager.startScan();
            if (!success) {
                scanFailure();
            }
        } else {
            submitScanList();
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
