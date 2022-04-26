package com.phonesilencer;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    Context context;
    SharedPreferences sharedPreferences;
    public SharedPreferencesHelper(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("Location_Details",Context.MODE_PRIVATE);
    }

    public void setDNDPermissionDetails(boolean granted){
        SharedPreferences sharedPreferences1 = context.getSharedPreferences("DND_Permission",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences1.edit();
        editor.putBoolean("Permission",granted);
        editor.apply();
    }

    public boolean getDNDPermission(){
        SharedPreferences sharedPreferences1 = context.getSharedPreferences("DND_Permission",Context.MODE_PRIVATE);
        return sharedPreferences1.getBoolean("Permission",false);
    }

    public void setLocationAddress(String locationAddress, String coordinates){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Location",locationAddress);
        editor.putString("Coordinates",coordinates);
        editor.apply();
    }

    public String getLocationAddress(){
        String address = sharedPreferences.getString("Location",null);
        return (address!=null)?address:"";
    }

    public void addLocation(String name, String coordinates, String boundedBox, String alertMode){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Location_Name",name);
        editor.putString("Coordinates",coordinates);
        editor.putString("BoundedBox",boundedBox);
        editor.putString("AlertMode",alertMode);
        editor.apply();
    }

    public String getCoordinates(){
        String coords = sharedPreferences.getString("Coordinates",null);
        String boundedBox = sharedPreferences.getString("BoundedBox",null);
        String alertMode = sharedPreferences.getString("AlertMode",null);
        String locationName = sharedPreferences.getString("Location_Name",null);
        return (coords!=null)?locationName+";"+coords+";"+boundedBox+";"+alertMode:"";
    }
}
