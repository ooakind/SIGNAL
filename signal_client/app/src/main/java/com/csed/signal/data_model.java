package com.csed.signal;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;

public class data_model {
    @SerializedName("user_id")
    @Expose
    private String userId;

    @SerializedName("latlng")
    @Expose
    private ArrayList<HashMap<String, String>> latlng;

    @SerializedName("type")
    @Expose
    private String type;

    public data_model(String userId, ArrayList<HashMap<String, String>> latlng, String type){
        this.userId = userId;
        this.latlng = latlng;
        this.type = type;
    }

    public data_model(String userId){
        this.userId = userId;
    }


    public String getUserId(){
        return userId;
    }

    public ArrayList<HashMap<String, String>> getLatlng(){
        return latlng;
    }

    public String getType(){
        return type;
    }

}
