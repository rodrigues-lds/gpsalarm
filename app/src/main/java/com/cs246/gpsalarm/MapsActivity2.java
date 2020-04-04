package com.cs246.gpsalarm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class is used when the user wants to add an address using the maps.
 * This activity provides the Edit Texts and tools needed to set the radius and a description of the address.
 * Then, the new address is added to Firebase and the user is returned to Control Panel Activity
 * @author Hernan Yupanqui
 */
public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    // Maps related variables
    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentUser;
    private Marker geofenceMarker;
    private Circle geoFenceLimits;

    // Layout related variables
    private double radius;
    EditText radius_from_layout, description_from_layout;
    ImageButton search_on_maps;



    /**
     * This activity is used when the activity is created, sets all the variables with their values and obtains the location of the user.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        radius_from_layout=(EditText) findViewById(R.id.address_txt_on_maps);
        description_from_layout=(EditText)findViewById(R.id.description_txt_on_maps);
        search_on_maps=(ImageButton) findViewById(R.id.search_button_maps);


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
     * @param googleMap It is an API
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

        if(radius_from_layout.getText().toString().length()<1) {
            Toast.makeText(this, "Enter the radius", Toast.LENGTH_SHORT).show();
        } else {
            markerForGeofence(latLng);
        }
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


            drawGeofence();

        }
    }
    /**
     * This method is for drawing the limits of the geofence in the map, this is called when we make a touch in the map
     * This uses the desired radius to draw the circle and limits of the future geofence.
     */
    private void drawGeofence() {

        radius=Float.parseFloat(radius_from_layout.getText().toString());

        if (geoFenceLimits!=null) {
            geoFenceLimits.remove();
        }

        CircleOptions circleOptions = new CircleOptions()
                .center(geofenceMarker.getPosition())
                .strokeColor(Color.argb(50,70,70,70))
                .fillColor(Color.argb(100,150,150,150))
                .radius(radius*1000);           //Because the radius is given in meters, here we are converting from kilometers to meters

        geoFenceLimits = mMap.addCircle(circleOptions);


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    /**
     * This method takes all the data from the map and creates a new GPSAlarm item.
     * Then it passes the data to the control panel activity.
     * @param view The save button calls this method
     */
    public void saveAddressFromMaps(View view) {

        String the_description = description_from_layout.getText().toString();
        radius=Float.parseFloat(radius_from_layout.getText().toString());


        //Checking if there is a description of the place before saving the address
        if (the_description.length()<1) {
            Toast.makeText(this, "Enter a description of the place", Toast.LENGTH_SHORT).show();
        } else if (geofenceMarker==null) {
            Toast.makeText(this, "Touch the map to add the address", Toast.LENGTH_SHORT).show();
        } else {
            //Creating the GPSAlarm object based on the marker from maps and the radius entered
            GPSAlarm the_gpsalarm = new GPSAlarm(geofenceMarker.getPosition().latitude, geofenceMarker.getPosition().longitude, radius, the_description, null);
            //mFirebaseDatabase.child("GPSAlarm").child(Long.toString(nextGPSAlarmID + 1)).setValue(the_gpsalarm);

            //Finishing this activity and passing to the next activity
            Intent intent = new Intent(MapsActivity2.this, AddressActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Status", "Returning with data from maps");
            intent.putExtra("Latitude", String.valueOf(the_gpsalarm.getLatitude()));
            intent.putExtra("Longitude", String.valueOf(the_gpsalarm.getLongitude()));
            intent.putExtra("Radius", String.valueOf(radius));
            intent.putExtra("Description", the_description);

            startActivity(intent);
            this.finish();
        }

    }

    /**
     * This class is used when we want to use the description of the address and see it in the map with the geofence limits
     * This request information from internet in a different thread.
     */
    public class GetCoordinates extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try {
                String address = strings[0];
                HttpDataHandler http = new HttpDataHandler();
                String url = String.format("https://us1.locationiq.com/v1/search.php?key=6463f683fa0be5&q=%s&format=json", address);
                response = http.getHTTPData(url);
                return response;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (radius_from_layout.length()<1){
                Toast.makeText(MapsActivity2.this, "Enter the radius in km before", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    Double lat_temp = Double.valueOf((String) jsonObject.get("lat"));
                    Double long_temp = Double.valueOf((String) jsonObject.get("lon"));
                    String temp_description = (String) jsonObject.get("display_name");

                    LatLng possible_address_on_maps = new LatLng(lat_temp, long_temp);

                    if (geofenceMarker != null) {
                        geofenceMarker.remove();
                    }

                    geofenceMarker=mMap.addMarker(new MarkerOptions().position(possible_address_on_maps).title("Address"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(possible_address_on_maps));

                    description_from_layout.setText(temp_description);

                    drawGeofence();


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    /**
     * This is activated when the button is clicked
     * It converts the address to latitude and longitude that appears as a marker in the map
     * @param view
     */
    public void goToAddressOnMaps(View view) {
        String temp=description_from_layout.getText().toString();
        new MapsActivity2.GetCoordinates().execute(temp);


    }



}
