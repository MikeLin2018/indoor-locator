
package com.github.pwittchen.reactivewifi.app;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

public class ApDataScan extends AppCompatActivity {
    private EditText tvWifiSignalLevel;
    private WifiManager myWifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_data_scan);

        tvWifiSignalLevel = (EditText) findViewById(R.id.wifi_signal_level_label);

        myWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        myWifiManager.setWifiEnabled(true);
        System.out.print("Hello");
        if (myWifiManager.startScan()) {
            List<ScanResult> scans = myWifiManager.getScanResults();
            if (scans != null && !scans.isEmpty()) {
                for (ScanResult scan : scans) {
                    int level = WifiManager.calculateSignalLevel(scan.level, 20);
                    int lev = scan.level;
                    String SSID = scan.SSID;
                    String BSSID = scan.BSSID;
                    System.out.print(lev);
                    tvWifiSignalLevel.setText(lev);
                }
            }
        }
    }

}