package com.cs246.gpsalarm;

import android.media.Ringtone;

import com.google.android.gms.maps.model.LatLng;

/**
 * This class contains all the information related with the address.
 * It is used in as an element of the list that creates the ListView in WorkingAddresses
 * @author Hernan Yupanqui
 */
public class AddressToUse {
    public LatLng coordinates;
    public  int radius;
    public String description;
    public Ringtone ringtone;


    //Constructor
    public AddressToUse(LatLng the_coordinates,
            int the_radius,
            String the_description,
            Ringtone the_ringtone) {

        coordinates=the_coordinates;
        radius=the_radius;
        ringtone=the_ringtone;
        description=the_description;

    }

    public String getDescription() {
        return description;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    public float getRadius() {
        return (float) radius;
    }
}
