package com.cs246.gpsalarm;

import android.media.Ringtone;

import com.google.android.gms.maps.model.LatLng;

public class AddressToUse {
    public LatLng coordinates;
    public  int radius;
    public String description;
    public Ringtone ringtone;

    public AddressToUse(LatLng the_coordinates,
            int the_radius,
            String the_description,
            Ringtone the_ringtone) {
        coordinates=the_coordinates;
        radius=the_radius;
        ringtone=the_ringtone;
        description=the_description;

    }
}
