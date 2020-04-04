package com.cs246.gpsalarm;

import android.annotation.SuppressLint;

/**
 * GPS ALARM
 * It provides the gps alarm definition.
 *
 * @author Robert Hampton, Hernan Yupanqui & Eduardo Rodrigues
 * @version 1.2
 * @since 2020-03-06
 * <p>
 * This class provides all the attributes necessary to serialize and deserialize gps alarm info
 * from and to Firebase. It serves as an structure for all the main data that will be
 * manipulated for the gsp location / alarm.
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

    /**
     * Default Constructor
     */
    public GPSAlarm() {

    }

    /**
     * @param latitude        of the address
     * @param longitude       of the address
     * @param the_radius      for the geo fence
     * @param the_description of the gps alarm
     * @param the_ringtone    to be played
     */
    public GPSAlarm(Double latitude, Double longitude, double the_radius,
                    String the_description,
                    String the_ringtone) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = the_radius * 1000;
        this.ringtone = the_ringtone;
        this.description = the_description;
        this.wasActivated = false;
        this.counter = 1;
    }

    /**
     * This function gets the description of the gps alarm.
     *
     * @return the gps alarm description
     */
    public String getDescription() {
        return description;
    }

    /**
     * This function gets the latitude of the location.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return this.latitude;
    }

    /**
     * This function gets the longitude of the location.
     *
     * @return the longitude.
     */
    public double getLongitude() {
        return this.longitude;
    }

    /**
     * This function gets the radius for the geo fence.
     *
     * @return the radius
     */
    public float getRadius() {
        return (float) radius;
    }

    /**
     * This function converts the Radius to Miles.
     *
     * @param km of the location
     * @return miles
     */
    public static double convertRadiusToMiles(double km) {
        return km * 0.621371;
    }
}
