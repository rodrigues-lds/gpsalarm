package com.cs246.gpsalarm;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Change this toast message in order to simulate the conflict when merging.
        // Don't forget to pull before pushing your code.
        Toast toast = Toast.makeText(this, "Robert changed this message, yo", Toast.LENGTH_LONG);
        toast.show();
    }
}
