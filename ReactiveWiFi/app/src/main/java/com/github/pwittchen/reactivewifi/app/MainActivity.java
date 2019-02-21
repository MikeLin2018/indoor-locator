/*
 * Copyright (C) 2016 Piotr Wittchen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.pwittchen.reactivewifi.app;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.github.pwittchen.reactivewifi.AccessRequester;
import com.github.pwittchen.reactivewifi.ReactiveWifi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;



public class MainActivity extends Activity {
  public static final boolean IS_PRE_M_ANDROID = Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
  private static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1000;
  private static final String TAG = "ReactiveWifi";
  private static final String WIFI_SIGNAL_LEVEL_MESSAGE = "WiFi signal level: ";
  private static final String WIFI_STATE_MESSAGE = "WiFi State: ";
  private TextView tvWifiSignalLevel;
  private TextView tvWifiState;
  private ListView lvAccessPoints;
  private Disposable wifiSubscription;
  private Disposable signalLevelSubscription;
  private Disposable supplicantSubscription;
  private Disposable wifiStateSubscription;
  private Disposable wifiInfoSubscription;
  private WifiManager myWifiManager;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    lvAccessPoints = (ListView) findViewById(R.id.access_points);
    tvWifiSignalLevel = (TextView) findViewById(R.id.wifi_signal_level);
    tvWifiState = (TextView) findViewById(R.id.wifi_state_change);

    //create object for wifi manager
    myWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    boolean wasEnabled = myWifiManager.isWifiEnabled();
    myWifiManager.setWifiEnabled(true);



  }

  @Override protected void onResume() {
    super.onResume();

    if (!isFineOrCoarseLocationPermissionGranted()) {
      requestCoarseLocationPermission();
    } else if (isFineOrCoarseLocationPermissionGranted() || IS_PRE_M_ANDROID) {
      startWifiAccessPointsSubscription();
    }

    startWifiSignalLevelSubscription();
    startSupplicantSubscription();
    startWifiInfoSubscription();
    startWifiStateSubscription();
    getWifiSignalInformation();
  }


  //This is the main method
  private void getWifiSignalInformation(){
    if(myWifiManager.isWifiEnabled()){
      if(myWifiManager.startScan()){
        // List available APs
        List<ScanResult> scans = myWifiManager.getScanResults();
        if(scans != null && !scans.isEmpty()){
          for (ScanResult scan : scans) {
            int level = WifiManager.calculateSignalLevel(scan.level, 20);
            String SSID = scan.SSID;
            String BSSID = scan.BSSID;
            System.out.println(level);
            System.out.println(SSID);
            System.out.println(BSSID);
          }
        }
      }
    }

  }




  // Below methods are not used
  private void startWifiSignalLevelSubscription() {
    signalLevelSubscription = ReactiveWifi.observeWifiSignalLevel(getApplicationContext())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(level -> {
          Log.d(TAG, level.toString());
          final String description = level.description;
          tvWifiSignalLevel.setText(WIFI_SIGNAL_LEVEL_MESSAGE.concat(description));
        });
  }


  private void startWifiAccessPointsSubscription() {

    boolean fineLocationPermissionNotGranted =
        ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED;
    boolean coarseLocationPermissionNotGranted =
        ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED;

    if (fineLocationPermissionNotGranted && coarseLocationPermissionNotGranted) {
      return;
    }

    if (!AccessRequester.isLocationEnabled(this)) {
      AccessRequester.requestLocationAccess(this);
      return;
    }
    /*else{
      AccessRequester.
    }*/

    wifiSubscription = ReactiveWifi.observeWifiAccessPoints(getApplicationContext())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::displayAccessPoints);
  }

  private void startSupplicantSubscription() {
    supplicantSubscription = ReactiveWifi.observeSupplicantState(getApplicationContext())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(supplicantState -> Log.d("ReactiveWifi",
            "New supplicant state: " + supplicantState.toString()));
  }

  private void startWifiInfoSubscription() {
    wifiInfoSubscription = ReactiveWifi.observeWifiAccessPointChanges(getApplicationContext())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(wifiInfo -> Log.d("ReactiveWifi", "New BSSID: " + wifiInfo.getBSSID()));
  }

  private void startWifiStateSubscription() {
    wifiStateSubscription = ReactiveWifi.observeWifiStateChange(getApplicationContext())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(wifiState -> {
          Log.d(TAG, "call: " + wifiState.name());
          tvWifiState.setText(WIFI_STATE_MESSAGE.concat(wifiState.description));
        });
  }

  private void displayAccessPoints(List<ScanResult> scanResults) {
    final List<String> ssids = new ArrayList<>();

    for (ScanResult scanResult : scanResults) {
      ssids.add(scanResult.SSID);
    }

    int itemLayoutId = android.R.layout.simple_list_item_1;
    lvAccessPoints.setAdapter(new ArrayAdapter<>(this, itemLayoutId, ssids));
  }

  @Override protected void onPause() {
    super.onPause();
    safelyUnsubscribe(wifiSubscription, signalLevelSubscription, supplicantSubscription,
        wifiInfoSubscription, wifiStateSubscription);
  }

  private void safelyUnsubscribe(Disposable... subscriptions) {
    for (Disposable subscription : subscriptions) {
      if (subscription != null && !subscription.isDisposed()) {
        subscription.dispose();
      }
    }
  }

  @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    final boolean isCoarseLocation = requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION;
    final boolean permissionGranted = grantResults[0] == PERMISSION_GRANTED;

    if (isCoarseLocation && permissionGranted && wifiSubscription == null) {
      startWifiAccessPointsSubscription();
    }
  }

  private void requestCoarseLocationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      requestPermissions(new String[] {ACCESS_COARSE_LOCATION},
          PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
    }
  }

  private boolean isFineOrCoarseLocationPermissionGranted() {
    boolean isAndroidMOrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    boolean isFineLocationPermissionGranted = isGranted(ACCESS_FINE_LOCATION);
    boolean isCoarseLocationPermissionGranted = isGranted(ACCESS_COARSE_LOCATION);

    return isAndroidMOrHigher && (isFineLocationPermissionGranted
        || isCoarseLocationPermissionGranted);
  }

  private boolean isGranted(String permission) {
    return ActivityCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED;
  }
}
