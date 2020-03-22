package com.cs246.gpsalarm;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private Marker geofenceMarker;

    private GeofencingClient geofencingClient;
    private Geofence geofence1;
    private ArrayList<Geofence> mGeofenceList;
    private GeofencingRequest geoRequest;
    private Circle geoFenceLimits;
    private PendingIntent geofencePendingIntent;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);


        //With dexter we manage the permissions of the application, in this case the Location permission
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(MapsActivity2.this);  //getting the client of the LocationServices

                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity2. this);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity2.this, "You must enable permission",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        //Initializing the Geofence client
        geofencingClient = LocationServices.getGeofencingClient(this);





    }
    /**
     * This method is used to define the LocationCallback, so we put here the actions that we want to perform when we have a response of the location
     */
    private void buildLocationCallback() {
        locationCallback=new LocationCallback() {

            //Setting a marker to know our location in the map
            @Override
            public void onLocationResult (LocationResult locationResult) {
                if (mMap!=null) {
                    if (currentUser !=null) currentUser.remove();
                    currentUser=mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(locationResult.getLastLocation().getLatitude(),locationResult.getLastLocation().getLongitude()))
                            .title("You"));

                    //moving camera
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(currentUser.getPosition(),12.07f));
                }
            }
        };
    }

    /**
     * This method is for detailing the request of the location services
     * @autor Hernan Yupanqui
     */
    private void buildLocationRequest() {
        locationRequest=new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }


    /**
     * This methods set the actions to perform when the map is ready.
     * Here we are using a marker for our location and, checking the permissions, setting the click listener to allow create a geofence by touching the map.
     * We also also setting the Location updates based on the request and callback previously created.
     * @param googleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Added recently to imlement the click on marker
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);


        mMap.getUiSettings().setZoomControlsEnabled(true);


        if(fusedLocationProviderClient !=null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

        //Using the "client" to ask the request in the locationCallback(like the listener) and a looper that makes it to repeat always.
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }

    @Override
    public void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }


    /**
     * Used when we make a touch in the map
     * @param latLng
     */
    @Override
    public void onMapClick(LatLng latLng) {
        markerForGeofence(latLng);
    }

    /**
     * Creates a marker in the place touched and creates the geofence in that place. We can only have one geofence at a time
     * @param latLng
     */
    private void markerForGeofence(LatLng latLng) {
        MarkerOptions optionsMarker=new MarkerOptions()
                .position(latLng)
                .title("Geofence Marker");
        if (mMap != null) {
            if (geofenceMarker != null) {

                geofenceMarker.remove();
            }

            geofenceMarker =mMap.addMarker(optionsMarker);


            startGeofence();

        }
    }

    /**
     * This method calls other methods to create the Geofence based in the position of the marker. Then it call the method add geofence.
     */
    private void startGeofence() {
        if (geofenceMarker != null) {

            Geofence geofence=createGeofence(geofenceMarker.getPosition(),10000f);
            geoRequest = createGeoRequest(geofence);
            addGeofence(geofence);


        }
    }


    /**
     * In this method the geofence was created and it just adds the geofence to the application and when it is successfully added when can draw the limits
     * @param geofence
     */
    private void addGeofence(Geofence geofence) {

        geofencingClient.addGeofences(geoRequest, createGeofencingPendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        drawGeofence();

                    }
                })

                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }
    /**
     * Here we creates the pending intent of the Geofence and we pass it to the GeofenceBroadcastReceiver, which will manage if we enter or exit the desired area
     * @return
     */
    private PendingIntent createGeofencingPendingIntent() {
        if (geofencePendingIntent!=null) {
            return geofencePendingIntent;
        }

        Intent i= new Intent(this,GeofenceBroadcastReceiver.class);


        return PendingIntent.getBroadcast(this,0,i, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    /**
     * THis method is for drawing the limits of the geofence in the map, this is called after the geofence was set and added to the geofence Client
     */
    private void drawGeofence() {
        if (geoFenceLimits!=null) {
            geoFenceLimits.remove();
        }

        CircleOptions circleOptions = new CircleOptions()
                .center(geofenceMarker.getPosition())
                .strokeColor(Color.argb(50,70,70,70))
                .fillColor(Color.argb(100,150,150,150))
                .radius(10000f);

        geoFenceLimits = mMap.addCircle(circleOptions);


    }

    /**
     * This function retrieves the Geofencing Request which basically builds the Geofence
     * @param geofence
     * @return GeofencingRequest
     */
    private GeofencingRequest createGeoRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    /**
     * THis method creates the geofence with the triggers that we want to monitor and the name of the Geofence.
     * We are giving the position of the marker in where we want to have the geofence.
     * @param position
     * @param v
     * @return
     */
    private Geofence createGeofence(LatLng position, float v) {
        return new Geofence.Builder()
                .setRequestId("My Geofence")
                .setCircularRegion(position.latitude, position.longitude,v)
                .setExpirationDuration(60*60*1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER| Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
