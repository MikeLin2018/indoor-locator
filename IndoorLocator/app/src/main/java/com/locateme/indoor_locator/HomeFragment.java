package com.locateme.indoor_locator;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class HomeFragment extends Fragment {
    private TextView useremail;
    private Button settings;
    private Button scan;
    private Button train;
    Intent in;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        Context c = getContext();
        String email = KeyValueDB.getEmail(c);
        int id = KeyValueDB.getUserId(c);
        String name = KeyValueDB.getName(c);
        settings = v.findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                in = new Intent(getActivity(),UserPreferenceActivity.class);
                startActivity(in);
            }
        });
        train = v.findViewById(R.id.train);
        train.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                in = new Intent(getActivity(),TrainingActivity.class);
                startActivity(in);
            }
        });
        scan = v.findViewById(R.id.scan);
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                in = new Intent(getActivity(),ApDataScanActivity.class);
                startActivity(in);
            }
        });
        useremail = (TextView) v.findViewById(R.id.emailTextView);
        useremail.setText("Welcome " + name);

        return v;
    }
}