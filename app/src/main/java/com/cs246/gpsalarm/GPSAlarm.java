package com.cs246.gpsalarm;

/**
 * Created by Eduardo A Rodrigues on 2/24/2020.
 */
public class GPSAlarm {

    /*
    This function converts the Radius to Kilometers.
     */
    public double convertRadiusToKilometers(double miles) {
        return miles * 1.609344;
    }

    /*
    This function checks if the User and Passwords have been filled, this should by itself
    take the Strings in the User and Passwords fields and return true if the have been filled,
    if not this should return false and another function should notify the user this.
     */
    public boolean validatingUserPassword(String userAndPassword) { //For test purpuses we are giving an input, but this should take that informatio by itself

        if (userAndPassword.length() >1) {//with this we can say that at least there are two characters in the user and password.
            return true;
        } else {
            return false;
        }
    }

    public boolean validatingUser(String user) { //For test purpuses we are giving an input, but this should take that informatio by itself

        if (user.length() >1) {//with this we can say that at least there are two characters in the user and password.
            return true;
        } else {
            return false;
        }
    }

    public boolean checksIfTheGPSisActivated() {

        boolean GPSstate=true; //This should be replaced in a future with the real logic of the method and as a result we will obtain true or false

        if (GPSstate=true) {
            return true;
        } else{
            return false;
        }
    }

    public double convertAddressToLatitude(String address, double latitude) {return latitude;}

    public double convertAddressToLongitude(String address, double longitude) {return longitude;}
}
