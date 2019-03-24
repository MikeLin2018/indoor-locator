package com.locateme.indoor_locator;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BuildingFragment extends Fragment {

    private List<Building> mBuildingList;
    private final String TAG = getClass().getSimpleName();
    private BuildingAdapter buildingAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_building_list, container, false);

        Activity activity = getActivity();
        RecyclerView buildingRecyclerView = v.findViewById(R.id.building_recycler_view);
        mBuildingList = getBuildingList();

        if (activity != null) {
            buildingRecyclerView.setLayoutManager(new LinearLayoutManager(activity));
            buildingAdapter = new BuildingAdapter(mBuildingList);
            buildingRecyclerView.setAdapter(buildingAdapter);
//            buildingRecyclerView.setItemAnimator(new DefaultItemAnimator());
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

//        mBuildingList.add(new Building("caldwell", 5.0, 5.0, Building.TrainingStatus.notTrained, Calendar.getInstance().getTime(), "Mike"));
//        buildingAdapter.notifyDataSetChanged();
        Log.d("mBuildingList",mBuildingList.toString());
    }


    @Override
    public void onActivityCreated(Bundle saveInstanceState) {
        super.onActivityCreated(saveInstanceState);


    }

    private List<Building> getBuildingList() {
        List<Building> buildingList = new ArrayList<Building>();

        //TODO: Make Async Query to construct buildingList.
        buildingList.add(new Building("doric",5.0,5.0,Building.TrainingStatus.notTrained, Calendar.getInstance().getTime(),"Mike"));
        buildingList.add(new Building("test",5.0,5.0,Building.TrainingStatus.notTrained, Calendar.getInstance().getTime(),"Mike"));

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
            this.buildingName = buildingName;
            buildingNameTextView.setText(this.buildingName);
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
            Building building = BuildingFragment.this.mBuildingList.get(position);
            holder.bind(building.getName());
            Log.d("building_id",String.valueOf(position));
            Log.d("building_List_length",String.valueOf(this.buildingList.size()));

        }

        @Override
        public int getItemCount() {
            return this.buildingList.size();
        }
    }

}
