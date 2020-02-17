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
        Toast toast = Toast.makeText(this, "Toast Message Example", Toast.LENGTH_LONG);
        toast.show();
    }
}
