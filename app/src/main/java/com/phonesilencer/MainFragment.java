package com.phonesilencer;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skyfishjy.library.RippleBackground;

import java.util.HashMap;
import java.util.Map;

public class MainFragment extends Fragment {

    Button addLocationBtn;
    TextView latTv,longTv,locTv;
    public static final int LOCATION_PERMISSION = 100;
    public static final int STORAGE_PERMISSION = 200;
    RippleBackground rippleBackground;
    StorageHelper storageHelper;
    BackgroundService backgroundService;
    String alertMode = "";
    FirebaseFirestore db;
    double latitude,longitude;


    private LocationCallback locationCallback = new LocationCallback() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                latitude = locationResult.getLastLocation().getLatitude();
                longitude = locationResult.getLastLocation().getLongitude();
                latTv.setText("Latitude: "+latitude);
                longTv.setText("Longitude: "+longitude);
                getAddress(latitude+"",longitude+"");
                locTv.setText("Location: "+new SharedPreferencesHelper(getActivity()).getLocationAddress());
                //storageHelper.setData(new String[]{latitude+"",longitude+""});
            }
        }

        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        if(container!=null){
            container.removeAllViews();
        }

        db = FirebaseFirestore.getInstance();
        addLocationBtn = root.findViewById(R.id.addLocationBtn);

        latTv = root.findViewById(R.id.latitudeTv);
        longTv = root.findViewById(R.id.longitudeTv);
        locTv = root.findViewById(R.id.locationTv);


        storageHelper = new StorageHelper(getActivity(),"Silencer",null,1);

        rippleBackground = root.findViewById(R.id.rippleBg);
        rippleBackground.startRippleAnimation();

        requestPermissions();
        startLocationService();

        getLocation();

        addLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View dialogView = inflater.inflate(R.layout.activity_add_location,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(dialogView);
                builder.setCancelable(false);

                TextInputLayout inputLayout = dialogView.findViewById(R.id.locationNameEt);
                TextInputLayout coordinateLayout = dialogView.findViewById(R.id.coordinatesEt);
                coordinateLayout.getEditText().setText(latitude+","+longitude);
                Chip silentChip = dialogView.findViewById(R.id.silentChip);
                alertMode = "Silent";
                Chip vibrateChip = dialogView.findViewById(R.id.vibrateChip);

                silentChip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertMode = "Silent";
                        silentChip.setChecked(true);
                        vibrateChip.setChecked(false);
                    }
                });

                vibrateChip.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertMode = "Vibrate";
                        vibrateChip.setChecked(true);
                        silentChip.setChecked(false);
                    }
                });

                AlertDialog alertDialog;
                builder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String locationName = inputLayout.getEditText().getText().toString();
                        String coordinates[] = new String[]{latitude+"",longitude+""};
                        sendLocationDataToDB(locationName,coordinates,alertMode);
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog = builder.create();
                alertDialog.show();
            }
        });

        return root;
    }

    public String getBoundedBox(double latitude, double longitude){
        double earthDiameter = 6378.137D;
        double pi = Math.PI;
        double m = (1/((2*pi*360)*earthDiameter))/1000;

        double newLatitude = latitude+(100*m);
        double newLongitude = longitude+(100*m)/Math.cos(latitude*(pi/180));

        return newLatitude+","+newLongitude;
    }

    public void sendLocationDataToDB(String locationName, String[] coordinates, String alertMode){
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Adding your location");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Map<String,Object> map = new HashMap<>();
        map.put("Location Name", locationName+"");
        map.put("Coordinates",coordinates[0]+","+coordinates[1]);
        map.put("Alert Mode",alertMode+"");
        db.collection("Users")
                .document("Balu")
                .collection("Locations")
                .document(locationName)
                .set(map)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "Location added", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.d(TAG, "onFailure: "+e);
                    }
                });
    }

    private void getAddress(String latitude, String longitude) {
        new FetchLocation(getActivity()).execute(latitude+","+longitude);
    }

    private boolean isLocationServiceRunning(){
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager!=null){
            for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
                if(BackgroundService.class.getName().equals(service.service.getClassName())){
                    if(service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent intent = new Intent(getActivity(),BackgroundService.class);
            intent.setAction("2000");
            getActivity().startService(intent);
            Toast.makeText(getActivity(), "Location Service started", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService(){
        if(isLocationServiceRunning()){
            Intent intent = new Intent(getActivity(),BackgroundService.class);
            intent.setAction("3000");
            getActivity().startService(intent);
            Toast.makeText(getActivity(), "Location Service stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        startLocationService();
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getLocation(){
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage("Fetching your location");
        progressDialog.setCancelable(false);
        progressDialog.show();
        if (ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            progressDialog.dismiss();
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }else{
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(2000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            LocationServices.getFusedLocationProviderClient(getActivity()).requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper());
            progressDialog.dismiss();
        }
    }

    private void requestPermissions(){
        if (ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION);
        }
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},STORAGE_PERMISSION);
        }
        if(ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION);
        }
        if(ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.FOREGROUND_SERVICE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.FOREGROUND_SERVICE},201);
        }
    }
}