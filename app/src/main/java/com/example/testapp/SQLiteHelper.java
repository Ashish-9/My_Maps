package com.example.testapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import static com.example.testapp.HomeFragment.sqLiteHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
    public SQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    public SQLiteHelper(Context context) {
        super(context,sqLiteHelper.getDatabaseName(),null,1);
    }

    public void queryData(String sql) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL(sql);
    }

    public void insertData(String desc, String lat, String longi, String areaa, byte[] photo, int status) {
        SQLiteDatabase database = getWritableDatabase();
        String sql = "INSERT INTO POST VALUES (NULL, ?, ?, ?, ?, ?, ?)";

        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();

        statement.bindString(1, desc);
        statement.bindString(2, lat);
        statement.bindString(3, longi);
        statement.bindString(4, areaa);
        statement.bindBlob(5, photo);
        statement.bindString(6, String.valueOf(status));

        statement.executeInsert();
    }


    public Cursor getData(String sql){
        SQLiteDatabase database = getReadableDatabase();
        return database.rawQuery(sql, null);
    }

    public void updateData(int Id)
    {
        SQLiteDatabase database = getWritableDatabase();
        String sql = "UPDATE POST SET status = 0 WHERE Id =?;";
        SQLiteStatement statement = database.compileStatement(sql);

        statement.bindString(1, String.valueOf(Id));
        statement.execute();
        database.close();
    }

    public  void deleteData(int Id) {
        SQLiteDatabase database = getWritableDatabase();
        String sql = "DELETE FROM POST WHERE Id = ?";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.clearBindings();
        statement.bindString(1, String.valueOf(Id));
        statement.execute();
        database.close();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}