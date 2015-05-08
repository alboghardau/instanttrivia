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
    public int db_version = 44; //TODO UPDATE VARIABLE WHEN DATABASE IS UPDATED. VARIABLE HAS TO BE IDENTICAL AS THE ONE ON DATABASE TABLE version.
    private int max_in_category = 1;

    ArrayList<Integer> seen = new ArrayList<>();

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
            sqle.printStackTrace();
            throw sqle;
        }

        db = mydbhelp.getReadableDatabase();
    }

    //returns question
    public String[] read_rand_question_difficulty(int diff, int cat){

        String[] question = new String[4];

        String id = "diff = " + diff+" AND cat_id = "+ cat;

        if(diff == 5) id = "cat_id = "+ cat;
        if(cat == 1) id = "diff = "+ diff;
        if(diff == 5 && cat == 1) id = null;
        int result;

        Cursor cursor = db.query("quest", null, id, null, null, null, null);

        result = cursor.getCount();

        //get random return id
        Random r = new Random();
        int i = (r.nextInt(result));

        while(seen.contains(i)){
            i = (r.nextInt(result));
        }
        cursor.moveToPosition(i);
        seen.add(i);

        //prevents repeating questions
        if(seen.size() > 10){
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

        ArrayList<String> result = new ArrayList<>();

        String where = "WHERE diff = " + difficulty+" AND cat_id = "+ category;
        if(difficulty == 5) where = "WHERE cat_id = "+ category;
        if(category == 1) where = "WHERE diff = "+ difficulty;
        if(difficulty == 5 && category == 1) where = "";

        Cursor cursor = db.rawQuery("SELECT * FROM quest "+where+" ORDER BY played,RANDOM() LIMIT 10", null);

        cursor.moveToFirst();

        while(!cursor.isAfterLast()){

            result.add(cursor.getInt(0) + "");  //id
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
        Cursor cursor = db.rawQuery("UPDATE quest SET played="+played+" WHERE id="+id,null);
        cursor.moveToFirst();
        cursor.close();
    }

    //UPDATE QUESTION FROM JSON, DOING APPROPIATE ACTION
    public void updateQuestionFromJSON(int id, String question, String answer, int cat_id, String cat_name, int diff, long time_stamp,ArrayList<Integer> arrayList){

        ContentValues contentValues = new ContentValues();
        contentValues.put("question", question);
        contentValues.put("answer", answer);
        contentValues.put("cat_id", cat_id);
        contentValues.put("cat_name", cat_name);
        contentValues.put("diff", diff);
        contentValues.put("time_stamp", time_stamp);

        if(arrayList.contains(id)){
            db.update("quest", contentValues, "id=" + id, null);
            Log.e("SYNC", "Updated "+id);
        }else{
            contentValues.put("id", id);
            db.insert("quest", null, contentValues);
            Log.e("SYNC", "Inserted " + id);
        }
    }

    //READ ALL EXISTING IDS
    public ArrayList<Integer> readAllIds(){
        ArrayList<Integer> arrayList = new ArrayList();
        Cursor cursor = db.rawQuery("SELECT * FROM quest",null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            arrayList.add(cursor.getInt(0));
            cursor.moveToNext();
        }
        cursor.close();
        return arrayList;
    }

    //TEST PURPOSE ONLY READS A QUESTION
    public String[] read_spec_questions(int idq){
        String[] question = new String[4];
        String id = "id = " + idq;
        int result;

        Cursor cursor = db.query("quest", null, id, null, null, null, null);

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
        Cursor cursor = db.rawQuery("SELECT * FROM cats", null);
        cursor.moveToFirst();

        ArrayList<String> cat = new ArrayList<>();

        int i = 0;
        while(!cursor.isAfterLast())
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