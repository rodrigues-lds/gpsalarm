package com.cs246.gpsalarm;

import android.annotation.SuppressLint;
import android.media.Ringtone;

/**
 * Robert Hampton, Hernan Yupanqui & Eduardo Rodrigues
 */
@SuppressLint("Registered")
public class GPSAlarm {

    double latitude;
    double longitude;
    public int radius;
    public String description;
    public Ringtone ringtone;



    public GPSAlarm() {

    }

    //Constructor
    public GPSAlarm(Double latitude, Double longitude,
                    int the_radius,
                    String the_description,
                    Ringtone the_ringtone) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = the_radius;
        this.ringtone = the_ringtone;
        this.description = the_description;

    }

    public String getDescription() {
        return description;
    }

    public double getLatitude()
    {
        return this.latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public float getRadius() {
        return (float) radius;
    }

    /*
    This function converts the Radius to Kilometers.
     */
    public double convertRadiusToKilometers(double miles) {
        return miles * 1.609344;
    }


}
