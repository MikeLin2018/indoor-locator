package com.locateme.indoor_locator;

import android.content.Context;
import android.content.Intent;
import android.gesture.Prediction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class HomeFragment extends Fragment implements View.OnClickListener {
    private TextView useremail;
    private Button scanButton;
    private Button trainButton;
    private Button predictButton;
    private Button settingButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        useremail = (TextView) v.findViewById(R.id.emailTextView);
        scanButton = v.findViewById(R.id.home_scan_btn);
        trainButton = v.findViewById(R.id.home_train_btn);
        predictButton = v.findViewById(R.id.home_predict_btn);
        settingButton = v.findViewById(R.id.home_settings_btn);

        String email = KeyValueDB.getEmail(getContext());
        useremail.setText(email);
        scanButton.setOnClickListener(this);
        trainButton.setOnClickListener(this);
        predictButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);


        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_scan_btn:
                Intent scanIntent = new Intent(getActivity(), ApDataScanActivity.class);
                startActivity(scanIntent);
                break;
            case R.id.home_train_btn:
                Intent trainIntent = new Intent(getActivity(), BuildingActivity.class);
                startActivity(trainIntent);
                break;
            case R.id.home_predict_btn:
                Intent predictIntent = new Intent(getActivity(), PredictionActivity.class);
                startActivity(predictIntent);
                break;
            case R.id.home_settings_btn:
                Toast.makeText(getActivity(), "Settings will come soon.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}