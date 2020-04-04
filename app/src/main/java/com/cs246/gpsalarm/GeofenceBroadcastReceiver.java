package com.cs246.gpsalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

/**
 * This class receives the information of the geofence and is activated when the user enters or exits the area.
 * We define here what the program does when is entering or exiting.
 * @author Hernan Yupanqui, Robert Hampton
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private String TAG="GeofenceBroadcastReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent=GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            //String errorMessage = GeofenceStatusCodes.getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, "Error in the event");
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {


            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();


            Log.i(TAG,"Entering or exiting");


        } else {
            Log.e(TAG, "Error again");
        }

        Log.i(TAG,"Receive is working");

        //Here we are going to put all the actions we want to make when the user enters; notifications will be made here also
        if (geofenceTransition==Geofence.GEOFENCE_TRANSITION_ENTER) {

            // adds link to ringtone and sends toast message indicating arrival
            Toast.makeText(context , "Your are at Desired Location",Toast.LENGTH_LONG).show();
            Uri notification = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();

        } else if (geofenceTransition==Geofence.GEOFENCE_TRANSITION_EXIT) {
            Toast.makeText(context,"I am exiting the desired area", Toast.LENGTH_SHORT).show();
        }


    }
}
