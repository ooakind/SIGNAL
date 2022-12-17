package com.csed.signal;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LocationService extends Service {

    private Handler mHandler;

    private Socket socket;

    private DataOutputStream dos;
    private DataInputStream dis;


    private int locationState = 0;


    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null){
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                connect(latitude, longitude);

                HashMap<String, Double> ll = new HashMap<>();
                ll.put("lat", latitude);
                ll.put("lng", longitude);

                Call<HashMap<String, Double>> call = retrofit_client.getApiService().getState(new currentLocationDataModel("test", ll));
                call.enqueue(new Callback<HashMap<String, Double>>() {
                    @Override
                    public void onResponse(Call<HashMap<String, Double>> call, Response<HashMap<String, Double>> response) {
                        HashMap<String, Double> stateBody = response.body();
                        Log.d("State ", Double.toString(stateBody.get("state")));
                        Log.d("Score", Double.toString(stateBody.get("score")));
                    }

                    @Override
                    public void onFailure(Call<HashMap<String, Double>> call, Throwable t) {

                    }
                });


            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent){
        return null;
    }

    private void startLocationService () {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId);

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channelId, "Location Service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest.Builder lBuilder = new LocationRequest.Builder(400);

        lBuilder.setMinUpdateIntervalMillis(2000);
        lBuilder.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationRequest locationRequest = lBuilder.build();
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, mLocationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
    }

    private void stopLocationService() {
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(Constants.ACTION_START_LOCATION_SERVICE)) {
                    startLocationService();
                } else if (action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)) {
                    stopLocationService();
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void connect(double latitude, double longitude) {
        mHandler = new Handler();
        Thread checkUpdate = new Thread() {
            public void run() {
                try{
                    socket = new Socket(Constants.IP, Constants.PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    if (socket == null)
                        System.out.println("???");
                    System.out.println(socket.getOutputStream());
                    dos = new DataOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(socket.getInputStream());
                    dos.writeUTF(latitude + " " + longitude);
                    dos.flush();

                    locationState = dis.readInt();

                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        };
        checkUpdate.start();
    }
}
