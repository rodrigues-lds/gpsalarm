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

    // The following properties will not be published at Firebase.
    private static final String TAG = "com.cs246.gpsalarm.TAG";
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth mAuth;

    /**
     * Default Constructor
     */
    public User() {
        this.userID = "";
        this.username = "";
        this.name = "";
        createFirebaseInstance();
    }

    /**
     * This function creates the Firebase instance to save and retrieve data.
     */
    private void createFirebaseInstance() {
        try {
            this.mFirebaseInstance = FirebaseDatabase.getInstance();
            this.mFirebaseDatabase = mFirebaseInstance.getReference("Users");
            this.mAuth = FirebaseAuth.getInstance();
            this.userID = mAuth.getCurrentUser().getUid();
            this.username = mAuth.getCurrentUser().getEmail().toString();
            Log.i(TAG, "GPS LOG | The Firebase instance was initiated.");
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to create the Firebase instance. " + ex.getMessage());
        }
    }

    /**
     * This function creates the database for the created user.
     */
    public void createUserDatabase() {
        try {
            mFirebaseDatabase.child(this.userID).setValue(this);
            Log.i(TAG, "GPS LOG | The " + this.name + " database was created at Firebase.");
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | The " + this.name + " instance had problems creating the database. " + ex.getMessage());
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
     * This function updates the user data.
     */
    public void updateUserData() {
        try {
            mFirebaseDatabase.child(this.userID).child("name").setValue(this.name);
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to update the database. " + ex.getMessage());
        }
    }
}