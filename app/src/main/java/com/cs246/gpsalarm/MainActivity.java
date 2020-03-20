package com.cs246.gpsalarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * USER LOGIN
 * It provides a login interface and its logic for a user to perform tje login.
 *
 * @author Jose Paz & Eduardo Rodrigues
 * @version 1.1
 * @since 2020-03-04
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "com.cs246.gpsalarm.LOG";
    private FirebaseAuth mAuth;
    private EditText editTextUsername, editTextPassword;
    private ProgressBar progressBar;

    /**
     * This function is called each time this activity is created.
     *
     * @param savedInstanceState It is a reference to a Bundle object that is passed into the onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initiate the instances
        editTextUsername = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        try {
            mAuth = FirebaseAuth.getInstance();
        } catch (Exception ex) {
            Log.e(TAG, "GPS LOG | It was not possible to get the instance from Firebase!");
        }

        // This elements are being monitored. If they are clicked, an action is performed
        findViewById(R.id.textViewSignUp).setOnClickListener(this);
        findViewById(R.id.buttonLogin).setOnClickListener(this);
    }

    /**
     * This function takes actions when any element is clicked on the screen.
     *
     * @param view is the base class for the activity.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // Login button
            case R.id.buttonLogin:
                performLogin();
                break;
            // Sing Up link
            case R.id.textViewSignUp:
                startActivity(new Intent(this, SignUpActivity.class));
                break;
        }
    }

    /**
     * When the activity enters the Started state, the system invokes this callback.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // If user is already logged in, the login process will not be necessary.
        if (mAuth.getCurrentUser() != null) {
            Log.i(TAG, "GPS LOG | The user is already logged in.");
            finish();
            startActivity(new Intent(this, AddressesActivity.class));
        }
    }

    /**
     * This function validates if the user credentials in the Login form are valid.
     *
     * @param username This parameter is the username inserted by the user.
     * @param password This parameter is the password inserted by the user.
     * @return if the credentials are valid, it returns true. Otherwise, false.
     */
    private boolean validateCredentials(String username, String password) {

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

        // Credentials are validated
        Log.i(TAG, "GPS LOG | The inserted data are valid.");
        return true;
    }

    /**
     * This function performs the login process by checking the user credentials in the database.
     */
    private void performLogin() {
        // Extract the data from the Login form
        final String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        //If the credentials are valid, perform the login with database.
        if (validateCredentials(username, password)) {
            progressBar.setVisibility(View.VISIBLE);
            try {
                mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.i(TAG, "GPS LOG | The user " + username + " is logged in!");
                            openAddressesActivity();
                        } else {
                            Log.w(TAG, "GPS LOG | " + task.getException().getMessage());
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } catch (Exception ex) {
                Log.e(TAG, "GPS LOG | It was not possible to authenticate. The connection with Firebase is not OK.");
            }
        }
    }

    /**
     * This function opens the next activity once the user is logged in.
     */
    private void openAddressesActivity() {
        // Close the current activity
        this.finish();

        // Open the main menu (AddressesActivity)
        Intent intent = new Intent(MainActivity.this, AddressesActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}