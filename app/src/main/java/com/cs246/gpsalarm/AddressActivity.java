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
    private String description;
    private GPSAlarm addressToUse;          //The address that will be uploaded to Firebase
    private int desired_radius;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        //Not sure the purpose of the following (by Hernan)
        //user = (EditText)findViewById(R.id.txtName);
        //email = (EditText)findViewById(R.id.txtUsername);

        address = (EditText) findViewById(R.id.txtAddress);
        radius = (EditText) findViewById(R.id.txtRadius);
        spinner=(Spinner) findViewById(R.id.view_spinner);
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("Users");

        UserId = mFirebaseDatabase.push().getKey();
    }

    public void updateUser(String username, String email) {
        mFirebaseDatabase.child("Users").child(UserId).child("username").setValue(username);
        mFirebaseDatabase.child("Users").child(UserId).child("email").setValue(email);
    }

    public void updateData(View view) {
        updateUser(user.getText().toString().trim(), email.getText().toString().trim());
    }

    public void readData(View view) {
        mFirebaseDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        String dbuser = ds.child("username").getValue(String.class);
                        String dbemail = ds.child("email").getValue(String.class);
                        Log.d("TAG", dbuser + "/" + dbemail);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //For now we are using the switch of the miles_to_kilometers to test the functionality
    public void saveAddress(View view) {
        //createAddressToUse();
        String radius_in_string = radius.getText().toString();
        desired_radius = Integer.parseInt(radius_in_string);
        addressToUse = new GPSAlarm(the_address, desired_radius, description, null);
        mFirebaseDatabase.child(mAuth.getInstance().getUid()).child("Addresses").child("1").setValue(addressToUse);

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

            Toast.makeText(AddressActivity.this,"Looking up the place", Toast.LENGTH_SHORT).show();
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

                for (int i=0;i<jsonArray.length();i++) {
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


    public void lookAllPossibleAddresses(View view) {
        String temp = address.getText().toString().replace(" ", "+");
        new GetCoordinates().execute(temp);


    }

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
            Intent i = new Intent();
            i.putExtra("alarm_location_latitude", latitude);
            i.putExtra("alarm_location_longitude", longitude);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
