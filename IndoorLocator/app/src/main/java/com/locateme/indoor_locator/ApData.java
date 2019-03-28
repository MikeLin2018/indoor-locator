package com.locateme.indoor_locator;

public class ApData {

    String BSSID;
    String SSID;
    int level;

    public ApData(String BSSID, String SSID, int level) {
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.level = level;
    }

    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
