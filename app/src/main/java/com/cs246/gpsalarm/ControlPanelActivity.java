package com.cs246.gpsalarm;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
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
import android.widget.ImageButton;
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

import java.util.Collections;
import java.util.List;

/**
 * CONTROL PANEL | ADDRESS CONTROL
 * It provides an interface to manage the addresses.
 *
 * @author Robert Hampton, Hernan Yupanqui & Eduardo Rodrigues
 * @version 1.2
 * @since 2020-03-06
 * <p>
 * This class is for the Activity that shows all the Addresses of the User that has been added
 * This should be obtained by requesting the data to Firebase. Here is also checked the permission
 * for Location and used to determine if we are entering or exiting a Geofence.
 */
public class ControlPanelActivity extends AppCompatActivity {
    // Test commit
    private static final String TAG = "com.cs246.gpsalarm.TAG";
    public static final String ADDRESS_POSITION = "com.cs246.gpsalarm";

    // Variables of the view
    public static List<GPSAlarm> gpsAlarmList;
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

    /**
     * This function is called each time this activity is created.
     *
     * @param savedInstanceState It is a reference to a Bundle object that is passed into the onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlpanel);

        // Firebase instance
        createFirebaseInstance();

        //This checks if the GPS is activated
        checkGPSActivated();

        // Retrieve user addresses from Firebase. This function must be called each time this activity is created.
        this.mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User users = new User();
                try {
                    // Deserialize Firebase user data to User class
                    users = dataSnapshot.getValue(User.class);

                    // Pass the serialized GPS data to a list of GPS Alarm
                    if (users.getGPSAlarm() != null) {
                        gpsAlarmList = users.getGPSAlarm();
                        gpsAlarmList.remove(0);     // Firebase brings a null index by default

                        // Populate the list
                        setAdapter();
                    }
                    Log.i(TAG, "GPS LOG | The user alarm address list was dematerialized from Firebase.");
                } catch (Exception ex) {
                    Log.e(TAG, "GPS LOG | It was not possible deserialize the address list from Firebase. " + ex.getMessage());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ControlPanelActivity.this, "Connection error.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "GPS LOG | It was not possible to read the address list from Firebase.");
            }
        });

        // Associating the element to the list
        this.gpsAlarmListView = findViewById(R.id.lstGPSAlarm);

        // Setting the location callback and its request for the geofence
        buildLocationCallback();
        buildLocationRequest();
        this.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Managing the permissions for location
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        try {
                            buildLocationRequest();
                            buildLocationCallback();
                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ControlPanelActivity.this);
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                            Log.i(TAG, "GPS LOG | GPS Permission is properly set.");
                        } catch (Exception ex) {
                            Log.e(TAG, "GPS LOG | There was a problem managing gps permission on Control Panel. " + ex.getMessage());
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ControlPanelActivity.this, "You must enable GPS permission!", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "GPS LOG | The user had not conceded GPS permission.");
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        Log.e(TAG, "GPS LOG | The permission rational should be shown at Control Panel.");
                    }
                }).check();

        this.geofencingClient = LocationServices.getGeofencingClient(this);

        try {
            // Checking the device permissions
            if (this.fusedLocationProviderClient != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
            }

            // Using the "client" to ask the request in the locationCallback (like the listener) and a looper that makes it to repeat always.
            this.fusedLocationProviderClient.requestLocationUpdates(this.locationRequest, this.locationCallback, Looper.myLooper());

            Log.i(TAG, "GPS LOG | All GPS permissions are conceded.");
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | There is a problem checking the user gps permission. " + ex.getMessage());
        }
    }

    /**
     * This method is executed when the activity is stopped.
     * Here we are saving the information of which geofences are activated.
     */
    @Override
    public void onPause() {

        int idx = 1;
        if (gpsAlarmList != null) {
            try {
                for (GPSAlarm x : gpsAlarmList) {
                    mFirebaseDatabase.child("GPSAlarm").child(Long.toString(idx)).setValue(x);
                    idx++;
                }
                Log.i(TAG, "GPS LOG | Information of Geofences on/off successfully saved on Firebase.");
            } catch (Exception ex) {
                Log.e(TAG, "GPS LOG | Problem saving the Geofences status to Firebase. " + ex.toString());
            }
        }
        super.onPause();
    }

    /**
     * This function is called when this activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        gpsAlarmList.clear();
        super.onDestroy();
    }

    /**
     * This function creates the Firebase instance to retrieve data.
     */
    private void createFirebaseInstance() {
        try {
            // Initializing Firebase
            this.mAuth = FirebaseAuth.getInstance();
            this.mFirebaseInstance = FirebaseDatabase.getInstance();
            this.mFirebaseDatabase = this.mFirebaseInstance.getReference("DataUsers/Users/" + this.mAuth.getCurrentUser().getUid());
            Log.i(TAG, "GPS LOG | The Firebase instance was initiated at Control Panel screen.");
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to create the Firebase instance at Control Panel screen. " + ex.getMessage());
        }
    }

    /**
     * This function sets the adapter for the list view element.
     */
    public void setAdapter() {
        try {
            // Setting the ListView of the activity
            CustomAdapter adapter = new CustomAdapter(this, gpsAlarmList, this);
            this.gpsAlarmListView.setAdapter(adapter);

            Log.i(TAG, "GPS LOG | The listview is set. ");
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to set the list to the activity. " + ex.getMessage());
        }
    }

    /**
     * This method is used to define the LocationCallback, so we put here the actions that we want to perform when we have a response of the location.
     */
    private void buildLocationCallback() {
        this.locationCallback = new LocationCallback() {
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
        try {
            this.locationRequest = new LocationRequest();
            this.locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            this.locationRequest.setInterval(5000);
            this.locationRequest.setFastestInterval(3000);
            this.locationRequest.setSmallestDisplacement(10f);

            Log.i(TAG, "GPS LOG | The detail request for the location service in ok. ");
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to detail the request of the location services. " + ex.getMessage());
        }
    }

    /**
     * This method calls other methods to create the Geofence based in the coordinates of the Address and the radius.
     * Then it calls the method add Geofence. With this method we create the Geofences.
     *
     * @param coordinates of the user location
     * @param radius      for the geo fences
     */
    private void startGeofence(LatLng coordinates, Float radius, int idnumber) {
        try {
            Log.i(TAG, "GPS LOG | Starting Geofence.");
            Geofence geofence = createGeofence(coordinates, radius, idnumber);
            this.geoRequest = createGeoRequest(geofence);
            addGeofence(geofence);
            Log.i(TAG, "GPS LOG | The Geofence is started.");
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to start Geofence. " + ex.getMessage());
        }
    }

    /**
     * This function creates the Geofence with all their properties
     *
     * @param position of the target location
     * @param radius   of the Geofences
     * @return the Geofences
     */
    private Geofence createGeofence(LatLng position, float radius, int IDnumber) {
        try {
            Log.i(TAG, "GPS LOG | Creating Geofence.");
            return new Geofence.Builder()
                    .setRequestId("My Geofence " + IDnumber)
                    .setCircularRegion(position.latitude, position.longitude, radius)
                    .setExpirationDuration(60 * 60 * 1000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to create the Geofence. " + ex.getMessage());
            return null;
        }
    }

    /**
     * This function creates the triggers of the Geofence that will be required.
     *
     * @param geofence in order to trigger the Geofences
     * @return Geo request
     */
    private GeofencingRequest createGeoRequest(Geofence geofence) {
        try {
            Log.i(TAG, "GPS LOG | Requesting Geofence.");
            return new GeofencingRequest.Builder()
                    .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                    .addGeofence(geofence)
                    .build();
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to request the Geofence. " + ex.getMessage());
            return null;
        }
    }

    /**
     * This function checks if the GPS is activated.
     */
    public void checkGPSActivated() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    /**
     * This function build an alert for Message No GPS.
     */
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * This function adds the Geofence to the Geofence clients that will manage all the instances.
     *
     * @param geofence instance
     */
    private void addGeofence(final Geofence geofence) {
        try {
            Log.i(TAG, "GPS LOG | Adding Geofence to be managed.");
            this.geofencingClient.addGeofences(this.geoRequest, createGeofencingPendingIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //Testing purposes only, to be erased at the end
                            Toast.makeText(ControlPanelActivity.this, "Geofence created ", Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "GPS LOG | The Geofence was properly added.");
                        }
                    })

                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.i(TAG, "GPS LOG | The Geofence was not added.");
                        }
                    });
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to add the Geofence to be managed. " + ex.getMessage());
        }
    }

    /**
     * This is used when the users want to remove a geofence or when the user has arrived and want to deactivate the alarm
     *
     * @param idnumber
     */
    private void removeGeofence(final int idnumber) {
        try {
            Log.i(TAG, "GPS LOG | Removing Geofence.");
            this.geofencingClient.removeGeofences(Collections.singletonList("My Geofence " + idnumber))
                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ControlPanelActivity.this, "Geofence " + idnumber + " removed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "GPS LOG | Remove Geofence Failure Listener added.");
                        }
                    });
            Log.i(TAG, "GPS LOG | The Geofence was properly removed.");

        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to remove the Geofence. " + ex.getMessage());
        }
    }

    /**
     * Creates the pending intent that will call the GeofenceBroadcastReceiver
     *
     * @return the pending Geofences broadcast receiver
     */
    private PendingIntent createGeofencingPendingIntent() {
        try {
            if (this.geofencePendingIntent != null) {
                return this.geofencePendingIntent;
            }

            Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
            return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | There was a problem calling the Geofence Broadcast Receiver. " + ex.getMessage());
            return null;
        }
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
                finish();
                if (this.isDestroyed()){
                    gpsAlarmList=null;
                }
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
        public View getView(final int position, View convertView, ViewGroup parent) {

            // Convert the element
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
            }

            final GPSAlarm gpsAlarm = (GPSAlarm) getItem(position);

            // Associate the elements to its local objects
            TextView titleView = (TextView) convertView.findViewById(R.id.txtTitle);
            TextView latitudeView = (TextView) convertView.findViewById(R.id.txtLatitude);
            TextView longitudeView = (TextView) convertView.findViewById(R.id.txtLongitude);
            ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.imgEdit);
            final Switch gpsToggleButton = (Switch) convertView.findViewById(R.id.swtOnOff);

            // Populate the elements
            titleView.setText(gpsAlarm.getDescription());
            latitudeView.setText("Lat: " + String.valueOf(gpsAlarm.getLatitude()));
            longitudeView.setText("Long: " + String.valueOf(gpsAlarm.getLongitude()));

            // To make sure that the switch is in the right position
            if (gpsAlarm.wasActivated == true) {
                gpsToggleButton.setChecked(true);
            } else {
                gpsToggleButton.setChecked(false);
            }

            // When we click in the switch
            try {
                gpsToggleButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        gpsAlarm.counter *= -1;

                        if (gpsAlarm.counter < 0) {           //When switch is on do this:
                            gpsToggleButton.setChecked(true);
                            gpsAlarm.wasActivated = true;
                            startGeofence(new LatLng(gpsAlarm.getLatitude(), gpsAlarm.getLongitude()), gpsAlarm.getRadius(), position);
                        } else {
                            gpsToggleButton.setChecked(false);
                            gpsAlarm.wasActivated = false;
                        }

                        if (gpsAlarm.wasActivated) {        //When switch is on do this:
                            Log.i(TAG, "GPS LOG | Switch turned on. ");
                        }

                        if (!gpsAlarm.wasActivated) {
                            removeGeofence(position);
                        }
                    }
                });
            } catch (Exception ex) {
                Log.e(TAG, "GPS LOG | There was a problem when starting to monitor the Geofence. " + ex.getMessage());
                return null;
            }

            //When the user wants to check the address in the map
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(ControlPanelActivity.this, MapsActivity.class);
                    intent.putExtra("latitude", String.valueOf(gpsAlarm.getLatitude()));
                    intent.putExtra("longitude", String.valueOf(gpsAlarm.getLongitude()));
                    intent.putExtra("radius", String.valueOf(gpsAlarm.getRadius()));
                    startActivity(intent);

                    Toast.makeText(ControlPanelActivity.this, gpsAlarm.getDescription(), Toast.LENGTH_SHORT).show();
                }
            });

            return convertView;
        }
    }
}