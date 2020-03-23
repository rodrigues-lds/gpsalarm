package com.cs246.gpsalarm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * CONTROL PANEL
 * It provides an interface to control the address processes.
 *
 * @author Eduardo Rodrigues
 * @version 1.2
 * @since 2020-03-10
 */
public class ControlPanelActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth mAuth;
    private EditText name, username;

    /**
     * This function is called each time this activity is created.
     *
     * @param savedInstanceState It is a reference to a Bundle object that is passed into the onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlpanel);

        // These elements are being monitored. If they are clicked, an action is performed
        findViewById(R.id.editAddressOne).setOnClickListener(this);
        findViewById(R.id.editAddressTwo).setOnClickListener(this);
        findViewById(R.id.textAddressOne).setOnClickListener(this);
        findViewById(R.id.textAddressTwo).setOnClickListener(this);

        // TO BE REMOVED
        //name = (EditText) findViewById(R.id.txtName);
        //username = (EditText) findViewById(R.id.txtUsername);

        // TO BE REMOVED
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("Users");
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * This function takes actions when any element is clicked on the screen.
     *
     * @param view is the base class for the activity.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.editAddressOne:
            case R.id.textAddressOne:
                openAddressActivity();
                break;
            case R.id.editAddressTwo:
            case R.id.textAddressTwo:
                openAddressActivity();
                break;
            default:
                break;
        }
    }

    /**
     * This function creates the user menu.
     *
     * @param menu instance.
     * @return true when the menu is loaded.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.userinfo, menu);
        return true;
    }

    /**
     * This function is called each time an item is tapped on menu.
     *
     * @param item selected by the user.
     * @return true when the item is performed.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemLogout:
                openMainActivity();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This function opens the next activity once the user is registered.
     */
    private void openMainActivity() {
        User user = new User();
        if (user.logout()) {

            // Close the current activity
            this.finish();

            // Creating a new instance of the intent
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);

            Toast.makeText(getApplicationContext(), "You are logged out.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "It was not possible to perform the logout process.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This function opens the the address information
     */
    private void openAddressActivity() {
        Intent intent = new Intent(ControlPanelActivity.this, AddressActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }














    // TO BE REMOVED
    public void updateData(View view) {
        User user = new User();
        user.setName(name.getText().toString().trim());
        user.updateData();
    }

    // TO BE REMOVED
    public void readData(View view) {
        mFirebaseDatabase.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot noteDataSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        String dbuser = ds.child("username").getValue(String.class);
                        String dbemail = ds.child("email").getValue(String.class);
                        Log.d("TAG", dbuser + "/" + dbemail);

                    }
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
