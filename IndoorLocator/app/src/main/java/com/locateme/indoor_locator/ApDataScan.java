
package com.locateme.indoor_locator;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class ApDataScan  {

    private WifiManager wifiManager;
    ApDataScan(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public void scanWifi(){

    }

    public List<ScanData> collectWifi(int count){

        int i = count;
        wifiManager.setWifiEnabled(true);
        List<ScanData> collectData = new ArrayList<>();
        while(i>0){
            //TimeUnit.SECONDS.sleep(1);
            List<ApData> onescan = new ArrayList<>();
            if (wifiManager.startScan()) {
                List<ScanResult> scans = wifiManager.getScanResults();
                System.out.println(scans.size());
                if (scans != null && !scans.isEmpty()) {
                    for (ScanResult scan : scans) {
                        ApData apData = new ApData(scan.BSSID,scan.SSID,scan.level);
                        onescan.add(apData);
                    }
                }

                if(i==count|| !compareLists(onescan, collectData.get(collectData.size()-1).getScanData())){
                    collectData.add(new ScanData(onescan));
                    i--;

                }
                else{
                    continue;
                }
            }
        }

        return collectData;
    }

     boolean compareLists(List<ApData> prevList, List<ApData> modelList) {

        if (prevList.size() != modelList.size())
            return false;
        else {
            Collections.sort(prevList, new Comparator<ApData>() {
                public int compare(ApData s1, ApData s2) {
                    return s1.getBSSID().compareTo(s2.getBSSID());
                }
            });

            Collections.sort(modelList, new Comparator<ApData>() {
                public int compare(ApData s1, ApData s2) {
                    return s1.getBSSID().compareTo(s2.getBSSID());
                }
            });

            for (int i = 0; i < prevList.size(); ++i) {
                if (!prevList.get(i).equals(modelList.get(i))) {
                    return false;
                }
            }

            return true;

        }



    }


}