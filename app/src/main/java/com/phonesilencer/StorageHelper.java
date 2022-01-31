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
    public static final String col_2 = "latitude";
    public static final String col_3 = "longitude";

    public StorageHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table locations(name text primary key,latitude text, longitude text, addedDate text)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setData(String[] locationCoordinates){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(col_1,"Name");
        cv.put(col_2,locationCoordinates[0]);
        cv.put(col_3,locationCoordinates[1]);

        try{
            db.insert("locations",null,cv);
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
                res+=cursor.getString(0)+","+cursor.getString(1)+","+cursor.getString(2)+"\n";
            }
        }
        return res;
    }

    public boolean deleteDataByIndex(int index){
        return false;
    }
}
