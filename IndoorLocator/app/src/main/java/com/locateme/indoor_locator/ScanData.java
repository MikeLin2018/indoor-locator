package com.locateme.indoor_locator;

import java.util.List;

public class ScanData {

    public ScanData(List<ApData> scanData) {
        this.scanData = scanData;
    }

    private List<ApData> scanData;

    public List<ApData> getScanData() {
        return scanData;
    }

    public void setScanData(List<ApData> scanData) {
        this.scanData = scanData;
    }
}
