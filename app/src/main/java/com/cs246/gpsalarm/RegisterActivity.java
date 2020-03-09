package com.cs246.gpsalarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

/*
This class defines the RegisterActivity
 */
public class RegisterActivity extends AppCompatActivity {

    /*
   This function is called each time this activity is instantiated.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    /*
    This function validates if the required register info are inserted in the register form.
     */
    private boolean validateRegisterForm(String name, String username, String password, String confirmPassword) {

        if (name.trim().isEmpty() && username.trim().isEmpty() && password.trim().isEmpty() && confirmPassword.trim().isEmpty()) {
            Toast.makeText(this, "Fill up all the information!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (name.trim().isEmpty()) {
            Toast.makeText(this, "Insert a name!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (username.trim().isEmpty()) {
            Toast.makeText(this, "Insert a username!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (password.trim().isEmpty()) {
            Toast.makeText(this, "Insert a password!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else if (confirmPassword.trim().isEmpty()) {
            Toast.makeText(this, "Confirm the password!",
                    Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    /*
    This function checks if the password and confirm password info matches.
     */
    private boolean confirmPasswordMatches(String password, String confirmPassword) {
        if (password.equals(confirmPassword)) {
            return true;
        } else {
            Toast.makeText(this, "Passwords do not match!",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /*
    [ STUB FUNCTION ] This function checks if a user with the same username (e-mail) already exist.
     */
    private boolean isUserDuplicated(String username) {
        // TO BE IMPLEMENTED
        return false;
    }

    /*
    [ STUB FUNCTION ] This function register the user into Firebase.
     */
    private boolean registerUserAtFirebase(String name, String username, String password) {
        // TO BE IMPLEMENTED
        boolean userRegistered = true;

        if (userRegistered) {
            Toast.makeText(this, "User Registered! Insert your username and password.",
                    Toast.LENGTH_LONG).show();
            return true;
        } else {
            Toast.makeText(this, "User NOT Registered! Try again later.",
                    Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /*
    This function opens the Main Activity screen
     */
    private void openMainActivity() {
        // Creating a new instance of the intent
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /*
    This function register the user in the Firebase
     */
    public void registerUser(View view) {
        // Extracting the values from the username and password text boxes.
        EditText nameContent = findViewById(R.id.txtName);
        EditText usernameContent = findViewById(R.id.txtUsername);
        EditText passwordContent = findViewById(R.id.txtPassword);
        EditText confirmPasswordContent = findViewById(R.id.txtConfirmPassword);
        String name = nameContent.getText().toString();
        String username = usernameContent.getText().toString();
        String password = passwordContent.getText().toString();
        String confirmPassword = confirmPasswordContent.getText().toString();

        // Validate user registration
        if ((validateRegisterForm(name, username, password, confirmPassword)) &&
                (confirmPasswordMatches(password, confirmPassword)) &&
                (!isUserDuplicated(username)) &&
                (registerUserAtFirebase(name, username, password))) {

            // Open Addresses Activity
            openMainActivity();
        }
    }
}
