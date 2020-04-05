package com.cs246.gpsalarm;

import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

/**
 * GPS ALARM UNIT TEST
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
    public void convertRadiusToMilesTest() {
        // Pre Conditions
        Random rd = new Random();

        // Data Mass
        double km = rd.nextDouble() * 100;

        // Test Steps
        double miles = GPSAlarm.convertRadiusToMiles(km);

        // Check Expected Results
        Assert.assertEquals(km * 0.621371, miles, 0);
    }

    /**
     * This test case validates if all user register form is empty
     */
    @Test
    public void validateUserRegisterFormAllEmpty(){
        RegisterActivity ra = new RegisterActivity();
        boolean result = ra.validateUserInformation("", "", "", "");
        Assert.assertFalse (result);
    }

    /**
     * This test case validates if all user register form is empty
     */
    @Test
    public void validateUserRegisterEmailBadFormat(){
        RegisterActivity ra = new RegisterActivity();
        boolean result = ra.validateUserInformation("Eduardo", "badfFormat.com", "12345", "12345");
        Assert.assertFalse (result);
    }

    /**
     * This test case validates if all the password does not fit the policies
     */
    @Test
    public void validateUserRegisterBadPassword(){
        RegisterActivity ra = new RegisterActivity();
        boolean result = ra.validateUserInformation("Eduardo", "aaa@uol.com", "126", "126");
        Assert.assertFalse (result);
    }

    /**
     * This test case validates if all the username is empty
     */
    @Test
    public void validateUserRegisterUsernameEmpty(){
        RegisterActivity ra = new RegisterActivity();
        boolean result = ra.validateUserInformation("", "aaa@uol.com", "126", "126");
        Assert.assertFalse (result);
    }

    /**
     * This test case validates if all the username is empty
     */
    @Test
    public void validateUserRegisterPasswordEmpty(){
        RegisterActivity ra = new RegisterActivity();
        boolean result = ra.validateUserInformation("Eduardo", "aaa@uol.com", "", "");
        Assert.assertFalse (result);
    }

    /**
     * This test case validates if password dont macthes
     */
    @Test
    public void validateUserRegisterPasswordDontMatch(){
        RegisterActivity ra = new RegisterActivity();
        boolean result = ra.validateUserInformation("Eduardo", "aaa@uol.com", "123456", "aaaaaa");
        Assert.assertFalse (result);
    }

    /**
     * This test case validates if password matches
     */
    @Test
    public void validateUserRegisterPasswordMatches(){
        RegisterActivity ra = new RegisterActivity();
        boolean result = ra.validateUserInformation("Eduardo", "aaa@uol.com", "123456", "123456");
        Assert.assertTrue (result);
    }

    /**
     * This test case validates the register form is ok
     */
    @Test
    public void validateUserRegisterFromOK(){
        RegisterActivity ra = new RegisterActivity();
        boolean result = ra.validateUserInformation("Eduardo Rodrigues", "rodrigues.lds@uol.com", "123456", "123456");
        Assert.assertTrue (result);
    }

    /**
     * This test case checks the missing infos on User Login
     */
    @Test
    public void checkUserLoginMissingInfo(){
        MainActivity ma = new MainActivity();
        boolean result = ma.validateCredentials("", "");
        Assert.assertFalse (result);
    }

    /**
     * This test case checks the missing username on User Login
     */
    @Test
    public void checkUserLoginMissingUsername(){
        MainActivity ma = new MainActivity();
        boolean result = ma.validateCredentials("", "123456");
        Assert.assertFalse (result);
    }

    /**
     * This test case checks the missing password on User Login
     */
    @Test
    public void checkUserLoginMissingPassword(){
        MainActivity ma = new MainActivity();
        boolean result = ma.validateCredentials("rodrigues.lds@gmail.com", "");
        Assert.assertFalse (result);
    }

    /**
     * This test case checks user credentials on login
     */
    @Test
    public void checkUserLoginFormOK(){
        MainActivity ma = new MainActivity();
        boolean result = ma.validateCredentials("rodrigues.lds@gmail.com", "123455");
        Assert.assertTrue (result);
    }
}
