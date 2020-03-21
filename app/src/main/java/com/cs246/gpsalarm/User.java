package com.cs246.gpsalarm;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * USER DATA
 * It provides the user definition.
 *
 * @author Eduardo Rodrigues
 * @version 1.1
 * @since 2020-03-10
 */
public class User {

    // Firebase properties (they must be public for realtime changes)
    public String userID;
    public String username;
    public String name;

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth mAuth;
    private static final String TAG = "com.cs246.gpsalarm.TAG";

    /**
     * Default Constructor
     */
    public User() {
        createFirebaseInstance();
    }

    /**
     * This function creates the Firebase instance to save and retrieve data.
     */
    private void createFirebaseInstance(){
        try{
            this.mFirebaseInstance = FirebaseDatabase.getInstance();
            this.mFirebaseDatabase = mFirebaseInstance.getReference("Users");
            this.mAuth = FirebaseAuth.getInstance();
            this.userID = mAuth.getCurrentUser().getUid();
            this.username = mAuth.getCurrentUser().getEmail().toString();
        } catch (Exception ex){
            Log.e(TAG, "GPS LOG | It was not possible to create the Firebase instance. " + ex.getMessage());
        }
    }

    /**
     * This function sets the name of the user.
     *
     * @param name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * This function creates the database for the created user.
     */
    public void createUserDatabase() {
        try {
            mFirebaseDatabase.child(this.userID).setValue(this);
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | The user instance had problems creating the database. " + ex.getMessage());
        }
    }

    /**
     * This function updates the user data.
     */
    public void updateUserData() {
        mFirebaseDatabase.child(this.userID).child("name").setValue(this.name);
    }
}

