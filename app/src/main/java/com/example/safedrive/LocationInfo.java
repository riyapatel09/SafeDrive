package com.example.safedrive;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationInfo {
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("userID")
    @Expose
    private int userID;

    @SerializedName("vehicleID")
    @Expose
    private String vehicleID;

    @SerializedName("longitude")
    @Expose
    private double longitude;

    @SerializedName("latitude")
    @Expose
    private double latitude;

    @SerializedName("locationTime")
    @Expose
    private String locationTime;
    public LocationInfo()
    {

    }

    public LocationInfo(int userID, String vehicleID, double longitude, double latitude, String locationTime) {
        super();
        this.userID = userID;
        this.vehicleID = vehicleID;
        this.longitude = longitude;
        this.latitude = latitude;
        this.locationTime = locationTime;
    }
    public int getUserID() {
        return userID;
    }
    public void setUserID(int userID) {
        this.userID = userID;
    }
    public String getVehicleID() {
        return vehicleID;
    }
    public void setVehicleID(String vehicleID) {
        this.vehicleID = vehicleID;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public String getLocationTime() {
        return locationTime;
    }
    public void setLocationTime(String locationTime) {
        this.locationTime = locationTime;
    }

    @Override
    public String toString() {
        return "LocationInfo [userID=" + userID + ", vehicleID=" + vehicleID + ", longitude=" + longitude
                + ", latitude=" + latitude + ", locationTime=" + locationTime + "]";
    }
}
