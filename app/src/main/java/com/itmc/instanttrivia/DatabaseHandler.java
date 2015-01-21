package com.itmc.instanttrivia;


import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;



public class DatabaseHandler extends SQLiteOpenHelper{

    private static String DB_PATH = "data/data/com.itmc.instanttrivia/databases/";
    private static final int DATABASE_VERSION = 1;
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
                copyDataBase();
            } catch (IOException e) {
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
        return dbFile.exists();
// this part was causing application to crash :-S         
//            SQLiteDatabase checkDB = null;
//     
//            try{
//            String myPath = DB_PATH + DB_NAME;
//            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
//                
//                
//     
//            }catch(SQLiteException e){     
//                //database does't exist yet.     
//            }     
//            if(checkDB != null){     
//                checkDB.close();     
//            }     
//            return checkDB != null ? true : false;
    }

    private void copyDataBase() throws IOException{
        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }
        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
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