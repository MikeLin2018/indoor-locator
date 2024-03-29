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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;

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
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RoomFragment extends Fragment {

    private List<Room> mRoomList = new ArrayList<>();
    private final String TAG = getClass().getSimpleName();
    private RoomAdapter roomAdapter;
    private OkHttpClient client = new OkHttpClient();
    private int parentBuildingID;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_room_list, container, false);

        Activity activity = getActivity();
        RecyclerView roomRecyclerView = v.findViewById(R.id.room_recycler_view);

        parentBuildingID = getActivity().getIntent().getExtras().getInt("building_id");

        if (activity != null) {
            roomRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            roomAdapter = new RoomAdapter(mRoomList);
            roomRecyclerView.setAdapter(roomAdapter);
            roomRecyclerView.setItemAnimator(new DefaultItemAnimator());

            // Add Swipe to delete
            ItemTouchHelper touchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(roomAdapter));
            touchHelper.attachToRecyclerView(roomRecyclerView);
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
                    actionBar.setSubtitle("Rooms");
                }
            }
        } catch (NullPointerException npe) {
            Log.e(TAG, "Could not set subtitle");
        }

        // Get Room List from Server
        getRoomList();
    }

    private void getRoomList() {
        HttpUrl url = HttpUrl.parse(getString(R.string.URL_ROOM)).newBuilder().addQueryParameter("building_id", String.valueOf(parentBuildingID)).build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        // Make async request to update building list
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
                Log.d(TAG, "getRoomList call failure");
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
                                    mRoomList.clear();
                                    for (int i = 0; i < data.length(); i++) {
                                        JSONObject room = data.getJSONObject(i);
                                        mRoomList.add(new Room(room.getInt("room_id"), room.getString("name"), room.getInt("floor")));
                                    }
                                    roomAdapter.notifyDataSetChanged();

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

    private class RoomHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Room room;
        private TextView roomNameTextView;
        private Button roomCollectButton;

        RoomHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_room, parent, false));
            roomNameTextView = itemView.findViewById(R.id.list_item_room_name);
            roomCollectButton = itemView.findViewById(R.id.list_item_room_collect_button);
        }

        void bind(Room room) {
            this.room = room;
            roomNameTextView.setText(room.getName());
            roomCollectButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.list_item_room_collect_button:
                    Intent intent = new Intent(getActivity(), ApDataCollectActivity.class);
                    intent.putExtra("room_id", room.getID());
                    intent.putExtra("building_id", parentBuildingID);
                    startActivity(intent);
                    break;
            }
        }
    }

    public class RoomAdapter extends RecyclerView.Adapter<RoomHolder> {

        private List<Room> roomList;

        RoomAdapter(List<Room> roomList) {
            this.roomList = roomList;
        }

        @Override
        public RoomHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new RoomHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(RoomHolder holder, int position) {
            Room room = RoomFragment.this.mRoomList.get(position);
            holder.bind(room);
        }

        @Override
        public int getItemCount() {
            return this.roomList.size();
        }

        public Object getContext() {
            return getActivity();
        }

        public void deleteItem(int position) {
            // Delete building item from recycler view
            String room_id = String.valueOf(mRoomList.get(position).getID());
            mRoomList.remove(position);
            roomAdapter.notifyDataSetChanged();

            // Build URL
            HttpUrl url = HttpUrl.parse(getString(R.string.URL_ROOM)).newBuilder()
                    .addQueryParameter("email", KeyValueDB.getEmail(getActivity()))
                    .addQueryParameter("room_id", room_id)
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
                    Log.d(TAG, "Delete room call failure");
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
                                        getRoomList();
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

}
