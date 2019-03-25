package com.locateme.indoor_locator;

import java.util.Date;

public class Room {


    private String name;
    private int floor;
//    private String buildingName;
//    private int buildingID;

    public Room(String name, int floor) {
        this.name = name;
        this.floor = floor;
//        this.buildingName = buildingName;
//        this.buildingID = buildingID;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }

        Room obj1 = (Room) obj;

        if (this.name.equals(obj1.name) && this.floor == obj1.floor) {
            return true;
        }

        return false;
    }

    public String getName() {
        return this.name;
    }

    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

}

