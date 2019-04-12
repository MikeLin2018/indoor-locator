package com.locateme.indoor_locator;

import java.util.Objects;

public class ApData {

    private String BSSID;
    private String SSID;
    private int level;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApData)) return false;
        ApData apData = (ApData) o;
        return getLevel() == apData.getLevel() &&
                Objects.equals(getBSSID(), apData.getBSSID()) &&
                Objects.equals(getSSID(), apData.getSSID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBSSID(), getSSID(), getLevel());
    }
}
