package com.cs246.gpsalarm;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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

import java.net.BindException;
import java.util.ArrayList;
import java.util.List;

public class AddressActivity extends AppCompatActivity {

    //These variables are from the view part
    EditText user, email, address, radius, latitude_txt, longitude_txt;

    //new changes
    Spinner spinner;
    ImageButton searchButtom;
    JSONArray addressesInJASON;
    List<String> possible_addresses=new ArrayList<String>();


    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth mAuth;
    private String UserId;

    //These variables are used to create the AddressToUse object
    private LatLng the_address;
    private double the_latitude, the_logitude;
    private String description;
    private GPSAlarm gpsAddress;          //The address that will be uploaded to Firebase
    private int desired_radius;
    private String addressPosition;
    long nextGPSAlarmID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();

        // Getting the data inserted in the Main Activity
        this.addressPosition = intent.getStringExtra(ControlPanelActivityOLD.ADDRESS_POSITION);

        address = (EditText) findViewById(R.id.txtAddress);
        radius = (EditText) findViewById(R.id.txtRadius);
        spinner=(Spinner) findViewById(R.id.view_spinner);


        this.mAuth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers/Users/" + mAuth.getCurrentUser().getUid());

        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nextGPSAlarmID = dataSnapshot.child("GPSAlarm").getChildrenCount();
            }



            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This method takes all the data after the user selected one of the possibble directions, and creates the GPSAlarm class.
     * After that it upload the class to firebase. This is activated  when the users clicks on the "Save" button.
     * @param view
     */
    public void saveAddress(View view) {
        //createAddressToUse();             previous to the changes


        String radius_in_string = radius.getText().toString();

        if (radius_in_string.length()<1) {
            Toast.makeText(this, "You must enter the radius",Toast.LENGTH_SHORT).show();
        } else {
            desired_radius = Integer.parseInt(radius_in_string);

            //Creating the new GPSAlarm class with all the information
            gpsAddress = new GPSAlarm(the_latitude, the_logitude, desired_radius, description, null);
            mFirebaseDatabase.child("GPSAlarm").child(Long.toString(nextGPSAlarmID + 1)).setValue(gpsAddress);

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
    private class GetCoordinates extends AsyncTask<String, Void, String> {
        //ProgressDialog dialog = new ProgressDialog(AddressActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(AddressActivity.this, "Looking up the place", Toast.LENGTH_SHORT).show();
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

            try {
                JSONArray jsonArray = new JSONArray(s);
                addressesInJASON=jsonArray;
                possible_addresses.clear();

                for (int i=0;i<10;i++) {
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

            latitude_txt = (EditText) findViewById(R.id.latitude);
            longitude_txt = (EditText) findViewById(R.id.longitude);

            latitude_txt.setText(lat + "");
            longitude_txt.setText(lon + "");

            double latitude = Double.parseDouble(latitude_txt.getText().toString());
            double longitude = Double.parseDouble(longitude_txt.getText().toString());

            the_latitude=latitude;
            the_logitude=longitude;

            Intent i = new Intent();
            i.putExtra("alarm_location_latitude", latitude);
            i.putExtra("alarm_location_longitude", longitude);



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void useMaps(View view) {
        this.finish();
        Intent intent = new Intent(AddressActivity.this, MapsActivity2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
