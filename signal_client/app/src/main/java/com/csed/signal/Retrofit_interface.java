package com.csed.signal;

import java.util.HashMap;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Path;

public interface Retrofit_interface {
    @POST("getData")
    Call<data_model> getData(@Body data_model dataModel);


    @POST("setData")
    Call<data_model> setData(@Body data_model dataModel);


    @POST("getState")
    Call<HashMap<String, Double>> getState(@Body currentLocationDataModel locModel);

    @GET("getPartnerState")
    Call<HashMap<String, Double>> getPartnerState(@Query("user_id") String user_id);

    @GET("getEmotionData/{userId}")
    Call<emotion_data_model> getEmotionData(@Path("userId") String userId);
}
