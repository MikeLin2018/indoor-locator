package com.locateme.indoor_locator;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BuildingFragment extends Fragment {

    private List<Building> mBuildingList = new ArrayList<>();
    private final String TAG = getClass().getSimpleName();
    private BuildingAdapter buildingAdapter;
    private OkHttpClient client = new OkHttpClient();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_building_list, container, false);

        Activity activity = getActivity();
        RecyclerView buildingRecyclerView = v.findViewById(R.id.building_recycler_view);


        if (activity != null) {
            buildingRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            buildingAdapter = new BuildingAdapter(mBuildingList);
            buildingRecyclerView.setAdapter(buildingAdapter);
            buildingRecyclerView.setItemAnimator(new DefaultItemAnimator());

            // Add Swipe to delete
            ItemTouchHelper touchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(buildingAdapter));
            touchHelper.attachToRecyclerView(buildingRecyclerView);

        }

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
                    actionBar.setSubtitle("Buildings");
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Could not set subtitle");
        }

        // Get Building List from Server
        getBuildingList();
    }


    private void getBuildingList() {
        // Add initial building, for testing purpose
        // mBuildingList.add(new Building("doric", 5.0, 5.0, Building.TrainingStatus.notTrained, Calendar.getInstance().getTime(), "Mike"));


        // build Request to get a list of building
        Request request = new Request.Builder()
                .url(getString(R.string.URL_BUILDING))
                .build();

        // Make async request to update building list
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d(TAG, "getBuildingList call failure");
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
                                    JSONArray data = finalResponseJSON.getJSONArray("data");
                                    mBuildingList.clear();
                                    // Iterate response data and add buildings
                                    for (int i = 0; i < data.length(); i++) {
                                        JSONObject buildingJSON = data.getJSONObject(i);
                                        Date date = new Date(Long.MIN_VALUE);
                                        if (!buildingJSON.getString("training_time").equals("None")) {
                                            Calendar cal = Calendar.getInstance();
                                            SimpleDateFormat sdf_parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            cal.setTime(sdf_parser.parse(buildingJSON.getString("training_time")));
                                        }
                                        Building building = new Building(buildingJSON.getInt("id"), buildingJSON.getString("name"),
                                                Double.valueOf(buildingJSON.getString("longitude")),
                                                Double.valueOf(buildingJSON.getString("latitude")),
                                                Building.TrainingStatus.valueOf(buildingJSON.getString("training_status")),
                                                date,
                                                buildingJSON.getString("username"));
                                        mBuildingList.add(building);
                                        buildingAdapter.notifyDataSetChanged();
                                    }
                                } else {
                                    // If server response "fail"
                                    JSONArray message = finalResponseJSON.getJSONArray("messages");
                                    Toast.makeText(getActivity(), message.getString(0), Toast.LENGTH_SHORT).show();
                                }
                                Log.d(TAG, responseText);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }


                    }
                });

            }
        });
    }

    private class BuildingHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Building building;
        private TextView buildingNameTextView;
        private Button buildingTrainButton;

        BuildingHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_building, parent, false));
            buildingNameTextView = itemView.findViewById(R.id.list_item_building_name);
            buildingTrainButton = itemView.findViewById(R.id.list_item_train_button);
        }

        void bind(Building building) {
            this.building = building;
            buildingNameTextView.setText(this.building.getName());
            buildingNameTextView.setOnClickListener(this);
            buildingTrainButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.list_item_train_button:
                    Intent trainingIntent = new Intent(getActivity(), TrainingActivity.class);
                    trainingIntent.putExtra("name", building.getName()).putExtra("longitude", building.getLongitude()).putExtra("latitude", building.getLatitude()).putExtra("creator", building.getCreator()).putExtra("building_id", building.getID());

                    startActivity(trainingIntent);
                    break;
                case R.id.list_item_building_name:
                    Intent roomIntent = new Intent(getActivity(), RoomActivity.class);
                    roomIntent.putExtra("building_id", building.getID());
                    startActivity(roomIntent);
                    break;
            }
        }
    }

    public class BuildingAdapter extends RecyclerView.Adapter<BuildingHolder> {

        private List<Building> buildingList;

        BuildingAdapter(List<Building> buildingList) {
            this.buildingList = buildingList;
        }

        @Override
        public BuildingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new BuildingHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(BuildingHolder holder, int position) {
            Building building = BuildingFragment.this.mBuildingList.get(position);
            holder.bind(building);
        }

        @Override
        public int getItemCount() {
            return this.buildingList.size();
        }

        public void deleteItem(int position) {
            // Delete building item from recycler view
            String building_id = String.valueOf(mBuildingList.get(position).getID());
            mBuildingList.remove(position);
            buildingAdapter.notifyDataSetChanged();

            // Build URL
            HttpUrl url = HttpUrl.parse(getString(R.string.URL_BUILDING)).newBuilder()
                    .addQueryParameter("email", KeyValueDB.getEmail(getActivity()))
                    .addQueryParameter("building_id", building_id)
                    .addQueryParameter("password", KeyValueDB.getPassword(getActivity()))
                    .build();

            // build Request to get a list of building
            Request request = new Request.Builder()
                    .url(url)
                    .delete()
                    .build();

            // Make async request to update building list
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    call.cancel();
                    Log.d(TAG, "Delete building call failure");
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
                                        JSONArray message = finalResponseJSON.getJSONArray("messages");
                                        Toast.makeText(getActivity(), message.getString(0), Toast.LENGTH_SHORT).show();
                                        getBuildingList();
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

        public Object getContext() {
            return getActivity();
        }
    }

}
