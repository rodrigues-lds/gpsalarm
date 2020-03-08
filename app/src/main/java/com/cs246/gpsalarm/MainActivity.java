package com.cs246.gpsalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;
import android.widget.Toast;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

/*
This class defines the MainActivity
 */
public class MainActivity extends AppCompatActivity {

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
            Toast.makeText(this, "Insert username and password!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (username.trim().isEmpty()) {
            Toast.makeText(this, "Insert username!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (password.trim().isEmpty()) {
            Toast.makeText(this, "Insert password!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    /*
    [ STUB FUNCTION ] This function validates the user credentials at Firebase.
     */
    private boolean validateLoginFirebase(String username, String password) {
        // TO BE IMPLEMENTED BY JOSE PAZ
        return true;
    }

    /*
    This function retrieve the Login status from the shared preferences.
     */
    private boolean getLoginStatus() {
        // Creating the preferences where the data will be retrieved
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEditor = myPreferences.edit();
        return myPreferences.getBoolean("userLoggedIn", false);
    }

    /*
    This function saves the username as well as the login status into the shared preferences.
     */
    private void setLoginStatus(String username) {
        // Creating the preferences where the data will be saved
        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEditor = myPreferences.edit();

        // Saving the data in the keys strings as defined in the class string.xml
        mEditor.putString(getString(R.string.username), username);
        mEditor.putBoolean("userLoggedIn", true);
        mEditor.commit();
    }

    /*
    This function open the Addresses Activity.
     */
    private void openAddressesActivity() {
        // Creating a new instance of the intent
        Intent intent = new Intent(this, AddressesActivity.class);
        startActivity(intent);
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
        }
    }

    public void changeToMaps(View view) {
        Intent maps=new Intent(this, MapsActivity.class);
        startActivity(maps);
    }
}
