package itmc.instanttrivia;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;


public class Game_Timer extends ActionBarActivity {

    TextView text_question;
    LinearLayout lin_answer;

    String question;
    String answer;
    String randomchars;
    ArrayList<Character> c;
    ArrayList<Character> ans_arr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game__timer);

        question = "Q?";
        answer = "SAURON";
        randomchars =  "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        //define veriables
        text_question = (TextView) findViewById(R.id.text_question);
        lin_answer = (LinearLayout) findViewById(R.id.linear_answer);

        text_question.setText(question);
        clear_answer(answer);

        //declare answer chars store , and store answer in array
        c = new ArrayList<Character>();
        ans_arr = new ArrayList<Character>();
        for(Character ch: answer.toCharArray())
        {
            ans_arr.add(ch);
        }

        //gen random chars
        fill_answer_chars(answer);

        display_start_randoms();
    }

    //generate textview for answer chars and fill them in linear layout
    private void clear_answer(String ans)
    {
        for( Character ch: ans.toCharArray()){
            TextView t = new TextView(this);
            t.setPadding(10,15,10,15);
            t.setTextSize(20);
            t.setTextColor(Color.WHITE);
            t.setText("_");
            lin_answer.addView(t);
        }
    }

    private void display_start_randoms(){

        LinearLayout line1 = (LinearLayout) findViewById(R.id.linear_ans_first);
        LinearLayout line2 = (LinearLayout) findViewById(R.id.lineage_ans_second);

        for(int i = 0; i < 8; i++){
            TextView t = new TextView(this);
            t.setText(c.get(i).toString());
            t.setBackgroundDrawable(getResources().getDrawable(R.drawable.text_view_all_indigo500));
            t.setTextColor(getResources().getColor(R.color.white));
            t.setTypeface(Typeface.MONOSPACE);
            t.setPadding(40,20,40,20);
            t.setTextSize(40);
            t.setGravity(Gravity.CENTER);
            //generate id starting with 100
            t.setId(100+i);

            //set clicker
            final int finalI = i;
            t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   click_btn(100+ finalI);
                }
            });

            //display text view
            if( i < 4)
            {
                line1.addView(t);
            }else{
                line2.addView(t);
            }
        }
    }

    private void click_btn(int id){

        TextView t = (TextView) findViewById(id);
        Character c_btn = t.getText().charAt(0);




        if(ans_arr.contains(c_btn) == true ){
            Log.e("text char press", "TRUE");
        }


    }

    //generate 8 random chars containing minimum 3 answer chars at beggining
    private void fill_answer_chars(String ans) {
        ArrayList<Character> a = new ArrayList<Character>();
        Character cha = null;
        Random rnd = new Random();
        //set answer to array
        for (Character ch : ans.toCharArray()) {
            a.add(ch);
        }

        //generate 8 random chars
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < randomchars.length(); j++) {
                cha = randomchars.charAt(rnd.nextInt(randomchars.length()));
                if (c.contains(cha) == false) {
                    c.add(cha);
                    break;
                }
            }
        }

        //generate three random index
        ArrayList<Integer> rand_index_ans = new ArrayList<>();
        for(int i = 0; i < ans.length(); i++){
            rand_index_ans.add(i);
        }
        ArrayList<Integer> rand_index_8 = new ArrayList<>();
        for( int i = 0; i < 8; i++) {
            rand_index_8.add(i);
        }

        //shuffle random numbers
        Collections.shuffle(rand_index_ans);
        Collections.shuffle(rand_index_8);

        //replace three chars
        for(int i = 0; i < 3; i++){
            if( c.contains(a.get(rand_index_ans.get(i))) == false)
            {
                c.set(rand_index_8.get(i),a.get(rand_index_ans.get(i)));
            }
        }

        Log.e("Answ Index", rand_index_ans.toString());
        Log.e("8 Index", rand_index_8.toString());
        Log.e("Answer", a.toString());
        Log.e("Answer Chars:", c.toString());
    }


}
