package com.locateme.indoor_locator;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TrainingFragment extends Fragment implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private TextView training_status_text;
    private OkHttpClient client = new OkHttpClient().newBuilder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS).build();
    private int parentBuildingID;
    private Button start_training_button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_building_train, container, false);

        start_training_button = v.findViewById(R.id.fragment_train_button);
        start_training_button.setOnClickListener(this);

        TextView text = v.findViewById(R.id.fragment_train_text);
        String buildingName = getActivity().getIntent().getExtras().getString("name");
        Double buildingLatitude = getActivity().getIntent().getExtras().getDouble("latitude");
        Double buildingLongitude = getActivity().getIntent().getExtras().getDouble("longitude");
        String creator = getActivity().getIntent().getExtras().getString("creator");
        text.setText(String.format("\nBuilding name: %s\nLatitude: %s\nLongitude: %s\nBuilding Creator: %s\n", buildingName, buildingLatitude, buildingLongitude, creator));

        training_status_text = v.findViewById(R.id.fragment_train_text_status);
        parentBuildingID = getActivity().getIntent().getExtras().getInt("building_id");
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setSubtitle("Train your building model");
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Could not set subtitle");
        }

        // build Request to get a list of building
        HttpUrl url = HttpUrl.parse(getString(R.string.URL_TRAIN)).newBuilder().addQueryParameter("building_id", String.valueOf(parentBuildingID)).build();
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Make async request to update building list
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d(TAG, "get train status call failure");
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
                                    Building.TrainingStatus training_status = Building.TrainingStatus.valueOf(data.getString("training_status"));
                                    if (training_status == Building.TrainingStatus.training) {
                                        training_status_text.setText(String.format("Training Status: %s\n\nPlease check back later.\n", "Training"));
                                        start_training_button.setVisibility(View.INVISIBLE);
                                    } else if (training_status == Building.TrainingStatus.notTrained) {
                                        training_status_text.setText(String.format("Training Status: %s\n\nPlease press the button below to train your building model.\n", "Not Trained"));
                                        start_training_button.setVisibility(View.VISIBLE);
                                    } else if (training_status == Building.TrainingStatus.trained) {
                                        String time = data.getString("training_time");
                                        training_status_text.setText(String.format("Training Status: %s\nTrained at: %s\n\nIf you want to re-train the model, please press the button below.\n", "Trained", time));
                                        start_training_button.setVisibility(View.VISIBLE);
                                    }
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_train_button:
                training_status_text.setText(String.format("Training Status: Training\n\nPlease check back later.\n"));
                start_training_button.setVisibility(View.INVISIBLE);
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("building_id", String.valueOf(getActivity().getIntent().getExtras().getInt("building_id")))
                        .build();


                Request request = new Request.Builder()
                        .url(getString(R.string.URL_TRAIN))
                        .post(requestBody)
                        .build();


                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        call.cancel();
                        Log.d(TAG, "Create training model call failure");
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
                                            JSONArray message = finalResponseJSON.getJSONArray("messages");
                                            Toast.makeText(getActivity(), message.getString(0), Toast.LENGTH_SHORT).show();

                                            // Change Training Status
                                            Building.TrainingStatus training_status = Building.TrainingStatus.valueOf(data.getString("training_status"));
                                            if (training_status == Building.TrainingStatus.training) {
                                                training_status_text.setText(String.format("Training Status: %s\nPlease check back later.\n", "Training"));
                                                start_training_button.setVisibility(View.INVISIBLE);
                                            } else if (training_status == Building.TrainingStatus.notTrained) {
                                                training_status_text.setText(String.format("Training Status: %s\n\nPlease press the button below to train your building model.\n", "Not Trained"));
                                                start_training_button.setVisibility(View.VISIBLE);
                                            } else if (training_status == Building.TrainingStatus.trained) {
                                                String time = data.getString("training_time");
                                                training_status_text.setText(String.format("Training Status: %s\nTrained at: %s\n\nIf you want to re-train the model, please press the button below.\n", "Trained", time));
                                                start_training_button.setVisibility(View.VISIBLE);
                                            }
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
                break;
        }
    }
}
