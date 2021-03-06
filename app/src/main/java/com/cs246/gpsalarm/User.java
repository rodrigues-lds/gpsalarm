package com.cs246.gpsalarm;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * USER DATA
 * It provides the user definition.
 *
 * @author Jose Paz, Robert Hampton, Hernan Yupanqui & Eduardo Rodrigues
 * @version 1.1
 * @since 2020-03-10
 * <p>
 * This class provides all the attributes necessary to serialize and deserialize info
 * from Firebase. It serves as an structure for all the main  data that will be
 * manipulated in this application.
 */
public class User {

    // Firebase properties (they must be public for realtime changes)
    public String userID;
    public String username;
    public String name;
    private List<GPSAlarm> GPSAlarm;

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
     * Non-Default Constructor
     *
     * @param name     of the user
     * @param username of the user
     */
    User(String name, String username) {
        this.name = name;
        this.username = username;
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
     * This function returns the list of all GPS Alarm of the user
     *
     * @return a list of GPS Alarm
     */
    public List<GPSAlarm> getGPSAlarm() {
        return this.GPSAlarm;
    }

    /**
     * This function creates the Firebase instance to save and retrieve data.
     */
    private void createFirebaseInstance() {
        try {
            this.mAuth = FirebaseAuth.getInstance();
            this.mFirebaseInstance = FirebaseDatabase.getInstance();
            this.mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers/Users/" + this.mAuth.getCurrentUser().getUid());
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
    public void createDatabase() {
        try {
            mFirebaseDatabase.setValue(this);
            Log.i(TAG, "GPS LOG | The " + this.name + " database was created at Firebase.");
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | The " + this.name + " instance had problems creating the database. " + ex.getMessage());
        }
    }

    /**
     * This function updates the user data.
     */
    public void updateData() {
        try {
            mFirebaseDatabase.setValue(this);
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to update the database. " + ex.getMessage());
        }
    }

    /**
     * This function performs the log out process.
     *
     * @return true if the user is logged out. Otherwise, false.
     */
    public boolean logout() {
        try {
            mAuth.signOut();
            Log.i(TAG, "GPS LOG | The user " + this.username + " is logged out.");
            return true;
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to log out! " + ex.getMessage());
            return false;
        }
    }
}