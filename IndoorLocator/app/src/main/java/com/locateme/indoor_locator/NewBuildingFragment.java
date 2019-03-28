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
import android.widget.Toast;

import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewBuildingFragment extends Fragment implements View.OnClickListener {

    private final String TAG = getClass().getSimpleName();
    private static final int PLACE_SELECTION_REQUEST_CODE = 56789;
    private static final String[] LOCATION_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };
    private static final int REQUEST_LOCATION_PERMISSIONS = 0;
    private EditText longitude;
    private EditText latitude;
    private EditText name;
    private OkHttpClient client = new OkHttpClient();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_building_new, container, false);
        Mapbox.getInstance(getActivity(), getString(R.string.mapbox_token));

        Button go_to_map_picker = v.findViewById(R.id.go_to_place_picker);
        go_to_map_picker.setOnClickListener(this);
        Button new_building_submit = v.findViewById(R.id.new_building_submit);
        new_building_submit.setOnClickListener(this);

        longitude = v.findViewById(R.id.new_building_longitude);
        latitude = v.findViewById(R.id.new_building_latitude);
        name = v.findViewById(R.id.new_building_name);

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
                    actionBar.setSubtitle("Create a new building");
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Could not set subtitle");
        }


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_to_place_picker:
                if (hasLocationPermission()) {
                    LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    double userLongitude = -73.9862;
                    double userLatitude = 40.7544;
                    if (location == null){
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                    if (location != null) {
                        userLongitude = location.getLongitude();
                        userLatitude = location.getLatitude();
                    }
                    Intent intent = new PlacePicker.IntentBuilder()
                            .accessToken(Mapbox.getAccessToken())
                            .placeOptions(
                                    PlacePickerOptions.builder()
                                            .statingCameraPosition(
                                                    new CameraPosition.Builder()
//                                                            .target(new LatLng(40.7544, -73.9862))
                                                            .target(new LatLng(userLatitude, userLongitude))
                                                            .zoom(16)
                                                            .build())
                                            .build())
                            .build(getActivity());
                    startActivityForResult(intent, PLACE_SELECTION_REQUEST_CODE);
                } else {
                    requestPermissions(LOCATION_PERMISSIONS, REQUEST_LOCATION_PERMISSIONS);
                }
                break;

            case R.id.new_building_submit:
                submitNewBuilding();
                break;

        }
    }

    private boolean hasLocationPermission() {
        int result = ContextCompat
                .checkSelfPermission(getActivity(), LOCATION_PERMISSIONS[0]);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSIONS) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                LocationManager lm = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double userLongitude = -73.9862;
                double userLatitude = 40.7544;
                if (location != null) {
                    userLongitude = location.getLongitude();
                    userLatitude = location.getLatitude();
                }
                Intent intent = new PlacePicker.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken())
                        .placeOptions(
                                PlacePickerOptions.builder()
                                        .statingCameraPosition(
                                                new CameraPosition.Builder()
//                                                            .target(new LatLng(40.7544, -73.9862))
                                                        .target(new LatLng(userLatitude, userLongitude))
                                                        .zoom(16)
                                                        .build())
                                        .build())
                        .build(getActivity());
                startActivityForResult(intent, PLACE_SELECTION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == PLACE_SELECTION_REQUEST_CODE) {

            // Retrieve the information from the selected location's CarmenFeature
            CarmenFeature carmenFeature = PlacePicker.getPlace(data);

            //An array in the form [longitude, latitude] at the center of the specified bbox.
            longitude.setText(String.valueOf(carmenFeature.center().coordinates().get(0)));
            latitude.setText(String.valueOf(carmenFeature.center().coordinates().get(1)));
        }
    }

    private void submitNewBuilding() {
        // Add initial building, for testing purpose
        // mBuildingList.add(new Building("doric", 5.0, 5.0, Building.TrainingStatus.notTrained, Calendar.getInstance().getTime(), "Mike"));


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("name", name.getText().toString())
                .addFormDataPart("longitude", longitude.getText().toString())
                .addFormDataPart("latitude", latitude.getText().toString())
                .addFormDataPart("email", "lin.2453@osu.edu") // Hard coded email address.
                .build();


        // build Request to get a list of building
        Request request = new Request.Builder()
                .url(getString(R.string.URL_BUILDING))
                .post(requestBody)
                .build();

        // Make async request to update building list
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d(TAG, "Create new building call failure");
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
                                    String buildingName = data.getString("name");
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
