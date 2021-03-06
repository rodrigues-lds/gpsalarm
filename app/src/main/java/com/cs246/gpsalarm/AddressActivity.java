package com.cs246.gpsalarm;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ADDRESS | DATA ENTRY
 * It provides an interface for creating a new address with parameters.
 *
 * @author Hernan Yupanqui, Eduardo Rodrigues, Jose Paz, & Robert Hampton
 * @version 1.2
 * @since 2020-03-06
 * <p>
 * This class is for the Activity that creates a new address. It passes the information to
 * Firebase and GPSalarm. Has an interface that allows user to choose the address via map or
 * separate line entry, the radius for the geofence, and the ringtone to be played with desired
 * address.
 */
public class AddressActivity extends AppCompatActivity {

    //These variables are from the view part
    EditText address, radius, latitude_txt, longitude_txt;
    Button ringtone;
    Ringtone mRingtone;
    TextView output;
    Switch unit_switch;

    //new changes
    Spinner spinner;
    JSONArray addressesInJASON;
    List<String> possible_addresses=new ArrayList<String>();


    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth mAuth;

    //These variables are used to create the AddressToUse object
    private LatLng the_address;
    private double the_latitude, the_longitude;
    private String description;
    private GPSAlarm gpsAddress;
    private double desired_radius;
    private String addressPosition;
    long nextGPSAlarmID;


    /**
     * This function is called each time this activity is created.
     *
     * @param savedInstanceState It is a reference to a Bundle object that is passed into the onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();

        // Get permission write for changing default ringtone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(this)) {
                // Do stuff here
            } else {
                Intent mIntent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                mIntent.setData(Uri.parse("package:" + this.getPackageName()));
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mIntent);
            }
        }

        // Getting the data inserted in the Main Activity
        this.addressPosition = intent.getStringExtra(ControlPanelActivity.ADDRESS_POSITION);


        // Get data from activity that the user enters
        address = (EditText) findViewById(R.id.txtAddress);
        radius = (EditText) findViewById(R.id.txtRadius);
        spinner=(Spinner) findViewById(R.id.view_spinner);
        ringtone = (Button) findViewById(R.id.ringtone);
        output = (TextView) findViewById(R.id.output);
        unit_switch=(Switch) findViewById(R.id.switchUnitDistance);
        latitude_txt = (EditText) findViewById(R.id.latitude);
        longitude_txt = (EditText) findViewById(R.id.longitude);

        // Create initial Firebase instance and pass user authentication to database
        this.mAuth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers/Users/" + mAuth.getCurrentUser().getUid());

        // Listens for data changes and sends to database
        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nextGPSAlarmID = dataSnapshot.child("GPSAlarm").getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        String the_Status;
        try {
            the_Status = getIntent().getExtras().getString("Status");
        } catch (Exception e) {
            the_Status = null;
        }
        // Sets local data
        if (the_Status!=null) {
            String lat_temp=getIntent().getExtras().getString("Latitude");
            String lng_temp=getIntent().getExtras().getString("Longitude");
            String dscrptn=getIntent().getExtras().getString("Description");
            String local_radius=getIntent().getExtras().getString("Radius");

            address.setText(dscrptn);
            radius.setText(local_radius);
            latitude_txt.setText(lat_temp+"");
            longitude_txt.setText(lng_temp+"");

        }

        // Debug material
        unit_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (unit_switch.isChecked()) {
                    Toast.makeText(AddressActivity.this, "The distance's unit is miles",Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddressActivity.this, "The distance's unit is kilometers",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /**
     * This method creates the ringtone manager activity and allows user to choose a ringtone
     * via the ringtone picker activity and sends info to onActivityResult
     * @param view
     */
    public void setRingtone(View view) {
        //create new intent and uri to save info on phone
        final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        final Uri currentTone= RingtoneManager.getActualDefaultRingtoneUri(AddressActivity.this, RingtoneManager.TYPE_ALARM);

        //add settings to ringtone manager to allow user to pick from alarms on phone
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentTone);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        startActivityForResult(intent, 1);
    }

    /**
     * This method creates the ringtone manager activity and allows user to choose a ringtone
     * @param data sends intent from ringtone picker activity
     * @param requestCode gets requestCode from setRingtone
     * @param resultCode gets resultCode from activity
     */
    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            //create uri from setRingtone and get the selected ringtone
            if (requestCode == 1 && resultCode == RESULT_OK) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);

                //Set selected ringtone here.
                RingtoneManager.setActualDefaultRingtoneUri(
                        this,
                        RingtoneManager.TYPE_ALARM,
                        uri
                );

                // Set uri to new default ringtone to be called by geofence and sent to Firebase
                uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM);
                mRingtone = RingtoneManager.getRingtone(this, uri);

                //change output in address_activity.xml to selected ringtone
                output.setText("Current Ringtone: " + mRingtone.getTitle(this));
            }
            else if (resultCode == RESULT_CANCELED) {
                return;
            }
        }

    /**
     * This method takes all the data after the user selected one of the possible directions, and creates the GPSAlarm class.
     * After that it upload the class to firebase. This is activated  when the users clicks on the "Save" button.
     * @param view
     */
    public void saveAddress(View view) {

        // Get longitude and latitude from activity and convert to string for usage in Firebase
        String lat_temp=latitude_txt.getText().toString();
        String long_temp=longitude_txt.getText().toString();
        description=address.getText().toString();

        String radius_in_string = radius.getText().toString();

        if (radius_in_string.length()<1) {
            Toast.makeText(this, "You must enter the radius",Toast.LENGTH_SHORT).show();
        } else if(latitude_txt.length()<1||longitude_txt.length()<1){
            Toast.makeText(AddressActivity.this, "Enter the latitude and longitude or tap on search button to find it for you.", Toast.LENGTH_SHORT).show();
        } else if (address.getText().toString().length()<1) {
            Toast.makeText(AddressActivity.this, "You must enter a description", Toast.LENGTH_SHORT).show();
        } else {
            the_latitude=Double.valueOf(lat_temp);
            the_longitude=Double.valueOf(long_temp);
            desired_radius = Float.parseFloat(radius_in_string);

            if (unit_switch.isChecked()) {
                desired_radius=GPSAlarm.convertRadiusToMiles(desired_radius);
            }
            if(mRingtone == null){
                gpsAddress = new GPSAlarm(the_latitude, the_longitude, desired_radius, description, null);
                mFirebaseDatabase.child("GPSAlarm").child(Long.toString(nextGPSAlarmID + 1)).setValue(gpsAddress);
            }
            else {
                //Creating the new GPSAlarm class with all the information
                gpsAddress = new GPSAlarm(the_latitude, the_longitude, desired_radius, description, mRingtone.getTitle(AddressActivity.this));
                mFirebaseDatabase.child("GPSAlarm").child(Long.toString(nextGPSAlarmID + 1)).setValue(gpsAddress);
            }

            //Finishing this activity and passing to the Control Panel Activity
            this.finish();
            Intent intent = new Intent(AddressActivity.this, ControlPanelActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /**
     * This class makes an asynchronous activity to request the information of the string given
     * It returns the Latitude, Longitude and Descriptions of the place
     */
    public class GetCoordinates extends AsyncTask<String, Void, String> {
        //ProgressDialog dialog = new ProgressDialog(AddressActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(AddressActivity.this, "Looking up the address", Toast.LENGTH_SHORT).show();
        }

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
            int limit=10;

            try {
                JSONArray jsonArray = new JSONArray(s);
                addressesInJASON=jsonArray;
                possible_addresses.clear();

                if (jsonArray.length()<10) {
                    limit=jsonArray.length();
                }

                for (int i=0;i<limit;i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    description = (String) jsonObject.get("display_name");
                    possible_addresses.add(description);

                }

                createSpinner();
                Toast.makeText(AddressActivity.this, "Select the address from the list", Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * This method sets all the data to the spinner and updates the data each time it changes.
     * When clicked in a item it sets the selected address in all the textViews of the layout to show the user the selected address.
     */
    public void createSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,possible_addresses);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                setTheSelectedAddress(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * It takes the string of the Address Edit Text and pass that to the GetCoordinates class that recieves the information from the Geocoding API
     * This is used each time the user clicks on the search image of the layout.
     * @param view
     */
    public void lookAllPossibleAddresses(View view) {
        String temp = address.getText().toString().replace(" ", "+");
        new GetCoordinates().execute(temp);
    }

    /**
     * This takes the JSONArray that was received after calling the GetCoordinates class, and pass all the information from the item clicked to the layout
     * The information is showed in the Edit Texts.
     * @param index
     */
    public void setTheSelectedAddress(int index) {

        try {
            JSONObject jsonObjectTemp = addressesInJASON.getJSONObject(index);

            String lat = (String) jsonObjectTemp.get("lat").toString();
            String lon = (String) jsonObjectTemp.get("lon").toString();
            the_address = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));   //Creating the coordinates to use from the latitude and longitude
            description = (String) jsonObjectTemp.get("display_name");                        //Description of the place

            //Just for testing purposes:
            String temp_result = "Latitude: " + lat + "Longitude: " + lon + description;
            Log.v("Main", "working---" + temp_result);
            Toast.makeText(AddressActivity.this, temp_result, Toast.LENGTH_SHORT).show();

            latitude_txt.setText(lat + "");
            longitude_txt.setText(lon + "");

            double latitude = Double.parseDouble(latitude_txt.getText().toString());
            double longitude = Double.parseDouble(longitude_txt.getText().toString());

            the_latitude=latitude;
            the_longitude=longitude;

            Intent i = new Intent();
            i.putExtra("alarm_location_latitude", latitude);
            i.putExtra("alarm_location_longitude", longitude);



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is used when the users wants to use the maps to the the address, instead of giving it to the geocoder
     * @param view Called with the button
     */
    public void useMaps(View view) {
        this.finish();
        Intent intent = new Intent(AddressActivity.this, MapsActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
