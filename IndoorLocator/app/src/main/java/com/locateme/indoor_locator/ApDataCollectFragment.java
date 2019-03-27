package com.locateme.indoor_locator;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;

public class ApDataCollectFragment extends Fragment implements View.OnClickListener {

    private List<Scan> mScanList;
    private int roomID;
    private int buildingID;
    private String email;
    private final String TAG = getClass().getSimpleName();
    private OkHttpClient client = new OkHttpClient();
    private Button collectButton;
    private TextView collectMessage;


    @Override

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_room_collect, container, false);
        collectButton = v.findViewById(R.id.collect_button);
        collectMessage = v.findViewById(R.id.collect_message);
        collectButton.setOnClickListener(this);

//        roomID = getActivity().getIntent().getExtras().getInt("room_id");
        roomID = 12;
        buildingID = 18;
        email = "lin.2453@osu.edu";

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        // Set actions bar, borrowed from Mr. Champions' TicTacToe android app
        try {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setSubtitle("Collect data for room");
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Could not set subtitle");
        }
    }

    private void submitScanList() {

        // Create JSON
        JSONObject json = new JSONObject();
        try {
            JSONArray scans = new JSONArray();
            for (int i = 0; i < mScanList.size(); i++) {
                JSONArray scan = new JSONArray();
                // TODO: Add scan to JSONArray
            }
            json.put("building_id", buildingID);
            json.put("room_id", roomID);
            json.put("email", email);
            json.put("scans", scans);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Construct Request Body
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());

        // Construct Post Request
        Request request = new Request.Builder()
                .url(getString(R.string.URL_SCAN))
                .post(body)
                .build();

        // Make async request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d(TAG, "Post scans call failure");
                Log.d(TAG, e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Create and parse responseJSON
                final String responseText = response.body().string();
                JSONObject responseJSON = null;
                try {
                    responseJSON = new JSONObject(responseText);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject finalResponseJSON = responseJSON;

                // Update UI by JSON response
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResponseJSON != null) {
                            try {
                                if (finalResponseJSON.getBoolean("success")) {
                                    // If server resposne "success"
                                    JSONObject data = finalResponseJSON.getJSONObject("data");
                                    int scanSubmitCount = data.getInt("count");
                                    Toast.makeText(getActivity(), String.format("%s scans have been successfully submitted.", String.valueOf(scanSubmitCount)), Toast.LENGTH_SHORT).show();
                                } else {
                                    // If server response "fail"
                                    JSONArray message = finalResponseJSON.getJSONArray("messages");
                                    Toast.makeText(getActivity(), message.getString(0), Toast.LENGTH_SHORT).show();
                                }
                                Log.d(TAG, responseText);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            }
        });
    }

    public List<Scan> getScanList() {
        return new ArrayList<>();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.collect_button:
                mScanList = getScanList();
                submitScanList();
                break;
        }
    }
}
