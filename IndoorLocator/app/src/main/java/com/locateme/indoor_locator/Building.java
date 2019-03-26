package com.locateme.indoor_locator;

import java.time.LocalDateTime;
import java.util.Date;

public class Building {

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    enum TrainingStatus {
        notTrained, training, trained
    }

    private int ID;
    private String name;
    private Double longitude;
    private Double latitude;
    private TrainingStatus trainingStatus;
    private Date trainingTime;
    private String creator;

    public Building(int id, String name, Double longitude, Double latitude, TrainingStatus trainingStatus, Date trainingTime, String creator) {
        this.ID = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.trainingStatus = trainingStatus;
        this.trainingTime = trainingTime;
        this.creator = creator;
    }

    public String getName() {
        return this.name;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public TrainingStatus getTrainingStatus() {
        return this.trainingStatus;
    }

    public Date getTrainingTime() {
        return this.trainingTime;
    }

    public String getCreator() {
        return this.creator;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        Building obj1 = (Building) obj;

        if (this.name.equals(obj1.name) && this.longitude.equals(obj1.longitude) && this.latitude.equals(obj1.latitude) && this.trainingStatus.equals(obj1.trainingStatus)
                && this.trainingTime.equals(obj1.trainingTime) && this.creator.equals(obj1.creator)) {
            return true;
        }

        return false;
    }
}

