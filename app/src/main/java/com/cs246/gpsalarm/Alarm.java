package com.cs246.gpsalarm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Alarm extends AppCompatActivity {
    Button btn ;
    TextView t1 , t2 ;
    EditText latitude_txt , longitude_txt ;
    boolean check = false ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        latitude_txt = (EditText)findViewById(R.id.latitude);
        longitude_txt = (EditText)findViewById(R.id.longitude);

        Bundle extras = getIntent().getExtras();
        double latitude = extras.getDouble("latitude");
        double longitude = extras.getDouble("longitude");
        latitude_txt.setText(latitude+"");
        longitude_txt.setText(longitude+"");

        TextView t = (TextView)findViewById(R.id.text);
        // t.setText(value1+" & "+value2);

    }

    @Override
    public void finish() {
        if(check==true) {
            double latitude = Double.parseDouble(latitude_txt.getText().toString());
            double longitude = Double.parseDouble(longitude_txt.getText().toString());
            // Prepare data intent
            Intent i = new Intent();
            i.putExtra("alarm_location_latitude", latitude);
            i.putExtra("alarm_location_longitude", longitude);
            // Activity finished ok, return the data
            setResult(RESULT_OK, i);
        }
        super.finish();
    }

    public void SetAlarm(View v){
        check = true;
        finish();
    }

}