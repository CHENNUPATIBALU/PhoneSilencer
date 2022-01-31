package com.phonesilencer;

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
                //new StorageHelper(getApplicationContext(),"Silencer",null,1).setData(new String[]{latitude+"",longitude+""});
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
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
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
        db.collection("Users")
                .document("Balu")
                .collection("Locations")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for(QueryDocumentSnapshot qds:queryDocumentSnapshots){
                            db.collection("Users")
                                    .document("Balu")
                                    .collection("Locations")
                                    .document(qds.getId())
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            String coordinates = documentSnapshot.getData().get("Coordinates").toString();
                                            double latitude = Double.parseDouble(coordinates.split(",")[0]);
                                            double longitude = Double.parseDouble(coordinates.split(",")[1]);
                                            if(latitude <= Double.parseDouble(curLatitude) && longitude <= Double.parseDouble(curLongitude)){
                                                switch (documentSnapshot.getData().get("Alert Mode").toString()){
                                                    case "Silent":
                                                        new AudioManagerHelper(getApplicationContext()).setAudioToSilent();
                                                        break;
                                                    case "Vibrate":
                                                        new AudioManagerHelper(getApplicationContext()).setAudioToVibration();
                                                        break;
                                                }
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}