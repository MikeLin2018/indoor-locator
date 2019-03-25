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
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.net.MediaType;
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
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_room_list, container, false);

        Activity activity = getActivity();
        RecyclerView roomRecyclerView = v.findViewById(R.id.room_recycler_view);

        // TODO: Get parent building id from Intent
        parentBuildingID = 18;


        if (activity != null) {
            roomRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            roomAdapter = new RoomAdapter(mRoomList);
            roomRecyclerView.setAdapter(roomAdapter);
            roomRecyclerView.setItemAnimator(new DefaultItemAnimator());
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
//        // build Request to get a list of rooms
//        JsonObject json = new JsonObject();
//        json.addProperty("building_id", parentBuildingID);
//
//
//        // TODO: Not Yet Finish getRoomList
//        RequestBody requestBody = RequestBody.create(JSON, json);

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
                                    for(int i=0;i<data.length();i++){
                                        JSONObject room = data.getJSONObject(i);
                                        mRoomList.add(new Room(room.getString("name"),room.getInt("floor")));
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

    private class RoomHolder extends RecyclerView.ViewHolder {
        private String roomName;
        private TextView roomNameTextView;

        RoomHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_room, parent, false));
            roomNameTextView = itemView.findViewById(R.id.list_item_room_name);
        }

        void bind(String buildingName) {
            this.roomName = buildingName;
            roomNameTextView.setText(this.roomName);
        }
    }

    private class RoomAdapter extends RecyclerView.Adapter<RoomHolder> {

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
            holder.bind(room.getName());
        }

        @Override
        public int getItemCount() {
            return this.roomList.size();
        }
    }

}
