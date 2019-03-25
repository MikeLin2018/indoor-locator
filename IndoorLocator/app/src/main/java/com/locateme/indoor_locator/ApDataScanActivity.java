package com.locateme.indoor_locator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ApDataScanActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private Button buttoncollect;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    StringBuilder sb;
    List<ScanResult> scanList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ap_data_scan);
        buttonScan = findViewById(R.id.scanBtn);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWifiNetworksList();

            }

        });

        buttoncollect = findViewById(R.id.collectBtn);
        buttoncollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();

            }

        });


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
    }

    public void scanWifi() {
        System.out.println("Test");
        wifiManager.setWifiEnabled(true);

        if (wifiManager.startScan()) {
            List<ScanResult> scans = wifiManager.getScanResults();
              if (scans != null && !scans.isEmpty()) {
                for (ScanResult scan : scans) {
                  int level = WifiManager.calculateSignalLevel(scan.level, 20);
                  String SSID = scan.SSID;
                  String BSSID = scan.BSSID;
                  System.out.println("hello");
                  System.out.println(level);

                }
              }
    }

    }

    private void getWifiNetworksList(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        final WifiManager wifiManager =
                (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                sb = new StringBuilder();
                scanList = wifiManager.getScanResults();
                sb.append("\n  Number Of Wifi connections :" + " "    +scanList.size()+"\n\n");
                for(int i = 0; i < scanList.size(); i++){
                    sb.append(new Integer(i+1).toString() + ". ");
                    sb.append((scanList.get(i)).toString());
                    sb.append("\n\n");
                }
                Toast.makeText(getApplicationContext(),""+sb.toString(),Toast.LENGTH_LONG).show();
                //textView.setText(sb);
                System.out.print(sb);
            }
        },filter);
    }

//    public void collectWifi() {
//        arrayList.clear();
//        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//        wifiManager.startScan();
//        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
//
//    }


    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            for (ScanResult scanResult : results) {
                arrayList.add(scanResult.SSID);
                adapter.notifyDataSetChanged();
            }
        }
    };
}