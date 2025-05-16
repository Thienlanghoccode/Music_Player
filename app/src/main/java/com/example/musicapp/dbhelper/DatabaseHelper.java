package com.example.musicapp.dbhelper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import com.example.musicapp.entity.User;

import java.io.ByteArrayOutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_FORM= "db_form";
    private static final String FORM_TABLE= "form_table";

    private static final String ID="ID";

    private static final String EMAIL_COLUMN= "EMAIL";
    private static final String PASSWORD_COLUMN= "PASSWORD";
    private static final String USERNAME_COLUMN= "USER_NAME";
    private static final String IMAGE_COLUMN= "IMAGE";

    public DatabaseHelper(Context context){
        super(context,DB_FORM,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table form_table(ID INTEGER PRIMARY KEY AUTOINCREMENT,USER_NAME TEXT,EMAIL TEXT,PASSWORD TEXT,IMAGE BLOB)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table IF exists "+ FORM_TABLE);
        onCreate(db);
    }
    public void isUserRegister(String username, String email, String password){

        SQLiteDatabase db= this.getReadableDatabase();
        ContentValues cv= new ContentValues();
        cv.put("USER_NAME",username);
        cv.put("EMAIL",email);
        cv.put("PASSWORD",password);
        cv.put("IMAGE","null");
        long result= db.insert(FORM_TABLE,null,cv);
    }

    public boolean isUserLogin(String email,String password){
        SQLiteDatabase db= this.getReadableDatabase();
        String [] id={ID};
        String selections= EMAIL_COLUMN+"=? and "+PASSWORD_COLUMN+"=?";
        String [] selectionArgs={email,password};
        @SuppressLint("Recycle") Cursor cursor= db.query(FORM_TABLE,id,selections,selectionArgs,null,null,null);
        return cursor.getCount()>0;
    }
    public boolean checkEmail(String email){
        SQLiteDatabase db= this.getReadableDatabase();
        String [] id={ID};
        String selections= EMAIL_COLUMN+"=?";
        String [] selectionArgs={email};
        @SuppressLint("Recycle") Cursor cursor= db.query(FORM_TABLE,id,selections,selectionArgs,null,null,null);
        return cursor.getCount()>0;
    }
    public boolean updatePassword(String email,String password){
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();
        cv.put("PASSWORD",password);
        long result = db.update(FORM_TABLE,cv,EMAIL_COLUMN+"=?",new String[]{email});
        return result != -1;
    }
    public Cursor findUser(String email){
        if (email == null) {
            return null;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"USER_NAME", EMAIL_COLUMN, "IMAGE"};
        String selection = EMAIL_COLUMN + "=?";
        String[] selectionArgs = {email};
        return db.query(FORM_TABLE, columns, selection, selectionArgs, null, null, null);
    }
    public void updateUser(User user){
        SQLiteDatabase db = this.getWritableDatabase();
        Bitmap bitmapImage= user.getAvatar();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.PNG,100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        ContentValues cv= new ContentValues();
        cv.put("USER_NAME",user.getUsername());
        cv.put("IMAGE", imageBytes);
        long result = db.update(FORM_TABLE,cv,EMAIL_COLUMN+"=?",new String[]{user.getEmail()});
    }


}
