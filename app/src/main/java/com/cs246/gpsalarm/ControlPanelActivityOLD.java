package com.cs246.gpsalarm;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CONTROL PANEL
 * It provides an interface to control the address processes.
 *
 * @author Eduardo Rodrigues
 * @version 1.2
 * @since 2020-03-10
 */
public class ControlPanelActivityOLD extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;
    private FirebaseAuth mAuth;
    private EditText user, email;
    private String UserId;

    public static final String ADDRESS_POSITION = "com.cs246.gpsalarm";

    /**
     * This function is called each time this activity is created.
     *
     * @param savedInstanceState It is a reference to a Bundle object that is passed into the onCreate.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controlpanel_old);



        // These elements are being monitored. If they are clicked, an action is performed
        findViewById(R.id.editAddressOne).setOnClickListener(this);
        findViewById(R.id.editAddressTwo).setOnClickListener(this);
        findViewById(R.id.textAddressOne).setOnClickListener(this);
        findViewById(R.id.textAddressTwo).setOnClickListener(this);

        // TO BE REMOVED
        user = (EditText) findViewById(R.id.txtName);
        email = (EditText) findViewById(R.id.txtUsername);

        // TO BE REMOVED
        this.mAuth = FirebaseAuth.getInstance();
        mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers/Users/" + mAuth.getCurrentUser().getUid());


        //UserId = mFirebaseDatabase.push().getKey();
        UserId = mAuth.getCurrentUser().getUid();

        /*mFirebaseInstance = FirebaseDatabase.getInstance();
        mFirebaseDatabase = mFirebaseInstance.getReference("DataUsers");
        mAuth = FirebaseAuth.getInstance();*/

        //UserId = mFirebaseDatabase.push().getKey();
    }

    /**
     * This function takes actions when any element is clicked on the screen.
     *
     * @param view is the base class for the activity.
     */
    @Override
    public void onClick(View view) {
        String addressPosition = "-1";

        switch (view.getId()) {
            case R.id.editAddressOne:
            case R.id.textAddressOne:
                addressPosition = "1";
                break;
            case R.id.editAddressTwo:
            case R.id.textAddressTwo:
                addressPosition = "2";
                break;
            default:
                break;
        }

        openAddressActivity(addressPosition);
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
    private void openAddressActivity(String addressPosition) {
        Intent intent = new Intent(ControlPanelActivityOLD.this, AddressActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ADDRESS_POSITION, addressPosition);
        startActivity(intent);
    }


    // TO BE REMOVED
    public void addUser(String username, String email) {
        User users = new User(username, email);

        //mFirebaseDatabase.child(UserId).setValue(users);
        mFirebaseDatabase.setValue(users);
    }

    public void updateUser(String username, String email) {
        mFirebaseDatabase.child("name").setValue(username);
        mFirebaseDatabase.child("username").setValue(email);
    }

    public void insertData(View view) {
        addUser(user.getText().toString().trim(), email.getText().toString().trim());
    }

    public void updateData(View view) {
        updateUser(user.getText().toString().trim(), email.getText().toString().trim());
    }

    public void readData(View view) {



        mFirebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               User users = new User();
               users = dataSnapshot.getValue(User.class);

                Map<String, Object> td = (HashMap<String,Object>) dataSnapshot.getValue();
                List<GPSAlarm> tds = (List<GPSAlarm>) dataSnapshot.child("GPSAlarm").getValue();

                int bbb = 0;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
