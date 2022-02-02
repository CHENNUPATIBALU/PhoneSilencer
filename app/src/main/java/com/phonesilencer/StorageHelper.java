package com.phonesilencer;

import static android.content.ContentValues.TAG;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.time.LocalDate;

public class StorageHelper extends SQLiteOpenHelper {

    Context context;
    public static final String col_1 = "name";
    public static final String col_2 = "location";
    public static final String col_3 = "alertMode";
    public static final String col_4 = "status";
    public static final String col_5 = "boundedBox";

    public StorageHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table locations(name text primary key,location text, alertMode text, status int, boundedBox text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setData(String name, String[] locationCoordinates, String alertMode, String boundedBox){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(col_1,name);
        cv.put(col_2,locationCoordinates[0]+","+locationCoordinates[1]);
        cv.put(col_3,alertMode);
        cv.put(col_4,1);
        cv.put(col_5,boundedBox);

        try{
            db.insert("locations",null,cv);
            Toast.makeText(context, "Location added successfully", Toast.LENGTH_SHORT).show();
        }catch (SQLiteConstraintException sqLiteConstraintException){
            Log.d(TAG, "setData: "+sqLiteConstraintException);
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    public String getData(){
        String res = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from locations",null);
        if(cursor.getCount()>0){
            while(cursor.moveToNext()){
                res+=cursor.getString(0)+";"+cursor.getString(1)+";"+cursor.getString(2)+";"+cursor.getInt(3)+"\n";
            }
        }

        db.close();
        return res;
    }

    public String getLocationNameByCoordinates(String location){
        String res = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from locations where location=?",new String[]{location});
        if(cursor.getCount()>0){
            while (cursor.moveToNext()){
                res += cursor.getString(0)+";"+cursor.getString(1)+";"+cursor.getString(2)+";"+cursor.getInt(3)+";"+cursor.getString(4);
            }
            db.close();
            return res;
        }
        return res;
    }

    public void deleteDataByLocationName(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        try{
            db.delete("locations","name=?",new String[]{name});
        }catch (Exception exception){
            Log.d(TAG, "deleteDataByLocationName: "+exception);
        }
        db.close();
    }

    public void changeLocationStatus(String name, int status){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(col_4,status);

        try {
            db.update("locations", cv, "name=?", new String[]{name});
        }catch (Exception e){}

        db.close();
    }
}
