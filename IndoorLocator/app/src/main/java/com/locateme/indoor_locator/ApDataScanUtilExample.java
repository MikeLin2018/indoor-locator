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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ApDataScanUtilExample {

    private final String TAG = getClass().getSimpleName();

    WifiManager wifiManager;
    private int scanCount;
    private int scanCountTarget;
    private List<List> scans;
    private List<ScanResult> scan;

    private Activity activity;

    public ApDataScanUtilExample(Activity activity) {
        // Initialize Variables
        this.activity = activity;
        scanCount = 0;
        scans = new ArrayList<>();
        scan = new ArrayList<>();
        // Initialize wifiManager
        wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Check WIFI is enabled
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(activity, "WiFi is disabled, please allow us to turn it on for scanning the access point data.", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
    }

    public void scan(int target) {
        this.scanCountTarget = target;
        scan.clear();
        Toast.makeText(activity, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        activity.registerReceiver(wifiScanReceiver, intentFilter);
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
        scan.clear();
        for (ScanResult scanResult : wifiManager.getScanResults()) {
            scan.add(scanResult);
        }
        scans.add(scan);
        scanCount += 1;
        Log.d(TAG, String.valueOf(scans.size()));
        Toast.makeText(activity, String.format("Scan Success: %s/%s", String.valueOf(scanCount), String.valueOf(scanCountTarget)), Toast.LENGTH_SHORT).show();
        if (scanCount < scanCountTarget) {
            boolean success = wifiManager.startScan();
            if (!success) {
                scanFailure();
            }
        }
    }

    private void scanFailure() {
        Toast.makeText(activity, "Scan Fail.", Toast.LENGTH_SHORT).show();
        boolean success = wifiManager.startScan();
        if (!success) {
            scanFailure();
        }
    }

    public int getScanCount() {
        return scanCount;
    }

    public List<List> getScans() {
        return scans;
    }
}
