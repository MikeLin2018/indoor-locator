package com.locateme.indoor_locator;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BuildingFragment extends Fragment {

    private List<Building> buildingList;
    private final String TAG = getClass().getSimpleName();


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_building_list, container, false);

        Activity activity = getActivity();
        RecyclerView buildingRecyclerView = v.findViewById(R.id.building_recycler_view);

        if (activity != null) {
            buildingRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
        }

        return v;
    }

    private List<Building> getBuildingList() {
        List<Building> buildingList = new ArrayList<Building>();

        //TODO: Make Async Query to construct buildingList.

        return buildingList;
    }

    private class BuildingHolder extends RecyclerView.ViewHolder {
        private String buildingName;
        private TextView buildingNameTextView;

        BuildingHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_building, parent, false));

            buildingNameTextView = itemView.findViewById(R.id.list_item_building_name);
        }

        void bind(String buildingName) {
            buildingNameTextView.setText(buildingName);
        }
    }

    private class BuildingAdapter extends RecyclerView.Adapter<BuildingHolder> {

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
            Building building = BuildingFragment.this.buildingList.get(position);
            holder.bind(building.getName());
        }

        @Override
        public int getItemCount() {
            return buildingList.size();
        }
    }

}
