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

    public void setUserDetails(String user){
        SharedPreferences sharedPreferences1 = context.getSharedPreferences("User_Details",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences1.edit();
        editor.putString("User",user);
        editor.apply();
    }

    public String getUser(){
        SharedPreferences sharedPreferences1 = context.getSharedPreferences("User_Details",Context.MODE_PRIVATE);
        return sharedPreferences1.getString("User",null);
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
}
