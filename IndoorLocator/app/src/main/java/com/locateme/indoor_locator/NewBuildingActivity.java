package com.locateme.indoor_locator;

import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class NewBuildingActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new NewBuildingFragment();
    }


}
