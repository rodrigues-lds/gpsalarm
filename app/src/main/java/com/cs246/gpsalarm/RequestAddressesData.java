package com.cs246.gpsalarm;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Just for testing, not finished
 * @author Hernan Yupanqui
 */
class requestDataFromFirebase extends AsyncTask<String, Void, String> {

    List<GPSAlarm> address_from_dafirebase;
    List<GPSAlarm> list_of_address_fromfirebase;
    Gson gson=new Gson();

    @Override
    protected String doInBackground(String... strings) {

        final DatabaseReference database= FirebaseDatabase.getInstance().getReference().child("DataUsers");
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                address_from_dafirebase= (List<GPSAlarm>) dataSnapshot.getValue();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        String result=gson.toJson(address_from_dafirebase);


        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        list_of_address_fromfirebase= gson.fromJson(s, new TypeToken<List<GPSAlarm>>(){}.getType());




    }

}
