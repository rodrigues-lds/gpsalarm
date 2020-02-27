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
}
