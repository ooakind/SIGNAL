package com.csed.signal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;


public class currentLocationDataModel {

    @SerializedName("user_id")
    @Expose
    private String user_id;


    @SerializedName("curr_loc")
    @Expose
    private HashMap <String, Double> userCurrentLocation;

    public currentLocationDataModel(String user_id, HashMap <String, Double> curr){
        this.user_id = user_id;
        this.userCurrentLocation = curr;
    }

    public HashMap <String, Double> getCurrLoc(){
        return userCurrentLocation;
    }

    public String getUserID(){
        return user_id;
    }
}
