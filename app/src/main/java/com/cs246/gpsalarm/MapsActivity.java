package com.cs246.gpsalarm;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

/**
 * MAPS ACTIVITY | 1
 * It provides the edit text and tools for the address.
 *
 * @author Hernan Yupanqui
 * @version 1.2
 * @since 2020-03-06
 * <p>
 * This class is used when the user wants to add an address using the maps.
 * This activity provides the Edit Texts and tools needed to set the radius and a description of the address.
 * Then, the new address is added to Firebase and the user is returned to Control Panel Activity
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // These are maps related variables
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;

    //widgets
    private EditText mSearchText;

    //GPSAlarm items from previous activity
    private Float the_latitude, the_longitude;
    private float the_radius;

    //Object to draw the address and its limits
    private LatLng requestedAddress;
    private Circle geoFenceLimits;

    /**
     * When we create the activity it sets the variables to their values
     * Also, it obtains the information form the previous activity and creates the marker in the map based on that.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Getting the information from the previous activity and convertig to the appropriate object
        String latitude_fromIntent = getIntent().getExtras().getString("latitude");
        the_latitude = Float.valueOf(latitude_fromIntent);

        String longitude_fromIntent = getIntent().getExtras().getString("longitude");
        the_longitude = Float.valueOf(longitude_fromIntent);

        String radius_fromIntent = getIntent().getExtras().getString("radius");
        the_radius = Float.parseFloat(radius_fromIntent);

        //With dexter we manage the permissions of the application, in this case the Location permission
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MapsActivity.this);  //getting the client of the LocationServices

                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity.this);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity.this, "You must enable permission", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    /**
     * This method is used to define the LocationCallback, so we put here the actions that we want to perform when we have a response of the location
     */
    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {

            //Setting a marker to know our location in the map
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mMap != null) {
                    if (currentUser != null) currentUser.remove();
                    currentUser = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude()))
                            .title("You"));
                    currentUser.showInfoWindow();

                    //moving camera
                    mMap.animateCamera(CameraUpdateFactory
                            .newLatLngZoom(currentUser.getPosition(), 12.07f));
                }
            }
        };
    }

    /**
     * This method is for detailing the request of the location services
     */
    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    /**
     * When the map is ready this creates the marker of the actual location and the marker of the address.
     *
     * @param googleMap The map we are using
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Creating the LatLong that will serve as a reference for the marker
        requestedAddress = new LatLng(the_latitude, the_longitude);
        //Adding the marker to the map
        mMap.addMarker(new MarkerOptions().position(requestedAddress).title("Your address"));

        drawGeofence();


        //Zoom settings
        mMap.getUiSettings().setZoomControlsEnabled(true);


        if (fusedLocationProviderClient != null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }


        //Using the "client" to ask the request in the locationCallback(like the listener) and a looper that makes it to repeat always.
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    /**
     * This method is called when this class is destroyed.
     * the location updates are canceled when the app is closed.
     */
    @Override
    protected void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }

    /**
     * This method draws the limits of the geofences that the users has selected
     */
    private void drawGeofence() {

        CircleOptions circleOptions = new CircleOptions()
                .center(requestedAddress)
                .strokeColor(Color.argb(50, 70, 70, 70))
                .fillColor(Color.argb(100, 150, 150, 150))
                .radius(the_radius);

        geoFenceLimits = mMap.addCircle(circleOptions);
    }
}