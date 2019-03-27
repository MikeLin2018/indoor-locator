package com.locateme.indoor_locator;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

public class UserLoginActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new UserLoginFragment();
    }



}
