package com.itmc.instanttrivia;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.*;
import android.util.Log;

import com.itmc.instanttrivia.DatabaseHandler;

public class DbOP {

    public DatabaseHandler mydbhelp = null;
    public SQLiteDatabase db;
    public int db_version = 38; //TODO UPDATE VARIABLE WHEN DATABASE IS UPDATED. VARIABLE HAS TO BE IDENTICAL AS THE ONE ON DATABASE TABLE version.

    ArrayList<Integer> seen = new ArrayList<Integer>();

    public DbOP(Context context){
        mydbhelp = new DatabaseHandler(context);
    }

    //return database version from table version
    public int db_ver(){
        int dbver = 0;
        Cursor cursor;
        cursor = db.query("version", null, null, null, null, null, null);
        cursor.moveToFirst();
        dbver = cursor.getInt(0);

        cursor.close();
        return dbver;
    }

    //test if newer database has been added delete old one upload new one
    public void testnewdb()
    {

        if(mydbhelp.checkDataBase() == true)
        {
            try{
                mydbhelp.openDataBase();
            } catch (SQLException sqle) {
                throw sqle;
            }

            db = mydbhelp.getReadableDatabase();

            int i = db_ver();
            Log.e("Database Version", i+"");
            if(db_version > i)
            {
                mydbhelp.deleteDB();
                try{
                    mydbhelp.createDataBase();

                }catch(IOException ioe) {
                    throw new Error("Unable to create db");
                }
                Log.e("DB","Questions DB Upgrade working");
            }

        }
        else
        {
            try{
                mydbhelp.createDataBase();

            }catch(IOException ioe) {
                throw new Error("Unable to create db");
            }
        }
    }

    //to start and read db
    public void startdb()
    {
        try{
            mydbhelp.createDataBase();

        }catch(IOException ioe) {
            throw new Error("Unable to create db");
        }

        try{
            mydbhelp.openDataBase();

        } catch (SQLException sqle) {
            throw sqle;
        }

        db = mydbhelp.getReadableDatabase();
    }

    //returns question
    public String[] read_rand_question_difficulty(int diff, int cat){

        String[] question = new String[4];
        Cursor cursor = null;

        String id = "diff = " + diff+" AND cat_id = "+ cat;

        if(diff == 5) id = "cat_id = "+ cat;
        if(cat == 1) id = "diff = "+ diff;
        if(diff == 5 && cat == 1) id = null;
        int result;

        cursor = db.query("quest", null, id, null, null, null, null);

        result = cursor.getCount();

        //get random return id
        Random r = new Random();
        int i = (r.nextInt(result));

        while(seen.contains((Integer)i) == true)
        {
            i = (r.nextInt(result));
        }
        cursor.moveToPosition(i);
        seen.add((Integer)i);

        //prevents repeating questions
        if(seen.size() > 10)
        {
            seen.clear();
        }
        Log.e("q no", seen.size()+"");

        question[0] = cursor.getString(cursor.getColumnIndex("question"));
        question[1] = cursor.getString(cursor.getColumnIndex("answer"));
        question[2] = cursor.getString(cursor.getColumnIndex("cat_name"));
        question[3] = cursor.getString(cursor.getColumnIndex("diff"));

        cursor.close();
        return question;
    }

    //test purpose only
    public String[] read_spec_questions(int idq)
    {
        String[] question = new String[4];
        Cursor cursor = null;

        String id = "id = " + idq;

        int result;

        cursor = db.query("quest", null, id, null, null, null, null);

        result = cursor.getCount();

        //get random return id
        Random r = new Random();
        int i = (r.nextInt(result));

        cursor.moveToPosition(i);
        int index = cursor.getColumnIndex("question");
        int index2 = cursor.getColumnIndex("answer");
        int index3 = cursor.getColumnIndex("cat");
        int index4 = cursor.getColumnIndex("diff");

        question[0] = cursor.getString(index);
        question[1] = cursor.getString(index2);
        question[2] = cursor.getString(index3);
        question[3] = cursor.getString(index4);

        cursor.close();
        return question;
    }

    //reads categories that have tests
    public ArrayList<String> read_cats(int diff)
    {
        Cursor cursor = null;

        cursor = db.rawQuery("SELECT * FROM cats", null);
        cursor.moveToFirst();
       // String[][] cats = new String[][];

        ArrayList<String> cat = new ArrayList<String>();
        ArrayList<Integer> cat_id = new ArrayList<Integer>();

        int i = 0;
        while(cursor.isAfterLast() == false)
        {
            int id = cursor.getInt(0);
            if(diff == 5 && cursor.getInt(2) > 25){
                  Log.e("Total_No", cursor.getInt(2)+"");

                  cat.add(cursor.getString(1));
                  cat.add(cursor.getInt(0)+"");
            }
            if(diff == 1 && cursor.getInt(3) > 25){
                Log.e("Easy_No", cursor.getInt(3)+"");
                cat.add(cursor.getString(1));
                cat.add(cursor.getInt(0)+"");
            }
            if(diff == 2 && cursor.getInt(4) > 25){
                Log.e("Med_No", cursor.getInt(4)+"");
                cat.add(cursor.getString(1));
                cat.add(cursor.getInt(0)+"");
            }
            if(diff == 3 && cursor.getInt(5) > 25){
                Log.e("Hard_No", cursor.getInt(5)+"");
                cat.add(cursor.getString(1));
                cat.add(cursor.getInt(0)+"");
            }

            cursor.moveToNext();
        }
        cursor.close();
        return cat;
    }

    public String[][] q_test(int test_id)
    {
        Cursor cursor = null;
        String[][] test = new String[10][3];
        cursor = db.rawQuery("SELECT * FROM quest WHERE test_id="+test_id, null);
        cursor.moveToFirst();

        int i = 0;
        while(cursor.isAfterLast() == false)
        {
            test[i][0] = cursor.getString(1);
            test[i][1] = cursor.getString(2);
            test[i][2] = cursor.getString(4);
            i++;
            cursor.moveToNext();
        }

        cursor.close();
        return test;
    }

    public void close(){
        if(mydbhelp != null)
        {
            mydbhelp.close();
        }
        if(db != null)
        {
            db.close();
        }
    }

}