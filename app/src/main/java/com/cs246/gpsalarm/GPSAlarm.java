package com.cs246.gpsalarm;

import android.annotation.SuppressLint;

/**
 * Robert Hampton, Hernan Yupanqui & Eduardo Rodrigues
 */
@SuppressLint("Registered")
public class GPSAlarm {

    double latitude;
    double longitude;
    private double radius;
    public String description;
    public String ringtone;
    public boolean wasActivated;
    public int counter;

    public GPSAlarm() {

    }

    //Constructor
    public GPSAlarm(Double latitude, Double longitude,
                    double the_radius,
                    String the_description,
                    String the_ringtone) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = the_radius*1000;
        this.ringtone = the_ringtone;
        this.description = the_description;
        this.wasActivated = false;
        this.counter = 1;
    }

    public String getDescription() {
        return description;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public float getRadius() {
        return (float) radius;
    }

    /*
    This function converts the Radius to Kilometers.
     */
    public static double convertRadiusToKilometers(double miles) {
        return miles * 0.621371;
    }
}
