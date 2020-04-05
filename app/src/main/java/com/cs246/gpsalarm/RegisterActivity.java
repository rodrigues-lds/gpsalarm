package com.cs246.gpsalarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

/**
 * REGISTER | SIGN UP
 * It provides an interface and its logic for a user to create an account.
 *
 * @author Jose Paz & Eduardo Rodrigues
 * @version 1.2
 * @since 2020-03-06
 * <p>
 * This class is for the Activity that allows the user to sign up / create a new user.
 * This activity must be visible only if the user is not logged in. Additionally, the Login screen
 * must lead the user to this activity.
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "com.cs246.gpsalarm.TAG";
    private EditText editTextName, editTextUsername, editTextPassword, editTextPasswordConfirmation;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    /**
     * This function is called each time this activity is created.
     *
     * @param savedInstanceState It is a reference to a Bundle object that is passed into the onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initiate the instances
        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPasswordConfirmation = (EditText) findViewById(R.id.editTextConfirmPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        // Initializing Firebase
        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to get the instance from Firebase!");
        }

        // Hide keyboard when text boxes looses its focus.
        editTextName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        editTextUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        editTextPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        editTextPasswordConfirmation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        // These elements are being monitored. If they are clicked, an action is performed
        findViewById(R.id.buttonRegister).setOnClickListener(this);
        findViewById(R.id.textViewLogin).setOnClickListener(this);
    }

    /**
     * This function takes actions when any element is clicked on the screen.
     *
     * @param view is the base class for the activity.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // Sing Up button
            case R.id.buttonRegister:
                registerUser(view);
                break;
            // Login button
            case R.id.textViewLogin:
                startActivity(new Intent(this, MainActivity.class));
                break;
            default:
                break;
        }
        Log.i(TAG,"Inside onClick Method");
    }

    /**
     * This function validates if the required register info are inserted in the register form.
     *
     * @param username             is the username inserted by the user.
     * @param password             is the password inserted by the user.
     * @param passwordConfirmation is the password confirmation inserted by the user.
     * @return if the credentials are valid, it returns true. Otherwise, false.
     */
    public boolean validateUserInformation(String name, String username, String password, String passwordConfirmation) {

        // Check if name is empty
        if (name.isEmpty()) {
            try {
                editTextName.setError("Name is required.");
                editTextName.requestFocus();
            }catch (Exception ex){
                String error = ex.toString();
            }
            return false;
        }

        // Check if username is empty
        if (username.isEmpty()) {
            try {
                editTextUsername.setError("Email is required.");
                editTextUsername.requestFocus();
            }catch (Exception ex){
                String error = ex.toString();
            }
            return false;
        }

        // Check if password is empty
        if (password.isEmpty()) {
            try {
                editTextPassword.setError("Password is required.");
                editTextPassword.requestFocus();
            }catch (Exception ex){
                String error = ex.toString();
            }

            return false;
        }

        // Check if password is valid
        if (password.length() < 6) {
            try {
                editTextPassword.setError("Password length should be 6 char at least.");
                editTextPassword.requestFocus();
            }catch (Exception ex){
                String error = ex.toString();
            }
            return false;
        }

        // Check if confirm password is empty
        if (passwordConfirmation.isEmpty()) {
            try {
                editTextPasswordConfirmation.setError("Password confirmation is required.");
                editTextPasswordConfirmation.requestFocus();
            }catch (Exception ex){
                String error = ex.toString();
            }
            return false;
        }

        // Check if passwords matches
        if (!password.equals(passwordConfirmation)) {
            try {
                editTextPasswordConfirmation.setError("Passwords do not match!");
                editTextPasswordConfirmation.requestFocus();
            }catch (Exception ex){
                String error = ex.toString();
            }
            return false;
        }

        try {
            // Check if username is valid
            if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                try {
                    editTextUsername.setError("Please enter a valid email.");
                    editTextUsername.requestFocus();
                }catch (Exception ex){
                    String error = ex.toString();
                }
                return false;
            }
        }catch (Exception ex){
            String error = ex.toString();
        }

        // Credentials are validated
        try {
            Log.i(TAG, "GPS LOG | The inserted data are valid in the register form.");
        }catch (Exception ex) {
            String error = ex.toString();
        }

        return true;
    }

    /**
     * This function register the user in the Firebase database.
     */
    private void registerUser(View view) {

        hideKeyboard(view);

        // Extract the data from the Register form
        final String name = editTextName.getText().toString().trim();
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String passwordConfirmation = editTextPasswordConfirmation.getText().toString().trim();

        //If the credentials are valid, perform the login with database
        if (validateUserInformation(name, username, password, passwordConfirmation)) {
            progressBar.setVisibility(View.VISIBLE);
            try {
                mAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Create User database
                            User user = new User();
                            user.setName(name);
                            user.createDatabase();

                            // This line of code is necessary because the login is done when a user is created
                            //mAuth.signOut();
                            user.logout();

                            // Ask the user to log in
                            openMainActivity();

                            Log.i(TAG, "GPS LOG | The user " + username + " was properly registered at Firebase!");
                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(getApplicationContext(), "You are already registered! " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.w(TAG, "GPS LOG | You are already registered!" + task.getException().getMessage());
                        } else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.w(TAG, "GPS LOG | " + task.getException().getMessage());
                        }
                    }
                });
            } catch (Exception ex) {
                Log.e(TAG, "GPS LOG | It was not possible to create the user. " + ex.getMessage());
            }
        }

    }

    /**
     * This function opens the next activity once the user is registered.
     */
    private void openMainActivity() {

        // Show a message asking the user to login
        Toast.makeText(getApplicationContext(), "Login with your created user.", Toast.LENGTH_LONG).show();

        // Close the current activity
        this.finish();

        // Creating a new instance of the intent
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        Log.i(TAG, "Inside openMainActivity Method");
    }

    /**
     * This function hides the Android Keyboard as soon as the Sing Up button is pressed.
     *
     * @param view the element keyboard.
     */
    private void hideKeyboard(View view) {

        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        Log.i (TAG, "Inside hideKeyboard Method");
    }
}