package com.phonesilencer;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class BackgroundService extends Service {
    private static final String NOTIF_ID = "100";
    private static final String NOTIF_CHANNEL_ID = "Notification";
    NotificationCompat.Builder notificationBuilder;
    FirebaseFirestore db;
    StorageHelper storageHelper;

    public BackgroundService() {

    }

    private LocationCallback locationCallback = new LocationCallback() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                Log.d("LOCATION_UPDATE", latitude + ", " + longitude);
                checkCoordinates(latitude+"",longitude+"");
            }
        }

        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        db = FirebaseFirestore.getInstance();
        storageHelper = new StorageHelper(getApplicationContext(),"Silencer",null,1);
        if(intent!=null){
            if(intent.getAction()!=null){
                if(intent.getAction().equals("2000")){
                    startForegroundService();
                }else if(intent.getAction().equals("3000")){
                    stopForegroundService();
                }
            }
        }
        startForegroundService();
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startForegroundService() {
        String NOTIFICATION_CHANNEL_ID = "com.srivenkataramanamilkproducts";
        String channelName = "Background Location";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("App is running in background")
                .setContentText("Syncing your location")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(false);

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            startForeground(1000,notificationBuilder.build());
        }
    }

    public void stopForegroundService(){
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    public void checkCoordinates(String curLatitude, String curLongitude){
        String data = storageHelper.getLocationNameByCoordinates(curLatitude+","+curLongitude);

        if(!data.equals("")){
            String[] details = data.split(";");
            double latitude = Double.parseDouble(details[1].split(",")[0]);
            double longitude = Double.parseDouble(details[1].split(",")[1]);
            String alertMode = details[2];
            String status = details[3];
            if(String.valueOf(latitude).equals(curLatitude)  && String.valueOf(longitude).equals(curLongitude) && status.equals("1")){
                switch (alertMode){
                    case "Silent":
                        try{
                            new AudioManagerHelper(getApplicationContext()).setAudioToSilent();
                        }catch (SecurityException securityException){
                            Toast.makeText(this, "Do Not Disturb Permission not granted", Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "onSuccess: SILENCED");
                        break;
                    case "Vibrate":
                        try{
                            new AudioManagerHelper(getApplicationContext()).setAudioToVibration();
                        }catch (SecurityException securityException){
                            Toast.makeText(this, "Do Not Disturb Permission not granted", Toast.LENGTH_SHORT).show();
                        }
                        Log.d(TAG, "onSuccess: VIBRATE");
                        break;
                }
            }else{
                try{
                    new AudioManagerHelper(getApplicationContext()).setAudioToNormal();
                }catch (SecurityException securityException){
                    Toast.makeText(this, "Do Not Disturb Permission not granted", Toast.LENGTH_SHORT).show();
                }
                Log.d(TAG, "onSuccess: NORMAL");
            }
        }
    }


}