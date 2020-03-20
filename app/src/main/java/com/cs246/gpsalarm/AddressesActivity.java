package com.cs246.gpsalarm;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/*
This class defines the Addresses Activity
 */
public class AddressesActivity extends AppCompatActivity {

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private String UserId;


    EditText user,email;
    private static final String TAG = "com.cs246.gpsalarm.TAG";
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresses);

        user = (EditText)findViewById(R.id.txtName);
        email = (EditText)findViewById(R.id.txtEmail);

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
        mAuth = FirebaseAuth.getInstance();
        String aaa = mAuth.getUid();
        Log.e(TAG, "GPS LOG | " + aaa.toString());

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
}
