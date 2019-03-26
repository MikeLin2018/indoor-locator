package com.locateme.indoor_locator;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class UserLoginActivity extends SingleFragmentActivity{ @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*@Override
        protected Fragment createFragment () {
            return new UserLoginFragment();
        }*/
    }
}
