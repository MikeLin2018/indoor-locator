package com.locateme.indoor_locator;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

public class ApDataTest {

    ApData apData;
    @Before
    public void initialize() {
        apData = new ApData("d8:c7:c8:cc:43:21", "Router21", 75);
    }

    @Test
    public void notNull() {
        assertNotNull(apData);
    }

    @Test
    public void Contents() {
        String BSSID = "d8:c7:c8:cc:43:21";
        int level = 75;
        String SSID = "Router21";


        assertEquals(BSSID, apData.getBSSID());
        assertEquals(SSID, apData.getSSID());
        assertEquals(level, apData.getLevel());


    }

}
