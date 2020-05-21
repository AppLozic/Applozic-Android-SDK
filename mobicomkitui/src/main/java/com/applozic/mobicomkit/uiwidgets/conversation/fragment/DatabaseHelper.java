package com.applozic.mobicomkit.uiwidgets.conversation.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Nozha GH
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "hearme";
    public static final String CONTACTS_TABLE_NAME = "LSF";
    public static final String CONFIG_TABLE_NAME = "CONFIG";
    public static final String MEMORY_TABLE_NAME = "memory";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
        Log.i("TAG", "DataBase : " + context + DATABASE_NAME);


    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(
                    "create table " + CONTACTS_TABLE_NAME + "(name text, category text, status INTEGER,favorite boolean,datetime default current_timestamp)"
            );
            db.execSQL(
                    "create table " + CONFIG_TABLE_NAME + "(id INTEGER, sizeFire long, sizeInternal)"
            );
            db.execSQL(
                    "create table " + MEMORY_TABLE_NAME + "(id string PRIMARY KEY, message text)"
            );

            Log.i("TAG", "DataBaseCreated : " + CONTACTS_TABLE_NAME);

        } catch (SQLiteException e) {
            try {
                throw new IOException(e);

            } catch (IOException e1) {
                Log.i("TAG", "ExceptionBaseCreated : " + e);
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
        onCreate(db);
    }

    // insert data in DB
    public boolean insert(String name, String category, Integer status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put("id", id);
        contentValues.put("name", name);
        contentValues.put("category", category);
        contentValues.put("status", status);
        contentValues.put("favorite", 0);


        db.replace(CONTACTS_TABLE_NAME, null, contentValues);
        Log.i("TAG", "DataBase : " + contentValues);

        return true;
    }

    //insert  in  CONFIG DB
    public boolean insertInConfig(long sizeFire, long sizeInternal) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("sizeFire", sizeFire);
        contentValues.put("sizeInternal", sizeInternal);

        db.replace(CONFIG_TABLE_NAME, null, contentValues);
        Log.i("TAG", "DataBaseCONFIG : " + contentValues);

        return true;
    }

    // get all data from database
    public ArrayList getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> array_list = new ArrayList<String>();


        Cursor cursor = db.rawQuery("select * from " + CONTACTS_TABLE_NAME, null);


        int count = cursor.getColumnCount();

        if (cursor != null) {

            if (cursor.moveToFirst()) {
                do {
                    for (int i = 0; i < count; i++) {
                        String data = cursor.getString(i);
                        String column_name = cursor.getColumnName(i);
                        Log.i("TAG", "Cursor" + data + column_name);
                        array_list.add(column_name+ " : "+ data);
                    }
                }
                while (cursor.moveToNext());
            }
            cursor.close();

        }

        return array_list;


    }

    //get size from config
    public ArrayList getDataConfig() {
        ArrayList<String> array_list = new ArrayList<String>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + CONFIG_TABLE_NAME, null);


        if (cursor.moveToFirst()) {
            do {
                String sizeFire = cursor.getString(cursor.getColumnIndex("sizeFire"));
                String sizeInternal = cursor.getString(cursor.getColumnIndex("sizeInternal"));
                array_list.add(sizeFire);
                array_list.add(sizeInternal);
                Log.i("TAG", "sizeInternal : " + sizeInternal);



            } while (cursor.moveToNext());
        }
        cursor.close();
        return array_list;
    }

    // update database
    public boolean update(String s, String s1) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("UPDATE " + CONTACTS_TABLE_NAME + " SET name = " + "'" + s + "', " + "video = " + "'" + s1 + "'");
        return true;
    }

    // delete database
    public boolean delete() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE from " + CONTACTS_TABLE_NAME);
        return true;
    }

    // get current size database


    // get All Category
    public final ArrayList getAllCategory(String cat) {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT name From " + CONTACTS_TABLE_NAME + " where category = '" + cat + "'", null);


        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                array_list.add(name);


            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return array_list;

    }

    // get All Category without condition
    public final ArrayList getAllCategoryWithoutCondition() {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT name From " + CONTACTS_TABLE_NAME, null);


        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                array_list.add(name);


            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return array_list;

    }
    

 /*   //Insert In memory data
    public void insertMemoryData(String id, String msg){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("message", msg);
        db.insert(MEMORY_TABLE_NAME, null, contentValues);
        Log.i("TAG", "MEMORY_TABLE_NAME : " + contentValues);

    }*/

    public void updateFavorite(int value, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        String update = "UPDATE " + CONTACTS_TABLE_NAME + " SET favorite = '"+ value +"' WHERE name = '" + name + "'";

        db.execSQL(update);

    }

    // get favorite
    public final ArrayList getFavorite() {
        ArrayList<String> array_list = new ArrayList<String>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT name From " + CONTACTS_TABLE_NAME + " where favorite = '" + 1 + "'", null);


        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                array_list.add(name);


            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return array_list;

    }



}






