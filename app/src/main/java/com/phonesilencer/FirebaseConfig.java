package com.phonesilencer;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class FirebaseConfig {

    FirebaseFirestore db;

    public FirebaseConfig(){
        db = FirebaseFirestore.getInstance();
    }

    public void saveLocation(String email, Map<String,String> locationDetails){

    }
}
