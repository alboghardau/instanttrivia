package com.itmc.instanttrivia;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class DatabaseHandler extends SQLiteOpenHelper{

    private static String DB_PATH = "data/data/com.itmc.instanttrivia/databases/";
    private static final int DATABASE_VERSION = 49;
    private static final String DB_NAME = "answerit.db";
    private final Context myContext;

    private static SQLiteDatabase myDataBase;

    public DatabaseHandler(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.myContext = context;
    }

    public void createDataBase() throws IOException{

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
            Log.e("Database", "Database exists!");
        }else{
            this.getReadableDatabase();
            try {
                copyDatabase();
            } catch (Exception e) {
                throw new Error("Error copying database");
            }
        }
    }

    public void deleteDB(){
        File dbFile = new File(DB_PATH + DB_NAME);
        dbFile.delete();
    }

    public boolean checkDataBase(){

        File dbFile = new File(DB_PATH + DB_NAME);
        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
            Log.e("DB EXISTS","DB EXISTS");
        }catch(SQLiteException e){
                //database does't exist yet.
        }
        if(checkDB != null){
                checkDB.close();
        }
        return checkDB != null ? true : false;
    }

    public void copyDatabase() {

        InputStream myInput;
        OutputStream outStream;
        try {
            myInput = myContext.getAssets().open(DB_NAME);
            String file = DB_PATH + DB_NAME;
            outStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = myInput.read(buffer)) >= 0) {
                outStream.write(buffer, 0, length);
            }
            outStream.flush();
            myInput.close();
            outStream.close();
            Log.e("DB", "DB COPIED");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }



    public void openDataBase() throws SQLException{
        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);


    }

    public synchronized void close() {
        if(myDataBase != null)
            myDataBase.close();
        super.close();
    }

    public void onCreate(SQLiteDatabase db){
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

    }
}