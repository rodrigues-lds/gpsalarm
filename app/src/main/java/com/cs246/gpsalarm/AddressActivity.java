package com.cs246.gpsalarm;

import android.app.ProgressDialog;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AddressActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String UserId;

    //These variables are used to create the AddresToUse object
    private LatLng the_address;
    private String description;
    private AddressToUse addressToUse;
    private int desired_radius;

    //These variables are from the view part
    EditText user,email, address, radius;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        user = (EditText)findViewById(R.id.txtName);
        email = (EditText)findViewById(R.id.txtEmail);
        address = (EditText) findViewById(R.id.txtAddress);
        radius=(EditText) findViewById(R.id.txtRadius);

        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");
        UserId = mFirebaseDatabase.push().getKey();

    }

    public void addUser(String username,String email)
    {
        User users = new User(username,email);
        mFirebaseDatabase.child("Users").child(UserId).setValue(users);
    }

    public void  updateUser(String username,String email)
    {
        mFirebaseDatabase.child("Users").child(UserId).child("username").setValue(username);
        mFirebaseDatabase.child("Users").child(UserId).child("email").setValue(email);
    }

    public void insertData(View view)
    {
        addUser(user.getText().toString().trim(),email.getText().toString().trim());
    }

    public void updateData(View view)
    {

        updateUser(user.getText().toString().trim(),email.getText().toString().trim());
    }

    public void readData(View view)
    {
        mFirebaseDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()){
                    for (DataSnapshot ds: dataSnapshot.getChildren()){

                        String dbuser = ds.child("username").getValue(String.class);
                        String dbemail = ds.child("email").getValue(String.class);
                        Log.d("TAG",dbuser+"/"+dbemail);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /**
     * This class makes an asyncronous activity to request the information of the string given
     * It returns the Latitude, Longitude and Descriptions of the place
     */
    private class GetCoordinates extends AsyncTask<String,Void, String> {
        //ProgressDialog dialog = new ProgressDialog(AddressActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*
            dialog.setMessage("Please wait");
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            */

            Toast.makeText(AddressActivity.this,"Looking the place", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String response;
            try {
                String address = strings[0];
                HttpDataHandler http=new HttpDataHandler();
                String url=String.format("https://us1.locationiq.com/v1/search.php?key=6463f683fa0be5&q=%s&format=json",address);
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
                JSONArray jsonArray=new JSONArray(s);
                //JSONObject jsonObject = new JSONObject(s);
                JSONObject jsonObject=jsonArray.getJSONObject(0);

                String lat = (String) jsonObject.get("lat").toString();
                String lon = (String) jsonObject.get("lon").toString();
                description=(String) jsonObject.get("display_name");    //Description of the place

                the_address=new LatLng(Double.parseDouble(lat), Double.parseDouble(lon));   //Creating the coordinates to use from the latitude and longitude


                String temp_result="Latitude: "+lat+"Longitude: "+lon;

                Log.v("Main", "working---"+ temp_result);
                Toast.makeText(AddressActivity.this, temp_result, Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void saveAddress(View view) {
        createAddressToUse();


    }

    /**
     * This methods creates the new object Address to use that contains all the information of the desired area and preferred settings
     */
    private void createAddressToUse() {
        String temp=address.getText().toString().replace(" ", "+");
        String radius_in_string=radius.getText().toString();
        desired_radius=Integer.parseInt(radius_in_string);     //It has to have a value, if its null it will not works, Be careful!!
        new GetCoordinates().execute(temp);

        addressToUse=new AddressToUse(the_address, desired_radius, description, null);
    }


}
