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
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

/**
 *  This class is for the Activity that shows all the Addresses of the User that has been added
 *  This should be obtained by requesting the data to Firebase. Here is also checked the permission
 *  for Location and used to determine if we are entering or exiting a geofence
 */
public class ControlPanelActivity extends AppCompatActivity {

    //Variables of the view part
    private ListView listView;
    public static List<GPSAlarm> the_list = new ArrayList<GPSAlarm>();
    public static String example;

    //Variables of the location part
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private  GeofencingClient geofencingClient;
    private  GeofencingRequest geoRequest;
    private  PendingIntent geofencePendingIntent;

    //Testing purposes only.
    public static void activateThisGeofence() {
        Log.v("Ups", example);
        Log.v("Ups", String.valueOf((float) 200));
        Log.v("Ups", String.valueOf(200f));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlpanel);

        listView = findViewById(R.id.listView);

        //This is only for testing purposes, actually here should come the real Addresses of the user after requesting the data to Firebase
        for (int x=0; x<5; x++) {
            the_list.add(new GPSAlarm(new LatLng(-11.960517, -77.08517), 20000, "Lima. province of Lima, Peru, America #"+(x+1), null));
        }

        //Setting the LIstView of the activity
        CustomAdapter adapter = new CustomAdapter(this, the_list, this);
        listView.setAdapter(adapter);

        //Setting te callback
        buildLocationCallback();

        //Setting the Request
        buildLocationRequest();

        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

        //Managing the permissions for location
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        buildLocationRequest();
                        buildLocationCallback();
                        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(ControlPanelActivity.this);
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(ControlPanelActivity.this, "You must enable permission",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        geofencingClient = LocationServices.getGeofencingClient(this);

        //THis is for future testing, for the moment not useful and this should be erased at the end
        /*
        startGeofence(new LatLng(-11.960517, -77.08517), 20000f);
        Log.v("Ups", "geofence 1");
        */

        //Checking the permissions
        if(fusedLocationProviderClient !=null)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

        //Using the "client" to ask the request in the locationCallback(like the listener) and a looper that makes it to repeat always.
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
    }

    /**
     * This method is used to define the LocationCallback, so we put here the actions that we want to perform when we have a response of the location
     */
    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                //For testing purposes only
                Toast.makeText(ControlPanelActivity.this, "Position: "+locationResult.getLastLocation().getLatitude() +" "+locationResult.getLastLocation().getLongitude(), Toast.LENGTH_LONG).show();
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
     * This method calls other methods to create the Geofence based in the coordinates of the Address and the radius.
     * Then it call the method add geofence. With this method we create the geofences.
     * @param coordinates
     * @param radius
     * @autor Hernan Yupanqui
     */
    private void startGeofence(LatLng coordinates, Float radius) {
        Geofence geofence=createGeofence(coordinates,radius);
        geoRequest = createGeoRequest(geofence);
        addGeofence(geofence);
    }

    /**
     * Creates the geofence with all their properties
     * @param position
     * @param radius
     * @return
     */
    private Geofence createGeofence(LatLng position, float radius) {
        Log.v("Ups", "geofence 2");
        return new Geofence.Builder()
                .setRequestId("My Geofence")
                .setCircularRegion(position.latitude, position.longitude,radius)
                .setExpirationDuration(60*60*1000)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER| Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    /**
     * Creates the triggers of the geofence that will be required.
     * @param geofence
     * @return
     */
    private GeofencingRequest createGeoRequest(Geofence geofence) {
        Log.v("Ups", "geofence 3");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    /**
     * Adds the geofence to the geofence clients that will manage all the geofences
     * @param geofence
     */
    private void addGeofence(final Geofence geofence) {

        Log.v("Ups", "geofence 3");

        geofencingClient.addGeofences(geoRequest, createGeofencingPendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        //Testing purposes only, to be erased at the end
                        Toast.makeText(ControlPanelActivity.this, "Geofence created "+geofence.getRequestId(), Toast.LENGTH_SHORT).show();

                    }
                })

                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

        Log.v("Ups","geofence added");

    }

    /**
     * Creates the pending intent that will call the GeofenceBroadcastReceiver
     * @return
     */
    private PendingIntent createGeofencingPendingIntent() {
        if (geofencePendingIntent!=null) {
            return geofencePendingIntent;
        }

        Intent i= new Intent(this, GeofenceBroadcastReceiver.class);


        return PendingIntent.getBroadcast(this,0,i, PendingIntent.FLAG_UPDATE_CURRENT);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userinfo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemLogout:
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

    private void openAddressActivity() {
        Intent intent = new Intent(this, AddressActivity.class);
        startActivity(intent);
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * This inner class creates the ListView by receiving the array of AddressToUse
     * In this class we are also calling the method to create geofences when the Switch is activated
     * The row.xml is the responsible of the design.
     */
    private class CustomAdapter extends BaseAdapter {

        List<GPSAlarm> the_list;
        Context context;
        ControlPanelActivity activity;

        //Constructor
        public CustomAdapter(Context c, List<GPSAlarm> my_list, ControlPanelActivity act) {
            context = c;
            this.the_list = my_list;
            this.activity = act;

        }

        @Override
        public int getCount() {
            return the_list.size();
        }

        @Override
        public Object getItem(int position) {
            return the_list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.row, parent, false);
            }

            final GPSAlarm tempAddress = (GPSAlarm) getItem(position);

            TextView desc = (TextView) convertView.findViewById(R.id.textView1);
            TextView view_latitude = (TextView) convertView.findViewById(R.id.textView2);
            TextView view_longitude= (TextView) convertView.findViewById(R.id.textView3);


            desc.setText(tempAddress.getDescription());
            view_latitude.setText("Lat: "+String.valueOf(tempAddress.getCoordinates().latitude));
            view_longitude.setText("Long: "+String.valueOf(tempAddress.getCoordinates().longitude));


            final Switch swtch = (Switch) convertView.findViewById(R.id.switch2);

            swtch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (swtch.isChecked()) {        //When switch is on do this:

                        //Testing purpose
                        Toast.makeText(context, "Switch turned on", Toast.LENGTH_SHORT).show();

                        //Calling the method to create the geofence of this address
                        activity.startGeofence(tempAddress.getCoordinates(), tempAddress.getRadius());

                        //Testing purposes only
                        activity.example = "Hello world";
                        activity.activateThisGeofence();

                    }
                }
            });


            return convertView;
        }

    }
}
