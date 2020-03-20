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
 * REGISTER | SING UP
 * It provides an interface and its logic for a user to create an account.
 *
 * @author Jose Paz & Eduardo Rodrigues
 * @version 1.2
 * @since 2020-03-06
 */
public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "com.cs246.gpsalarm.TAG";
    private FirebaseAuth mAuth;
    private EditText editTextUsername, editTextPassword, editTextPasswordConfirmation;
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
        editTextUsername = (EditText) findViewById(R.id.editTextUsername);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        editTextPasswordConfirmation = (EditText) findViewById(R.id.editTextConfirmPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        // Initializing FirebaseAuth
        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to get the instance from Firebase!");
        }

        // Hide keyboard when text boxes looses its focus.
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
    }

    /**
     * This function validates if the required register info are inserted in the register form.
     *
     * @param username             This parameter is the username inserted by the user.
     * @param password             This parameter is the password inserted by the user.
     * @param passwordConfirmation This parameter is the password confirmation inserted by the user.
     * @return if the credentials are valid, it returns true. Otherwise, false.
     */
    private boolean validateUserInformation(String username, String password, String passwordConfirmation) {

        // Check if username is empty
        if (username.isEmpty()) {
            editTextUsername.setError("Email is required.");
            editTextUsername.requestFocus();
            return false;
        }

        // Check if username is valid
        if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            editTextUsername.setError("Please enter a valid email.");
            editTextUsername.requestFocus();
            return false;
        }

        // Check if password is empty
        if (password.isEmpty()) {
            editTextPassword.setError("Password is required.");
            editTextPassword.requestFocus();
            return false;
        }

        // Check if password is valid
        if (password.length() < 6) {
            editTextPassword.setError("Password length should be 6 char at least.");
            editTextPassword.requestFocus();
            return false;
        }

        // Check if confirm password is empty
        if (passwordConfirmation.isEmpty()) {
            editTextPasswordConfirmation.setError("Password confirmation is required.");
            editTextPasswordConfirmation.requestFocus();
            return false;
        }

        // Check if passwords matches
        if (!password.equals(passwordConfirmation)) {
            editTextPasswordConfirmation.setError("Passwords do not match!");
            editTextPasswordConfirmation.requestFocus();
            return false;
        }

        // Credentials are validated
        Log.i(TAG, "GPS LOG | The inserted data are valid in the register form.");
        return true;
    }

    /**
     * This function register the user in the Firebase database.
     */
    private void registerUser(View view) {
        // Extract the data from the Register form
        final String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String passwordConfirmation = editTextPasswordConfirmation.getText().toString().trim();

        // Hide the Keyboard
        hideKeyboard(view);

        //If the credentials are valid, perform the login with database
        if (validateUserInformation(username, password, passwordConfirmation)) {
            progressBar.setVisibility(View.VISIBLE);
            try {
                mAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // This line of code is necessary because the login is done when a user is created
                            mAuth.signOut();
                            Log.i(TAG, "GPS LOG | The user " + username + " was properly registered at Firebase!");
                            openMainActivity();
                        } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Log.w(TAG, "GPS LOG | You are already registered!" + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), "You are already registered!" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        } else {
                            Log.w(TAG, "GPS LOG | " + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (Exception ex) {
                Log.e(TAG, "GPS LOG | It was not possible to create the user. " + ex.getMessage().toString());
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
    }

    /**
     * This function hides the Android Keyboard as soon as the Sing Up button is pressed.
     *
     * @param view The element keyboard.
     */
    private void hideKeyboard(View view) {

        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}