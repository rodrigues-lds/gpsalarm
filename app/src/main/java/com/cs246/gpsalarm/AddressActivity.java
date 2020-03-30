package com.cs246.gpsalarm;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class AddressActivity extends AppCompatActivity {

    //These variables are from the view part
    EditText user, email, address, radius, latitude_txt, longitude_txt;
    ;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth mAuth;
    private String UserId;

    //These variables are used to create the AddressToUse object
    private LatLng the_address;
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

    //For now we are using the switch of the miles_to_kilometers to test the functionality
    public void saveAddress(View view) {
        createAddressToUse();
    }

    /**
     * This methods creates the new object Address to use that contains all the information of the desired area and preferred settings
     */
    private void createAddressToUse() {
        String temp = address.getText().toString().replace(" ", "+");
        String radius_in_string = radius.getText().toString();
        desired_radius = Integer.parseInt(radius_in_string);     //It has to have a value, if its null it will not works, Be careful!!
        new GetCoordinates().execute(temp);
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
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                String lat = (String) jsonObject.get("lat").toString();
                String lon = (String) jsonObject.get("lon").toString();
                the_address = new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));   //Creating the coordinates to use from the latitude and longitude
                description = (String) jsonObject.get("display_name");                        //Description of the place

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
                Intent i = new Intent();
                i.putExtra("alarm_location_latitude", latitude);
                i.putExtra("alarm_location_longitude", longitude);

                //The final new object created as result of all the previous code
                gpsAddress = new GPSAlarm(latitude, longitude, desired_radius, description, null);

                //Uploading the new object to firebase
                mFirebaseDatabase.child("GPSAlarm").child(Long.toString(nextGPSAlarmID + 1)).setValue(gpsAddress);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
