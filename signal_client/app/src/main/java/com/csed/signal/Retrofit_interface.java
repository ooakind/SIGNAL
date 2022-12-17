package com.csed.signal;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.POST;

public interface Retrofit_interface {
    @POST("getData")
    Call<data_model> getData(@Body data_model dataModel);


    @POST("setData")
    Call<data_model> setData(@Body data_model dataModel);


    @POST("getState")
    Call<HashMap<String, Double>> getState(@Body currentLocationDataModel locModel);

}
