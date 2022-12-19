package com.csed.signal;

import android.media.MediaParser;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;


public class TargetNotificationActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_notification);
        setListeners();
    }

    private void setListeners () {
        ((RadioGroup)findViewById(R.id.rdQ2)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch(i){
                    case R.id.rQ2select1:
                        mediaPlayer = MediaPlayer.create(TargetNotificationActivity.this, R.raw.sample1);
                        mediaPlayer.start();
                        break;
                    case R.id.rQ2select2:
                        mediaPlayer = MediaPlayer.create(TargetNotificationActivity.this, R.raw.sample2);
                        mediaPlayer.start();
                        break;
                }

            }
        });
        findViewById(R.id.buttonHearMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer = MediaPlayer.create(TargetNotificationActivity.this, R.raw.sample2);
                mediaPlayer.start();
            }
        });

    }
}

