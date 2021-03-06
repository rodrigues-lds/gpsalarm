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
import java.util.Timer;
import java.util.TimerTask;

/**
 * GEO FENCE BROADCAST | RECEIVER
 * It provides an interface to manage the addresses.
 *
 * @author Hernan Yupanqui
 * @version 1.2
 * @since 2020-03-06
 * <p>
 * This class receives the information of the geofence and is activated when the user enters or exits the area.
 * We define here what the program does when is entering or exiting.
 */
public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private String TAG = "com.cs246.gpsalarm.TAG";

    /**
     * This function is created when the Geo fence is received
     *
     * @param context of the app
     * @param intent  being passed
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            //String errorMessage = GeofenceStatusCodes.getErrorString(geofencingEvent.getErrorCode());
            Log.e(TAG, "Error in the event");
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // This is the list that contains all the geofences that are activated at the same time
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            //Testing purposes
            Log.v(TAG, "" + triggeringGeofences.size());
            Log.v(TAG, "" + triggeringGeofences.get(0).getRequestId());

            // This is the id of the geofence that is activated when we enter the area
            String geofence_id = triggeringGeofences.get(0).getRequestId();

            Log.i(TAG, "Entering or exiting");

        } else {
            Log.e(TAG, "Error again");
        }

        Log.i(TAG, "Receive is working");

        //Here we are going to put all the actions we want to make when the user enters; notifications will be made here also
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // adds link to ringtone and sends toast message indicating arrival
            Toast.makeText(context, "Your are at Desired Location", Toast.LENGTH_LONG).show();
            Uri notification = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM);
            final Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    r.stop();
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, 5000);

        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Toast.makeText(context, "I am exiting the desired area", Toast.LENGTH_SHORT).show();
        }
    }
}
