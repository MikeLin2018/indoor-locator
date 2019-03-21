package com.github.pwittchen.reactivewifi.app;

import android.support.v4.app.Fragment;

public class BuildingActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment() {
        return new BuildingFragment();
    }

}
