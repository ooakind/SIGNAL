package com.csed.signal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PreferredTimingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferred_timing);

        setListeners();
    }

    public void setListeners(){
        ((Button) findViewById(R.id.shareFast)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData(true);
                finish();
            }
        });
        ((Button) findViewById(R.id.shareSlow)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData(false);
                finish();
            }
        });
    }

    public void sendData(boolean isFast){ // send data to user config

    }
}