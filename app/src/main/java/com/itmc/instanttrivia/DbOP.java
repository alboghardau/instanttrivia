package com.itmc.instanttrivia;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.*;
import android.util.Log;

import com.itmc.instanttrivia.DatabaseHandler;

public class DbOP {

    public DatabaseHandler mydbhelp = null;
    public SQLiteDatabase db;
    public int db_version = 41; //TODO UPDATE VARIABLE WHEN DATABASE IS UPDATED. VARIABLE HAS TO BE IDENTICAL AS THE ONE ON DATABASE TABLE version.
    private int max_in_category = 1;

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
        else{
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

    public ArrayList<String> read_10_questions(int difficulty, int category){

        Cursor cursor = null;
        ArrayList<String> result = new ArrayList<String>();

        String where = "WHERE diff = " + difficulty+" AND cat_id = "+ category;
        if(difficulty == 5) where = "WHERE cat_id = "+ category;
        if(category == 1) where = "WHERE diff = "+ difficulty;
        if(difficulty == 5 && category == 1) where = "";

        cursor = db.rawQuery("SELECT * FROM quest "+where+" ORDER BY played,RANDOM() LIMIT 10", null);

        cursor.moveToFirst();

        while(cursor.isAfterLast() == false){

            result.add(cursor.getString(1));    //question
            result.add(cursor.getString(2));    //answer
            result.add(cursor.getString(4));    //categoryname

            update_db_played(cursor.getInt(0), cursor.getInt(6)+1);

            cursor.moveToNext();
        }
        cursor.close();
        return result;
    }

    //UPDATES THE PLAYED COLUMN OF THE QUESTION TABLE
    private void update_db_played(int id,int played){
        Cursor cursor = null;

        cursor = db.rawQuery("UPDATE quest SET played="+played+" WHERE id="+id,null);
        cursor.moveToFirst();
        cursor.close();
    }

    //TEST PURPOSE ONLY READS A QUESTION
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

        question[0] = cursor.getString(index);
        question[1] = cursor.getString(index2);

        cursor.close();
        return question;
    }

    //READS CATEGORIES WITH A MINIMUM NO O QUESTIONS/EACH DIFFICULTY
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
            if(diff == 5 && cursor.getInt(2) > max_in_category){
                  Log.e("Total_No", cursor.getInt(2)+"");

                  cat.add(cursor.getString(1));
                  cat.add(cursor.getInt(0)+"");
            }
            if(diff == 1 && cursor.getInt(3) > max_in_category){
                Log.e("Easy_No", cursor.getInt(3)+"");
                cat.add(cursor.getString(1));
                cat.add(cursor.getInt(0)+"");
            }
            if(diff == 2 && cursor.getInt(4) > max_in_category){
                Log.e("Med_No", cursor.getInt(4)+"");
                cat.add(cursor.getString(1));
                cat.add(cursor.getInt(0)+"");
            }
            if(diff == 3 && cursor.getInt(5) > max_in_category){
                Log.e("Hard_No", cursor.getInt(5)+"");
                cat.add(cursor.getString(1));
                cat.add(cursor.getInt(0)+"");
            }

            cursor.moveToNext();
        }
        cursor.close();
        return cat;
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