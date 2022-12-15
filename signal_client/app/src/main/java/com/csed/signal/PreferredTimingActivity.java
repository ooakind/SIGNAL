package com.csed.signal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreferredTimingActivity extends AppCompatActivity {

    private ArrayList<HashMap<String, String>> latlng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferred_timing);

        getUserData();
        setListeners();
    }

    public void setListeners(){
        ((Button) findViewById(R.id.shareFast)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData(true);
            }
        });
        ((Button) findViewById(R.id.shareSlow)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData(false);
            }
        });
    }

    public void getUserData(){ // get data from user config
        Call<data_model> call = retrofit_client.getApiService().getData(new data_model("test", new ArrayList<HashMap<String, String>>(), "fast"));
        call.enqueue(new Callback<data_model>() {
            @Override
            public void onResponse(Call<data_model> call, Response<data_model> response) {
                data_model result = response.body();
                latlng = result.getLatlng();
                String userType = result.getType();

                ((TextView)findViewById(R.id.currentType)).setText("Current Type: " + userType);
            }

            @Override
            public void onFailure(Call<data_model> call, Throwable t) {
                //Log.d("??", t.toString());
            }
        });

    }


    public void sendData(boolean isFast){ // send data to user config
        int i = 0;

        String userType = isFast ? "fast" : "slow";
        data_model dm = new data_model("test", latlng, userType);
        Call<data_model> call = retrofit_client.getApiService().setData(dm);
        call.enqueue(new Callback<data_model>() {
            @Override
            public void onResponse(Call<data_model> call, Response<data_model> response) {

            }

            @Override
            public void onFailure(Call<data_model> call, Throwable t) {

            }
        });

        finish();

    }
}