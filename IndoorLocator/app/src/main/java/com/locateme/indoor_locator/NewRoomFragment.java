package com.locateme.indoor_locator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewRoomFragment extends Fragment implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private EditText floor;
    private EditText name;
    private OkHttpClient client = new OkHttpClient();
    private int buildingID;
    private String email;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_room_new, container, false);

        Button new_room_submit = v.findViewById(R.id.new_room_submit);
        new_room_submit.setOnClickListener(this);

        floor = v.findViewById(R.id.new_room_floor);
        name = v.findViewById(R.id.new_room_name);

        buildingID = getActivity().getIntent().getExtras().getInt("building_id");
        email = KeyValueDB.getEmail(getActivity());

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
                    actionBar.setSubtitle("Create a new room");
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Could not set subtitle");
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_room_submit:
                submitNewRoom();
                break;

        }
    }

    private void submitNewRoom() {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", name.getText().toString())
                .addFormDataPart("floor", floor.getText().toString())
                .addFormDataPart("building_id", String.valueOf(buildingID))
                .addFormDataPart("email", email)
                .build();

        Request request = new Request.Builder()
                .url(getString(R.string.URL_ROOM))
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d(TAG, "Create new room call failure");
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
                                    String roomName = data.getString("name");
                                    JSONArray message = finalResponseJSON.getJSONArray("messages");
                                    Toast.makeText(getActivity(), message.getString(0), Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
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
}
