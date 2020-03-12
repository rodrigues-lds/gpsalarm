package com.cs246.gpsalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/*
This class defines the MainActivity
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "com.cs246.gpsalarm.TAG";

    /*
    This function is called each time this main activity is instantiated.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // #################################################
        // GET USER LOGIN STATE IN GET PREFERENCES.
        // IF USER HAS ALREADY PERFORMED THE LOGIN PREVIOUSLY...
        // THE VALUE userLoggedIn WILL BE SET AS TRUE IN THE SHARED PREFERENCES ...
        // THEN CALL THE FUNCTION TO OPEN ADDRESSES ACTIVITY BECAUSE ...
        // LOGIN WILL NO LONGER BE NECESSARY.
        // #################################################
        // if (getLoginStatus() == true) {
        //    openAddressesActivity();
        //}
    }

    /*
    This function validates if the required username and password are inserted in the login form.
     */
    private boolean validateLoginForm(String username, String password) {

        if (username.trim().isEmpty() && password.trim().isEmpty()) {
            Toast.makeText(this, "Insert both username and password!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (username.trim().isEmpty()) {
            Toast.makeText(this, "Insert the username!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (password.trim().isEmpty()) {
            Toast.makeText(this, "Insert the password!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else {
            // Generate Log
            Log.i(TAG, "GPS LOG | The login form was properly validated!");
            return true;
        }
    }

    /*
    [ STUB FUNCTION ] This function validates the user credentials at Firebase.
     */
    private boolean validateLoginFirebase(String username, String password) {
        try {

            // TO BE IMPLEMENTED BY JOSE PAZ
            // Generate Log
            Log.i(TAG, "GPS LOG | The credentials were properly validated.");
            return true;
        } catch (Exception ex) {
            // Generate Log
            Log.w(TAG, "GPS LOG | Something went wrong when validating the Login at Firebase.");
            return false;
        }
    }

    /*
    This function retrieve the Login status from the shared preferences.
     */
    private boolean getLoginStatus() {
        try {
            // Creating the preferences where the data will be retrieved
            SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor mEditor = myPreferences.edit();
            return myPreferences.getBoolean("userLoggedIn", false);
        }catch (Exception ex) {
            // Generate Log
            Log.e(TAG, "GPS LOG | It was not possible to read shared preferences.");
            return false;
        }
    }

    /*
    This function saves the username as well as the login status into the shared preferences.
     */
    private void setLoginStatus(String username) {
        try {
            // Creating the preferences where the data will be saved
            SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor mEditor = myPreferences.edit();

            // Saving the data in the keys strings as defined in the class string.xml
            mEditor.putString(getString(R.string.username), username);
            mEditor.putBoolean("userLoggedIn", true);
            mEditor.commit();

            // Generate Log
            Log.i(TAG, "GPS LOG | Username and login status were saved at shared preferences.");
        } catch (Exception ex) {
            // Generate Log
            Log.e(TAG, "GPS LOG | It was not possible to save shared preferences.");
        }
    }

    /*
    This function opens the Addresses Activity.
     */
    private void openAddressesActivity() {
        // Creating a new instance of the intent
        Intent intent = new Intent(this, AddressesActivity.class);
        startActivity(intent);
    }

    /*
    This function opens the Register Activity
     */
    public void openRegisterActivity(View view) {
        // Creating a new instance of the intent
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /*
    This function opens the Maps Activity.
     */
    public void changeToMaps(View view) {
        Intent maps = new Intent(this, MapsActivity.class);
        startActivity(maps);
    }

    /*
    This function performs the login process.
     */
    public void performLogin(View view) {

        // Extracting the values from the username and password text boxes.
        EditText usernameContent = findViewById(R.id.txtUsername);
        EditText passwordContent = findViewById(R.id.txtPassword);
        String username = usernameContent.getText().toString();
        String password = passwordContent.getText().toString();

        // Validate login conditions
        if ((validateLoginForm(username, password)) && (validateLoginFirebase(username, password))) {
            // Save username in shared preferences
            setLoginStatus(username);

            // Open Addresses Activity
            openAddressesActivity();

            // Generate Log
            Log.i(TAG, "GPS LOG | User credentials are ok.");
        }
    }
}