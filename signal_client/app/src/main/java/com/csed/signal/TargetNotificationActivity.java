package com.csed.signal;

import android.content.Intent;
import android.media.MediaParser;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TargetNotificationActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private String Q1Answer;
    private String Q2Answer;
    private Integer clickMoreCnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_notification);
        setListeners();
    }

    private void setListeners () {
        ((RadioGroup)findViewById(R.id.rdQ1)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.rQ1Eating:
                        Q1Answer = "eating";
                        break;
                    case R.id.rQ1Hobby:
                        Q1Answer = "hobby";
                        break;
                    case R.id.rQ1Moving:
                        Q1Answer = "moving";
                        break;
                    case R.id.rQ1Talking:
                        Q1Answer = "talking";
                        break;
                    case R.id.rQ1Working:
                        Q1Answer = "working";
                        break;
                    case R.id.rQ1Others:
                        Q1Answer = "others";
                        break;
                }
            }
        });
        ((RadioGroup)findViewById(R.id.rdQ2)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.rQ2select1:
                        Q2Answer = "yes";
                        mediaPlayer = MediaPlayer.create(TargetNotificationActivity.this, R.raw.sample1);
                        mediaPlayer.start();
                        break;
                    case R.id.rQ2select2:
                        Q2Answer = "no";
                        mediaPlayer = MediaPlayer.create(TargetNotificationActivity.this, R.raw.sample2);
                        mediaPlayer.start();
                        break;
                }

            }
        });
        findViewById(R.id.buttonHearMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Field[] fields=R.raw.class.getFields();
                int randomIdx = new Random().nextInt(fields.length);
                try {
                    int sampleIdx = fields[randomIdx].getInt(fields[randomIdx]);
                    mediaPlayer = MediaPlayer.create(TargetNotificationActivity.this, sampleIdx);
                    mediaPlayer.start();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                clickMoreCnt += 1;
            }
        });
        findViewById(R.id.buttonSubmit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date now = new Date();
                SimpleDateFormat formattedNow = new SimpleDateFormat("dd-MM-yy hh:mm:ss");
                String _timestamp = formattedNow.format(now);

                target_notification_data_model dataModel = new target_notification_data_model(Constants.USER_ID, _timestamp, Q1Answer, Q2Answer, clickMoreCnt);
                Call<HashMap<String, String>> call = retrofit_client.getApiService().sendTargetNotificationData(dataModel);
                call.enqueue(new Callback<HashMap<String, String>>() {
                    @Override
                    public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                        Log.d("send target success", response.message());
                    }

                    @Override
                    public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                        Log.d("send target failure", t.getMessage());

                    }
                });

                Intent intent = new Intent(TargetNotificationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}

