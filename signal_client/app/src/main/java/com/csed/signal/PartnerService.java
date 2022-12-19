package com.csed.signal;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PartnerService extends Service {

    private AtomicBoolean working = new AtomicBoolean(true);
    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private boolean stopFlag = false;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {

            while(working.get() && !stopFlag) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Call<HashMap<String, Double>> call = retrofit_client.getApiService().getPartnerState(Constants.USER_ID);
                call.enqueue(new Callback<HashMap<String, Double>>() {
                    @Override
                    public void onResponse(Call<HashMap<String, Double>> call, Response<HashMap<String, Double>> response) {
                        HashMap<String, Double> data = response.body();
                        if (data.get("state") == 1) {
                            notificationManager.notify(Constants.PARTNER_SERVICE_ID, builder.build());
                        }

                    }

                    @Override
                    public void onFailure(Call<HashMap<String, Double>> call, Throwable t) {

                    }
                });

            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Intent resultIntent = new Intent(getApplicationContext(), CallActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(getApplicationContext(), "partner");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Call your partner!");
        builder.setContentText("Your partner may have a hard time.");
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel("partner") == null) {
                NotificationChannel notificationChannel = new NotificationChannel("partner", "partner Service", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setDescription("This channel is used by partner service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }


        new Thread(runnable).start();
        Toast.makeText(getApplicationContext(), "Partner service started", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopFlag = true;
        Toast.makeText(getApplicationContext(), "Partner service stopped", Toast.LENGTH_SHORT).show();
        stopForeground(true);
    }
}