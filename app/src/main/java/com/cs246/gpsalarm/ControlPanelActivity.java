package com.cs246.gpsalarm;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.util.ArrayList;
import java.util.List;

/**
 * CONTROL PANEL | ADDRESS CONTROL
 * It provides an interface to manage the addresses
 *
 * @author Jose Paz, Robert Hampton, Hernan Yupanqui & Eduardo Rodrigues
 * @version 1.2
 * @since 2020-03-06
 *
 *  This class is for the Activity that shows all the Addresses of the User that has been added
 *  This should be obtained by requesting the data to Firebase. Here is also checked the permission
 *  for Location and used to determine if we are entering or exiting a Geofence.
 */
public class ControlPanelActivity extends AppCompatActivity {

    // Variables of the view
    public static List<GPSAlarm> gpsAlarmList = new ArrayList<GPSAlarm>();
    public static String example;
    private ListView gpsAlarmListView;

    // Variables of the gps location part (Latitude, Longitude, etc...)
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;

    // Variables for Geo Fencing
    private GeofencingClient geofencingClient;
    private GeofencingRequest geoRequest;
    private PendingIntent geofencePendingIntent;

    // Variables for Firebase Instance
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth mAuth;

    // ********************** TO BE REMOVED ********************** //
    // Testing purposes only.
    public static void activateThisGeofence() {
        Log.v("Ups", example);
        Log.v("Ups", String.valueOf((float) 200));
        Log.v("Ups", String.valueOf(200f));
    }
    // *********************************************************** //

    /**
     * This function is called each time this activity is created.
     *
     * @param savedInstanceState It is a reference to a Bundle object that is passed into the onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlpanel);

        // Get the logged user information from Firebase
        this.mAuth = FirebaseAuth.getInstance();
        this.mFirebaseInstance = FirebaseDatabase.getInstance();
        this.mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers/Users/" + mAuth.getCurrentUser().getUid());

        // Retrieve user data from Firebase. This function must be called each time this activity is created.
        this.mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // Deserialize Firebase user data to User class
                User users = new User();
                users = dataSnapshot.getValue(User.class);

                // Pass the serialized GPS data to a list of GPS Alarm
                if(users.GPSAlarm != null) {
                    gpsAlarmList = users.GPSAlarm;
                    gpsAlarmList.remove(0);     // Firebase brings a null index by default
                    setAdapter();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // ******************** A LOG TAG TO BE IMPLEMENTED HERE ******************** //
            }
        });

        // Associating the element to the list
        gpsAlarmListView = findViewById(R.id.lstGPSAlarm);

        // ********************** TO BE REMOVED ********************** //
        //*********** NOT NECESSARY - THE LIST IS NOW DYNAMICALLY CREATED ********************
        //This is only for testing purposes, actually here should come the real Addresses of the user after requesting the data to Firebase
        //for (int x = 0; x < 5; x++) {
        //    the_list.add(new GPSAlarm(new LatLng(-11.960517, -77.08517), 20000, "Lima. province of Lima, Peru, America #" + (x + 1), null));
        //}
        // *********************************************************** //
        // ********************** TO BE REMOVED ********************** //
        //*********** THIS WAS MOVED TO A METHOD CALLED SET ADAPTER ********************
        //Setting the LIstView of the activity
        // CustomAdapter adapter = new CustomAdapter(this, the_list, this);
        // listView.setAdapter(adapter);
        // *********************************************************** //

        // Setting the location callback and its request
        buildLocationCallback();
        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Managing the permissions for location
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ControlPanelActivity.this);
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ControlPanelActivity.this, "You must enable permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        geofencingClient = LocationServices.getGeofencingClient(this);

        // ********************** TO BE REMOVED ********************** //
        //THis is for future testing, for the moment not useful and this should be erased at the end
        /*
        startGeofence(new LatLng(-11.960517, -77.08517), 20000f);
        Log.v("Ups", "geofence 1");
        */
        // *********************************************************** //

        // Checking the device permissions
        if (fusedLocationProviderClient != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }
        }

        // Using the "client" to ask the request in the locationCallback (like the listener) and a looper that makes it to repeat always.
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    /**
     * This function sets the adapter for the list view element.
     */
    public void setAdapter() {
        //Setting the LIstView of the activity
        CustomAdapter adapter = new CustomAdapter(this, gpsAlarmList, this);
        gpsAlarmListView.setAdapter(adapter);
    }

    /**
     * This method is used to define the LocationCallback, so we put here the actions that we want to perform when we have a response of the location.
     */
    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                // ********************** TO BE REMOVED ********************** //
                //For testing purposes only
                Toast.makeText(ControlPanelActivity.this, "Position: " +
                        locationResult.getLastLocation().getLatitude() + " " +
                        locationResult.getLastLocation().getLongitude(), Toast.LENGTH_LONG).show();
                // *********************************************************** //
            }
        };
    }

    /**
     * This method is for detailing the request of the location services.
     */
    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    /**
     * This method calls other methods to create the Geofence based in the coordinates of the Address and the radius.
     * Then it calls the method add Geofence. With this method we create the Geofences.
     *
     * @param coordinates of the user location
     * @param radius      for the geo fences
     */
    private void startGeofence(LatLng coordinates, Float radius) {
        Geofence geofence = createGeofence(coordinates, radius);
        geoRequest = createGeoRequest(geofence);
        addGeofence(geofence);
    }

    /**
     * This function creates the Geofence with all their properties
     *
     * @param position of the target location
     * @param radius   of the Geofences
     * @return the Geofences
     */
    private Geofence createGeofence(LatLng position, float radius) {
        Log.v("Ups", "geofence 2");
        return new Geofence.Builder()
                .setRequestId("My Geofence")
                .setCircularRegion(position.latitude, position.longitude, radius)
                .setExpirationDuration(60 * 60 * 1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    /**
     * This function creates the triggers of the Geofence that will be required.
     *
     * @param geofence in order to trigger the Geofences
     * @return Geo request
     */
    private GeofencingRequest createGeoRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    /**
     * This function adds the Geofence to the Geofence clients that will manage all the instances.
     *
     * @param geofence instance
     */
    private void addGeofence(final Geofence geofence) {


        geofencingClient.addGeofences(geoRequest, createGeofencingPendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Testing purposes only, to be erased at the end
                        Toast.makeText(ControlPanelActivity.this, "Geofence created " + geofence.getRequestId(), Toast.LENGTH_SHORT).show();
                    }
                })

                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // ******************** A LOG TAG TO BE IMPLEMENTED HERE ******************** //
                    }
                });

        Log.v("Ups", "geofence added");
    }

    /**
     * Creates the pending intent that will call the GeofenceBroadcastReceiver
     *
     * @return the pending Geofences broadcast receiver
     */
    private PendingIntent createGeofencingPendingIntent() {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }

        Intent i = new Intent(this, GeofenceBroadcastReceiver.class);
        return PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * This function creates the menu on the up side corner of this activity.
     *
     * @param menu to be created
     * @return true when it is created
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userinfo, menu);
        return true;
    }

    /**
     * This function is called each time a menu item is selected.
     *
     * @param item selected
     * @return true when action is performed
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemLogout:
                mAuth.signOut();
                openMainActivity();
                return true;
            case R.id.itemAddAddress:
                openAddressActivity();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function opens the Address Activity for the user to created a new address.
     */
    private void openAddressActivity() {
        Intent intent = new Intent(this, AddressActivity.class);
        startActivity(intent);
    }

    /**
     * This function opens the Main Activity (Login). It is called when the user logs out.
     */
    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * This inner class creates the ListView by receiving the array of GPSAlarm list
     * In this class we are also calling the method to create Geofences when the Switch is activated
     * The row.xml is the responsible of the design.
     */
    private class CustomAdapter extends BaseAdapter {

        private Context context;
        private ControlPanelActivity activity;
        private List<GPSAlarm> gpsAlarmList;

        // Non-Default Constructor
        public CustomAdapter(Context c, List<GPSAlarm> gpsAlarmList, ControlPanelActivity act) {
            this.context = c;
            this.gpsAlarmList = gpsAlarmList;
            this.activity = act;
        }

        /**
         * This function gets the number of items in the list.
         *
         * @return the size of the list
         */
        @Override
        public int getCount() {
            return this.gpsAlarmList.size();
        }

        /**
         * This function gets the item based on its position.
         *
         * @param position of the item
         * @return the GPS Alarm item
         */
        @Override
        public Object getItem(int position) {
            return this.gpsAlarmList.get(position);
        }

        /**
         * This function gets the item ID.
         *
         * @param position of the item
         * @return the position
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * This function gets the converted view in order to manipulate the elements on the list.
         *
         * @param position    of the GPS Alarm object
         * @param convertView of the element
         * @param parent      of the view group
         * @return converted view
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
            }

            final GPSAlarm gpsAlarm = (GPSAlarm) getItem(position);

            // Associate the elements to its local objects
            TextView titleView = (TextView) convertView.findViewById(R.id.txtTitle);
            TextView latitudeView = (TextView) convertView.findViewById(R.id.txtLatitude);
            TextView longitudeView = (TextView) convertView.findViewById(R.id.txtLongitude);

            // Populate the elements
            titleView.setText(gpsAlarm.getDescription());
            latitudeView.setText("Lat: " + String.valueOf(gpsAlarm.getLatitude()));
            longitudeView.setText("Long: " + String.valueOf(gpsAlarm.getLongitude()));

            final Switch gpsToggleButton = (Switch) convertView.findViewById(R.id.swtOnOff);
            gpsToggleButton.setOnClickListener(new View.OnClickListener() {
                /**
                 * This function activates the GPS Address Geofence when it the toggle button is activated.
                 * @param view
                 */
                @Override
                public void onClick(View view) {
                    if (gpsToggleButton.isChecked()) {        //When switch is on do this:

                        // Testing purpose
                        Toast.makeText(context, "Switch turned on", Toast.LENGTH_SHORT).show();

                        // // ********************** MUST REPLACE GET COORDINATES BASED ON LAT AND LONG ********************** //
                        // Calling the method to create the geofence of this address
                        activity.startGeofence(new LatLng(gpsAlarm.getLatitude(), gpsAlarm.getLongitude()), gpsAlarm.getRadius());
                        // // ********************** ************************************************** ********************** //

                        // ********************** TO BE REMOVED ********************** //
                        // Testing purposes only
                        activity.example = "Hello world";
                        activity.activateThisGeofence();
                        // *********************************************************** //
                    }
                }
            });
            return convertView;
        }


    }

    @Override
    public void onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

}