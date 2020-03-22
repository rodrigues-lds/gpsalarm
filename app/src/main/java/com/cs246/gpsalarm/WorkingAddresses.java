package com.cs246.gpsalarm;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Telephony;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class WorkingAddresses extends AppCompatActivity {
    //Variables of the view part
    ListView listView;
    List<AddressToUse> the_list = new ArrayList<AddressToUse>();

    //Variables of the location part
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private GeofencingClient geofencingClient;
    private GeofencingRequest geoRequest;
    private PendingIntent geofencePendingIntent;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_working_addresses);

        listView = findViewById(R.id.listView);

        //This is only for testing purposes, actually here should come the real Addresses of the user after requesting the data to Firebase
        for (int x=0; x<5; x++) {
            the_list.add(new AddressToUse(new LatLng(-77,-12), 5, "Mi house"+x, null));
        }

        //Setting the LIstView of the activity
        CustomAdapter adapter = new CustomAdapter(this, the_list);
        listView.setAdapter(adapter);

        //Setting the Location functions

        //Settting te callback
        buildLocationCallback();


        //Setting the Request
        buildLocationRequest();

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

        //fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(WorkingAddresses.this);
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());


                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(WorkingAddresses.this, "You must enable permission",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        geofencingClient = LocationServices.getGeofencingClient(this);

        startGeofence(new LatLng(-11.960517, -77.08517), 20000f);
        Log.v("Ups", "geofence 1");




        if(fusedLocationProviderClient !=null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

        //Using the "client" to ask the request in the locationCallback(like the listener) and a looper that makes it to repeat always.
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Toast.makeText(WorkingAddresses.this, "Position: "+locationResult.getLastLocation().getLatitude() +" "+locationResult.getLastLocation().getLongitude(), Toast.LENGTH_LONG).show();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest=new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void startGeofence(LatLng coordinates, Float radius) {
        Geofence geofence=createGeofence(coordinates,radius);
        geoRequest = createGeoRequest(geofence);
        addGeofence(geofence);
    }

    private Geofence createGeofence(LatLng position, float radius) {
        Log.v("Ups", "geofence 2");
        return new Geofence.Builder()
                .setRequestId("My Geofence")
                .setCircularRegion(position.latitude, position.longitude,radius)
                .setExpirationDuration(60*60*1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER| Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    private GeofencingRequest createGeoRequest(Geofence geofence) {
        Log.v("Ups", "geofence 3");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private void addGeofence(final Geofence geofence) {

        Log.v("Ups", "geofence 3");

        geofencingClient.addGeofences(geoRequest, createGeofencingPendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(WorkingAddresses.this, "Geofence created "+geofence.getRequestId(), Toast.LENGTH_SHORT).show();

                    }
                })

                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    private PendingIntent createGeofencingPendingIntent() {
        if (geofencePendingIntent!=null) {
            return geofencePendingIntent;
        }

        Intent i= new Intent(this, GeofenceBroadcastReceiver.class);


        return PendingIntent.getBroadcast(this,0,i, PendingIntent.FLAG_UPDATE_CURRENT);

    }



}
