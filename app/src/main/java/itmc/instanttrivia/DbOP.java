package itmc.instanttrivia;

import java.io.IOException;
import java.util.*;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.*;
import android.util.Log;

import itmc.instanttrivia.DatabaseHandler;


public class DbOP {

    public DatabaseHandler mydbhelp = null;
    public SQLiteDatabase db;
    public int db_version = 27; //variable to update when database is updated

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
            Log.i("test", i+"");
            if(db_version > i)
            {
                mydbhelp.deleteDB();
                try{
                    mydbhelp.createDataBase();

                }catch(IOException ioe) {
                    throw new Error("Unable to create db");
                }
                Log.i("DB","Questions DB Upgrade working");
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
    public String[] read_rand_question_difficulty(int diff){

        String[] question = new String[4];
        Cursor cursor = null;


        String id = "diff = " + diff;

        if(diff == 5) id = null;
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
        if(seen.size() > 50)
        {
            seen.clear();
        }
        Log.i("q no", seen.size()+"");

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
    public String[][] read_cats()
    {
        Cursor cursor = null;

        cursor = db.rawQuery("SELECT * FROM cats", null);
        cursor.moveToFirst();
        String[][] cats = new String[cursor.getCount()+1][2];
        int i = 0;
        while(cursor.isAfterLast() == false)
        {
            int id = cursor.getInt(0);
            Cursor cur = null;
            cur = db.rawQuery("SELECT * FROM tests WHERE cat_id="+id, null);

            if(cur.getCount() > 0)
            {
                cats[i][0] = Integer.toString(cursor.getInt(0));
                cats[i][1] = cursor.getString(1);
                i++;
            }

            cursor.moveToNext();

        }

        cursor.close();
        return cats;
    }


    public Integer[][] read_tests(int cat_id)
    {
        Cursor cursor = null;

        cursor = db.rawQuery("SELECT * FROM tests WHERE cat_id = " + cat_id +" ORDER BY diff ASC, id ASC", null);
        cursor.moveToFirst();
        Integer[][] tests = new Integer[cursor.getCount()+1][3];

        Log.i("count cat_id", cat_id + "");
        Log.i("count test", cursor.getCount() +"");

        int i = 0;
        while(cursor.isAfterLast() == false)
        {
            tests[i][0] = cursor.getInt(0);
            tests[i][1] = cursor.getInt(2);
            tests[i][2] = 0;
            Log.i("sql_test", cursor.getInt(0) + "");

            i++;
            cursor.moveToNext();
        }

        cursor.close();
        return tests;
    }

    public String[] read_topics(int cat_id)
    {
        Cursor cursor = null;

        cursor = db.rawQuery("SELECT * FROM tests WHERE cat_id= "+ cat_id +" ORDER BY diff ASC, id ASC", null);
        cursor.moveToFirst();
        String[] topics = new String[cursor.getCount()+1];

        int i = 0;
        while(cursor.isAfterLast() == false)
        {
            topics[i] = cursor.getString(3);
            cursor.moveToNext();
            i++;
        }

        cursor.close();
        return topics;
    }

    public int test_countr(int cat_id)
    {
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT * FROM tests WHERE cat_id=" + cat_id, null);

        int contor = cursor.getCount();

        cursor.close();
        return contor;
    }

    public String read_topic(int test_id)
    {
        String topic = null;
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT * FROM tests WHERE id=" + test_id, null);
        cursor.moveToFirst();
        topic = cursor.getString(3);

        cursor.close();
        return topic;
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