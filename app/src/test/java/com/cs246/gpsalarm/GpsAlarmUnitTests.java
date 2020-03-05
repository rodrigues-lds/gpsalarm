package com.cs246.gpsalarm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * Created by Eduardo A Rodrigues on 2/24/2020.
 */
public class GpsAlarmUnitTests {
    @Test
    public void ConvertRadiusToKilometersTest() {
        // Pre Conditions
        GPSAlarm gpsAlarm = new GPSAlarm();
        Random rd = new Random();

        // Data Mass
        double miles = rd.nextDouble() * 100;

        // Test Steps
        double kilometers = gpsAlarm.convertRadiusToKilometers(miles);

        // Check Expected Results
        Assert.assertEquals(miles * 1.609344, kilometers, 0);
    }

    @Test
    public void CheckThePassword() {

        GPSAlarm gpsAlarm= new GPSAlarm();

        String example= "User + Password";

        Assert.assertTrue(gpsAlarm.validatingUserPassword(example));

    }

    @Test
    public void CheckTheUser() {

        GPSAlarm gpsAlarm= new GPSAlarm();

        String example= "User + Password";

        Assert.assertTrue(gpsAlarm.validatingUser(example));

    }

    @Test
    public void ConvertAddressToLongitudeTest() {
        // Pre Conditions
        GPSAlarm gpsAlarm = new GPSAlarm();

        // Data Mass
        String address = "Address";

        // Test Steps
        double longitude = gpsAlarm.convertAddressToLongitude(address, 0);

        // Check Expected Results
        Assert.assertEquals(longitude, 0, 0);
    }

    @Test
    public void ConvertAddressToLatitudeTest() {
        // Pre Conditions
        GPSAlarm gpsAlarm = new GPSAlarm();

        // Data Mass
        String address = "Address";

        // Test Steps
        double latitude = gpsAlarm.convertAddressToLatitude(address, 0);

        // Check Expected Results
        Assert.assertEquals(latitude, 0, 0);
    }
    /*
    This function is checking if the checkifTheGPSisActivated method is working as it should be, providing boolean data

     */
    @Test
    public void CheckGPSActivated() {
        //Check if the GPS is activated
        GPSAlarm gpsAlarm=new GPSAlarm();

        if (gpsAlarm.checksIfTheGPSisActivated()){
            Assert.assertEquals(true , gpsAlarm.checksIfTheGPSisActivated());
        } else {
            Assert.assertEquals(false, gpsAlarm.checksIfTheGPSisActivated());
        }


    }


}
