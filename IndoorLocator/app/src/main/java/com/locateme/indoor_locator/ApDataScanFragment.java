package com.locateme.indoor_locator;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ApDataScanFragment extends Fragment implements View.OnClickListener {
    private Button buttonScan;
    private ListView listView;
    private ArrayAdapter resultAdapter;
    private ArrayList<String> scanResultStrList = new ArrayList<>();
    private final String TAG = getClass().getSimpleName();

    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS_SCAN = 0;


    WifiManager wifiManager;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_ap_data_scan, container, false);
        buttonScan = v.findViewById(R.id.scanBtn);
        listView = v.findViewById(R.id.wifiList);
        resultAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, scanResultStrList);

        listView.setAdapter(resultAdapter);
        buttonScan.setOnClickListener(this);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Enable WIFI if not
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getActivity(), "WiFi is disabled, please allow us to turn it on for scanning the access point data.", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        // Set actions bar, borrowed from Mr. Champions' TicTacToe android app
        try {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setSubtitle("Scan");
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Could not set subtitle");
        }
    }


    private void scanWifi() {
        scanResultStrList.clear();
        resultAdapter.notifyDataSetChanged();
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
        scanResultStrList.clear();
        for (ScanResult scanResult : wifiManager.getScanResults()) {
            Log.d(TAG + "-ScanWIFI", scanResult.toString());
            scanResultStrList.add(String.format("BSSID: %s\nSSID: %s\nQuality: %s", scanResult.BSSID.toString(), scanResult.SSID.toString(), String.valueOf(scanResult.level)));
        }
        resultAdapter.notifyDataSetChanged();
        Toast.makeText(getActivity(), "Scanning Complete.", Toast.LENGTH_SHORT).show();
    }

    private void scanFailure() {
        Log.d(TAG + "-ScanWIFI", "Scan Failed");
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
                scanWifi();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scanBtn:
                if (hasLocationPermission()) {
                    scanWifi();
                } else {
                    requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS_SCAN);
                }
                break;
        }

    }

}