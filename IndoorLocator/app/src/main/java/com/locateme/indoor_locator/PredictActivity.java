package com.locateme.indoor_locator;

import android.support.v4.app.Fragment;

public class PredictActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PredictFragment();
    }


}
