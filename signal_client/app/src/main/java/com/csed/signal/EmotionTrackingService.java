package com.csed.signal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmotionTrackingService extends Service{
    private NotificationManager notificationManager;
    private NotificationCompat.Builder builder;

    private AtomicBoolean working = new AtomicBoolean(true);
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (working.get()) {
                Call<emotion_data_model> call = retrofit_client.getApiService().getEmotionData(Constants.USER_ID); //EDIT
                call.enqueue(new Callback<emotion_data_model>() {
                    @Override
                    public void onResponse(Call<emotion_data_model> call, Response<emotion_data_model> response) {
                        emotion_data_model emotionDataBody = response.body();
                        Log.i("Emotion onResponse", "working");
                        if (emotionDataBody.getEmotionData() != null) {
                            notificationManager.notify(Constants.EMOTION_TRACKING_SERVICE_ID, builder.build());
                            Log.d("emotionData ", emotionDataBody.getEmotionData().toString());
                            Log.d("error", emotionDataBody.getError());
                        } else {
                            Log.d("emotion tracker", "No emotion change.");
                        }
                    }
                    @Override
                    public void onFailure(Call<emotion_data_model> call, Throwable t) {
                        Log.d("emotionData onFailure", t.getMessage());
                    }
                });
                try {
                    Thread.sleep(900000); // 15 min: 900000
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        String channelId = "target_notification_channel";
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent(getApplicationContext(), TargetNotificationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(getApplicationContext(), channelId);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Regulate your emotional state :)");
        builder.setContentText("Take a short break. Click!");
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, "Emotion Tracking Service", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("This channel is used by emotion tracking service");
            notificationManager.createNotificationChannel(notificationChannel);
        }

        new Thread(runnable).start();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        working.set(false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
