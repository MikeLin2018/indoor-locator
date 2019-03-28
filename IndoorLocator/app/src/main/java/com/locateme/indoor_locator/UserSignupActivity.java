package com.locateme.indoor_locator;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class UserSignupActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new UserSignupFragment();
    }
}