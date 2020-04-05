package com.cs246.gpsalarm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * GPS ALARM | TEST CASES
 * It provides the test cases to validate some aspects of the app.
 *
 * @author Jose Paz, Robert Hampton, Hernan Yupanqui & Eduardo Rodrigues
 * @version 1.2
 * @since 2020-03-06
 * <p>
 * This class provides all test cases necessary to validate the application.
 */
public class GpsAlarmUnitTests {

    /**
     * This test case validates if radius can be converted to miles.
     */
    @Test
    public void ConvertRadiusToMilesTest() {
        // Pre Conditions
        Random rd = new Random();

        // Data Mass
        double km = rd.nextDouble() * 100;

        // Test Steps
        double miles = GPSAlarm.convertRadiusToMiles(km);

        // Check Expected Results
        Assert.assertEquals(km * 0.621371, miles, 0);
    }

    public void deva(){
        RegisterActivity ra = new RegisterActivity();
        ra.validateUserInformation("", "", "", "");
    }

    /*
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

    *//*
    This function is checking if the check if TheGPSisActivated method is working as it should be, providing boolean data

     *//*
    @Test
    public void CheckGPSActivated() {
        //Check if the GPS is activated
        GPSAlarm gpsAlarm=new GPSAlarm();

        if (gpsAlarm.checksIfTheGPSisActivated()){
            Assert.assertEquals(true , gpsAlarm.checksIfTheGPSisActivated());
        } else {
            Assert.assertEquals(false, gpsAlarm.checksIfTheGPSisActivated());
        }


    }*/
}
