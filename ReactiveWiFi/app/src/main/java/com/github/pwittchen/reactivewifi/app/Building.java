package com.github.pwittchen.reactivewifi.app;

import java.time.LocalDateTime;

public class Building {

    enum TrainingStatus {
        notTrained, training, trained
    }


    private String name;
    private Double longitude;
    private Double latitude;
    private TrainingStatus trainingStatus;
    private LocalDateTime trainingTime;
    private String creator;

    public Building(String name, Double longitude, Double latitude, TrainingStatus trainingStatus, LocalDateTime trainingTime, String creator) {
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

    public LocalDateTime getTrainingTime() {
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

