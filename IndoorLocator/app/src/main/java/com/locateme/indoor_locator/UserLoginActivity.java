package com.locateme.indoor_locator;

import android.support.v4.app.Fragment;

public class UserLoginActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new UserLoginFragment();
    }

}
